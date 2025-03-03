package com.example.nycopenjobs.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.nycopenjobs.NYCOpenJobsApp
import com.example.nycopenjobs.data.AppRepository
import com.example.nycopenjobs.model.JobPost
import com.example.nycopenjobs.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed interface HomeScreenUIState {
    data class Success(val data: List<JobPost>) : HomeScreenUIState
    object Error : HomeScreenUIState
    object Loading : HomeScreenUIState
    object Ready : HomeScreenUIState
}

class HomeScreenViewModel(private val appRepository: AppRepository) : ViewModel() {

    var uiState: HomeScreenUIState by mutableStateOf(HomeScreenUIState.Ready)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var showFavorites by mutableStateOf(false)
        private set

    init {
        getJobpostings()
    }

    fun getJobpostings() {
        viewModelScope.launch {
            uiState = HomeScreenUIState.Loading
            uiState = try {
                HomeScreenUIState.Success(appRepository.getJobPostings())
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
                HomeScreenUIState.Error
            }
        }
    }

    fun toggleShowFavorites() {
        showFavorites = !showFavorites
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            uiState = HomeScreenUIState.Loading
            val filteredJobs = appRepository.searchJobs(searchQuery, showFavorites)
            uiState = HomeScreenUIState.Success(filteredJobs)
        }
    }

    suspend fun toggleFavorite(jobId: Int, currentFavorite: Boolean) {
        with(viewModelScope) {
            launch(Dispatchers.IO) {
                appRepository.updateFavorite(jobId, !currentFavorite)
                // Re-apply filters after updating favorite status
                val filteredJobs = appRepository.searchJobs(searchQuery, showFavorites)
                uiState = HomeScreenUIState.Success(filteredJobs)
            }
        }
    }

    fun getScrollPosition(): Int {
        return appRepository.getScrollPosition()
    }

    fun setScrollPosition(position: Int) {
        appRepository.setScrollPosition(position)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                Log.i(TAG, "view model factory: getting app container")
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as NYCOpenJobsApp
                val appContainer = application.container
                return HomeScreenViewModel(appContainer.appRepository) as T
            }
        }
    }
}
