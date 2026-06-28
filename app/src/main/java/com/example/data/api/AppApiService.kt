package com.example.data.api

import com.example.data.model.Course
import com.example.data.model.Subject
import com.example.data.model.Lecture
import com.example.data.model.UserProfile
import retrofit2.http.*

data class LoginRequest(val username: String, val email: String)
data class LoginResponse(val success: Boolean, val token: String, val refreshToken: String, val message: String)
data class TokenRefreshRequest(val refreshToken: String)
data class TokenRefreshResponse(val token: String, val refreshToken: String)

data class LiveClass(
    val id: String,
    val title: String,
    val teacher: String,
    val startTime: String,
    val streamUrl: String,
    val isLive: Boolean
)

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String
)

data class ProgressSyncRequest(
    val lectureId: String,
    val isCompleted: Boolean,
    val lastPositionMs: Long
)

data class GenericResponse(val success: Boolean, val message: String)

interface AppApiService {
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("v1/auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): TokenRefreshResponse

    @GET("v1/courses")
    suspend fun getCourses(): List<Course>

    @GET("v1/courses/{courseId}/subjects")
    suspend fun getSubjects(@Path("courseId") courseId: String): List<Subject>

    @GET("v1/subjects/{subjectId}/lectures")
    suspend fun getLectures(@Path("subjectId") subjectId: String): List<Lecture>

    @GET("v1/live-classes")
    suspend fun getLiveClasses(): List<LiveClass>

    @GET("v1/notifications")
    suspend fun getNotifications(): List<AppNotification>

    @GET("v1/profile")
    suspend fun getProfile(): UserProfile

    @POST("v1/progress/sync")
    suspend fun syncProgress(@Body request: ProgressSyncRequest): GenericResponse
}
