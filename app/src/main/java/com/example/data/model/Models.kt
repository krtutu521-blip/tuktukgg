package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val teacher: String,
    val totalLectures: Int,
    val totalPdfs: Int,
    val progress: Float // 0.0f to 1.0f
) : Serializable

data class Subject(
    val id: String,
    val courseId: String,
    val name: String,
    val iconName: String,
    val totalVideos: Int,
    val totalNotes: Int,
    val progressPercent: Int // 0 to 100
) : Serializable

data class Lecture(
    val id: String,
    val subjectId: String,
    val title: String,
    val duration: String,
    val teacher: String,
    val videoUrl: String,
    val pdfUrl: String,
    val isCompleted: Boolean,
    val isDownloaded: Boolean
) : Serializable

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val filePath: String,
    val status: String, // "PENDING", "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED"
    val progress: Int // 0 to 100
) : Serializable

data class UserProfile(
    val name: String,
    val avatar: String,
    val purchasedCourses: List<Course>,
    val overallProgress: Float,
    val certificates: List<String>
) : Serializable
