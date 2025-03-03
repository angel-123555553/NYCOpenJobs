package com.example.nycopenjobs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nycopenjobs.NYCOpenJobsApp
import com.example.nycopenjobs.model.JobPost
import com.example.nycopenjobs.util.LoadingSpinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    jobId: Int,
    onBack: () -> Unit,
    onToggleFavorite: suspend (jobId: Int, currentFavorite: Boolean) -> Unit
) {
    var job by remember { mutableStateOf<JobPost?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fetch the job in a background coroutine
    LaunchedEffect(jobId) {
        val repository = (context.applicationContext as NYCOpenJobsApp).container.appRepository
        job = repository.getJobPost(jobId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(job?.agency ?: "", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        job?.let { jp ->
                            scope.launch(Dispatchers.IO) {
                                onToggleFavorite(jp.jobId, jp.isFavorite)
                                // Refresh job after toggling favorite
                                val repository = (context.applicationContext as NYCOpenJobsApp).container.appRepository
                                job = repository.getJobPost(jobId)
                            }
                        }
                    }) {
                        val icon = if (job?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                        Icon(icon, contentDescription = "Toggle Favorite")
                    }
                }
            )
        }
    ) { paddingValues ->
        job?.let { jp ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Job ID: ${jp.jobId}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Business Title: ${jp.businessTitle}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Career Level: ${jp.careerLevel}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Salary: ${jp.salaryRangeFrom} to ${jp.salaryRangeTo} (${jp.salaryFrequency})",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Job Category: ${jp.jobCategory}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Work Location: ${jp.workLocation}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Division: ${jp.divisionWorkUnit}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Description:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(jp.jobDescription, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (jp.minRequirement.isNotEmpty()) {
                    Text("Minimum Qualification Requirements:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(jp.minRequirement, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (jp.preferredSkills.isNotEmpty()) {
                    Text("Preferred Skills:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(jp.preferredSkills, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (jp.additionalInfo.isNotEmpty()) {
                    Text("Additional Information:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(jp.additionalInfo, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (jp.toApply.isNotEmpty()) {
                    Text("How to Apply:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(jp.toApply, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } ?: LoadingSpinner()
    }
}
