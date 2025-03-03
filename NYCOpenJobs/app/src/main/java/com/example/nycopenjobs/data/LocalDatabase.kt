package com.example.nycopenjobs.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.example.nycopenjobs.model.JobPost
import com.example.nycopenjobs.util.TAG
import kotlinx.coroutines.flow.Flow

@Dao
interface JobPostDao {
    @Query("SELECT * FROM JobPost ORDER BY postingLastUpdated DESC")
    fun getAll(): Flow<List<JobPost>>

    @Query("SELECT * FROM JobPost WHERE jobId = :id")
    suspend fun get(id: Int): JobPost  // Now suspend

    @Upsert(entity = JobPost::class)
    suspend fun upsert(jobPostings: List<JobPost>)

    @Query("UPDATE JobPost SET isFavorite = :isFavorite WHERE jobId = :jobId")
    suspend fun updateFavoriteStatus(jobId: Int, isFavorite: Boolean)
}

@Database(entities = [JobPost::class], version = 2, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun jobPostDao(): JobPostDao

    companion object {
        private const val DATABASE = "local_database"

        @Volatile
        private var Instance: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, LocalDatabase::class.java, DATABASE)
                    .fallbackToDestructiveMigration()
                    .build().also { Instance = it }
            }
        }
    }
}
