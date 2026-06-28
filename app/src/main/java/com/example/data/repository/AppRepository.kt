package com.example.data.repository

import android.content.Context
import com.example.data.api.AppApiService
import com.example.data.api.LiveClass
import com.example.data.api.AppNotification
import com.example.data.api.LoginRequest
import com.example.data.api.ProgressSyncRequest
import com.example.data.api.NetworkClient
import com.example.data.database.AppDatabase
import com.example.data.database.LectureProgress
import com.example.data.database.TokenEntity
import com.example.data.model.Course
import com.example.data.model.Subject
import com.example.data.model.Lecture
import com.example.data.model.DownloadItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class AppRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val apiService: AppApiService = NetworkClient.getApiService(context)

    // Downloads Local DB access
    val allDownloadsFlow: Flow<List<DownloadItem>> = database.downloadDao().getAllDownloadsFlow()

    suspend fun getAllDownloads(): List<DownloadItem> = database.downloadDao().getAllDownloads()

    suspend fun getDownloadById(id: String): DownloadItem? = database.downloadDao().getDownloadById(id)

    suspend fun insertOrUpdateDownload(item: DownloadItem) = database.downloadDao().insertOrUpdate(item)

    suspend fun deleteDownload(item: DownloadItem) {
        // Delete physical file if exists
        try {
            val file = File(item.filePath)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        database.downloadDao().delete(item)
    }

    suspend fun deleteDownloadById(id: String) {
        val item = database.downloadDao().getDownloadById(id)
        if (item != null) {
            deleteDownload(item)
        }
    }

    // Auth Token persistence
    suspend fun saveToken(token: String) {
        database.tokenDao().saveToken(TokenEntity(token = token))
    }

    suspend fun getToken(): String {
        return database.tokenDao().getToken()?.token ?: ""
    }

    suspend fun clearToken() {
        database.tokenDao().clearToken()
    }

    // Remote REST API network operations
    suspend fun login(username: String, email: String): String {
        val response = apiService.login(LoginRequest(username, email))
        if (response.success) {
            saveToken(response.token)
            return response.token
        }
        throw Exception(response.message)
    }

    suspend fun getCourses(): List<Course> = apiService.getCourses()

    suspend fun getSubjects(courseId: String): List<Subject> = apiService.getSubjects(courseId)

    suspend fun getLectures(subjectId: String): List<Lecture> {
        val remoteLectures = apiService.getLectures(subjectId)
        // Combine with local completion status from Database
        return remoteLectures.map { lecture ->
            val localProgress = database.lectureProgressDao().getProgressForLecture(lecture.id)
            val isCompleted = localProgress?.isCompleted ?: lecture.isCompleted
            // Also check if downloaded locally
            val localDownload = database.downloadDao().getDownloadById(lecture.id)
            val isDownloaded = localDownload?.status == "COMPLETED"

            lecture.copy(isCompleted = isCompleted, isDownloaded = isDownloaded)
        }
    }

    suspend fun getLiveClasses(): List<LiveClass> = apiService.getLiveClasses()

    suspend fun getNotifications(): List<AppNotification> = apiService.getNotifications()

    suspend fun getUserProfile(): UserProfile {
        val remoteProfile = apiService.getProfile()
        // Load purchased courses dynamically from getCourses API to enrich UI
        val courses = getCourses()
        return remoteProfile.copy(purchasedCourses = courses)
    }

    // Lecture Progress / Resume Position Tracker
    suspend fun getLectureProgress(lectureId: String): LectureProgress? {
        return database.lectureProgressDao().getProgressForLecture(lectureId)
    }

    suspend fun saveLectureProgress(lectureId: String, isCompleted: Boolean, resumePositionMs: Long) {
        database.lectureProgressDao().insertOrUpdate(
            LectureProgress(
                lectureId = lectureId,
                isCompleted = isCompleted,
                resumePositionMs = resumePositionMs
            )
        )
        // Sync with cloud server in the background
        try {
            apiService.syncProgress(
                ProgressSyncRequest(
                    lectureId = lectureId,
                    isCompleted = isCompleted,
                    lastPositionMs = resumePositionMs
                )
            )
        } catch (e: Exception) {
            e.printStackTrace() // Keep robust for offline usage
        }
    }
}
