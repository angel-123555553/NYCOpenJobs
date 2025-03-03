package com.example.nycopenjobs.data

import android.content.SharedPreferences
import android.util.Log
import com.example.nycopenjobs.api.NycOpenDataApi
import com.example.nycopenjobs.model.JobPost
import com.example.nycopenjobs.util.TAG
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

interface AppRepository {
    fun getScrollPosition(): Int
    fun setScrollPosition(position: Int)
    suspend fun getJobPostings(): List<JobPost>
    suspend fun getJobPost(jobId: Int): JobPost
    suspend fun updateFavorite(jobId: Int, isFavorite: Boolean)
    suspend fun searchJobs(query: String, onlyFavorites: Boolean): List<JobPost>
}

class AppRepositoryImpl(
    private val nycOpenDataApi: NycOpenDataApi,
    private val sharedPreferences: SharedPreferences,
    private val dao: JobPostDao,
) : AppRepository {

    private val scrollPositionKey = "scroll_position"
    private val offsetKey = "offset"
    private var offset = sharedPreferences.getInt(offsetKey, 0)
    private var totalJobs = 0

    private fun updateOffset() {
        offset += (totalJobs - offset)
        Log.i(TAG, "offset: $offset")
        sharedPreferences.edit().putInt(offsetKey, offset).apply()
    }

    private fun updateTotalJobs(newTotal: Int) {
        totalJobs = newTotal
        Log.i(TAG, "total jobs: $totalJobs")
    }

    override suspend fun getJobPostings(): List<JobPost> {
        Log.i(TAG, "getting job postings")

        updateOffset()

        val localData = dao.getAll().first()
        updateTotalJobs(localData.size)

        return if (offset == totalJobs) {
            Log.i(TAG, "getting job posting via API")
            val jobs = try {
                nycOpenDataApi.getJobPostings(offset)
            } catch (e: IOException) {
                Log.e(TAG, e.message ?: "IOException")
                emptyList()
            } catch (e: HttpException) {
                Log.e(TAG, e.message ?: "HttpException")
                emptyList()
            }

            if (jobs.isNotEmpty()) {
                Log.i(TAG, "API returned ${jobs.size} jobs. updating local database")
                dao.upsert(jobs)
            }

            val updatedJobs = dao.getAll().first()
            updateTotalJobs(updatedJobs.size)
            Log.i(TAG, "returning updated jobs from API")
            updatedJobs
        } else {
            Log.i(TAG, "returning local data")
            localData
        }
    }

    override suspend fun getJobPost(jobId: Int): JobPost {
        Log.i(TAG, "getting job post $jobId")
        return dao.get(jobId)
    }

    override fun getScrollPosition(): Int {
        val scrollPosition = sharedPreferences.getInt(scrollPositionKey, 0)
        Log.i(TAG, "scroll position: $scrollPosition")
        return scrollPosition
    }

    override fun setScrollPosition(position: Int) {
        Log.i(TAG, "setting scroll position: $position")
        sharedPreferences.edit().putInt(scrollPositionKey, position).apply()
    }

    override suspend fun updateFavorite(jobId: Int, isFavorite: Boolean) {
        dao.updateFavoriteStatus(jobId, isFavorite)
    }

    override suspend fun searchJobs(query: String, onlyFavorites: Boolean): List<JobPost> {
        val allJobs = dao.getAll().first()
        val filtered = allJobs.filter {
            (it.agency.contains(query, ignoreCase = true) ||
                    it.businessTitle.contains(query, ignoreCase = true))
        }.filter {
            if (onlyFavorites) it.isFavorite else true
        }
        return filtered
    }
}
