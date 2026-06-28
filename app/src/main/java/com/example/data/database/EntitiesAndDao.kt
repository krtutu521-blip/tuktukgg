package com.example.data.database

import androidx.room.*
import com.example.data.model.DownloadItem
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lecture_progress")
data class LectureProgress(
    @PrimaryKey val lectureId: String,
    val isCompleted: Boolean,
    val resumePositionMs: Long
)

@Entity(tableName = "auth_tokens")
data class TokenEntity(
    @PrimaryKey val key: String = "jwt_token",
    val token: String
)

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads")
    fun getAllDownloadsFlow(): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads")
    suspend fun getAllDownloads(): List<DownloadItem>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(downloadItem: DownloadItem)

    @Delete
    suspend fun delete(downloadItem: DownloadItem)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface LectureProgressDao {
    @Query("SELECT * FROM lecture_progress")
    fun getAllProgressFlow(): Flow<List<LectureProgress>>

    @Query("SELECT * FROM lecture_progress WHERE lectureId = :lectureId")
    suspend fun getProgressForLecture(lectureId: String): LectureProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(lectureProgress: LectureProgress)
}

@Dao
interface TokenDao {
    @Query("SELECT * FROM auth_tokens WHERE `key` = 'jwt_token'")
    suspend fun getToken(): TokenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(tokenEntity: TokenEntity)

    @Query("DELETE FROM auth_tokens WHERE `key` = 'jwt_token'")
    suspend fun clearToken()
}

@Database(
    entities = [DownloadItem::class, LectureProgress::class, TokenEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun lectureProgressDao(): LectureProgressDao
    abstract fun tokenDao(): TokenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appx_learn_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
