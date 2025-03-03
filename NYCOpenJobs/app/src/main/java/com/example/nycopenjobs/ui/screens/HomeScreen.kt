package com.example.nycopenjobs.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nycopenjobs.R
import com.example.nycopenjobs.model.JobPost
import com.example.nycopenjobs.util.LoadingSpinner
import com.example.nycopenjobs.util.ToastMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState = viewModel.uiState
    val searchQuery = viewModel.searchQuery
    val showFavorites = viewModel.showFavorites

    Scaffold(
        topBar = {
            HomeTopBar(
                searchQuery = searchQuery,
                onSearchQueryChanged = viewModel::onSearchQueryChanged
            )
        },
        bottomBar = {
            BottomNavigationBar(
                showFavorites = showFavorites,
                onToggleFavorites = viewModel::toggleShowFavorites
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is HomeScreenUIState.Loading -> LoadingSpinner()
            is HomeScreenUIState.Success -> {
                JobListings(
                    jobs = uiState.data,
                    loadMoreData = { viewModel.getJobpostings() },
                    updateScrollPosition = { viewModel.setScrollPosition(it) },
                    scrollPosition = viewModel.getScrollPosition(),
                    modifier = modifier.padding(paddingValues),
                    onItemClick = onItemClick
                )
            }
            is HomeScreenUIState.Error -> ToastMessage(stringResource(R.string.job_listing_not_available_at_this_time))
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    var searching by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("NYC Jobs") },
        actions = {
            if (searching) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier.width(200.dp)
                )
                IconButton(onClick = { searching = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                }
            } else {
                IconButton(onClick = { searching = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }
    )
}

@Composable
fun BottomNavigationBar(
    showFavorites: Boolean,
    onToggleFavorites: () -> Unit
) {
    BottomAppBar {
        IconButton(onClick = {
            if (showFavorites) onToggleFavorites()
        }) {
            Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                tint = if (!showFavorites) Color.Blue else Color.White
            )
        }

        IconButton(onClick = {
            onToggleFavorites()
        }) {
            Icon(
                imageVector = if (showFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorites",
                tint = if (showFavorites) Color.Red else Color.White
            )
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
fun JobListings(
    jobs: List<JobPost>,
    loadMoreData: () -> Unit,
    updateScrollPosition: (Int) -> Unit,
    scrollPosition: Int,
    modifier: Modifier,
    onItemClick: (Int) -> Unit
) {
    val firstVisibleIndex = if (scrollPosition > jobs.size) 0 else scrollPosition
    val listState: LazyListState = rememberLazyListState(firstVisibleIndex)

    LazyColumn(modifier = modifier, state = listState) {
        items(jobs) { jobPost: JobPost ->
            JobItem(jobPost = jobPost, onClick = { onItemClick(jobPost.jobId) })
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .debounce(timeoutMillis = 500L)
            .collect { lastVisibleItemIndex ->
                updateScrollPosition(listState.firstVisibleItemIndex)
                if (lastVisibleItemIndex != null && lastVisibleItemIndex >= jobs.size - 1) {
                    loadMoreData()
                }
            }
    }
}

@Composable
fun JobItem(jobPost: JobPost, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = "Agency: ${jobPost.agency}", style = MaterialTheme.typography.titleMedium)
        Text(text = "Title: ${jobPost.businessTitle}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Career Level: ${jobPost.careerLevel}", style = MaterialTheme.typography.bodySmall)
    }
}
