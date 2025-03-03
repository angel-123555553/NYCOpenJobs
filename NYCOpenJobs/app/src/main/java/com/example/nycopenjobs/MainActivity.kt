package com.example.nycopenjobs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nycopenjobs.ui.screens.DetailScreen
import com.example.nycopenjobs.ui.screens.HomeScreen
import com.example.nycopenjobs.ui.screens.HomeScreenViewModel
import com.example.nycopenjobs.ui.theme.NYCOpenJobsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NYCOpenJobsTheme {
                val navController = rememberNavController()
                val viewModel: HomeScreenViewModel by viewModels { HomeScreenViewModel.Factory }

                Surface {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onItemClick = { jobId ->
                                    navController.navigate("detail/$jobId")
                                }
                            )
                        }

                        composable(
                            route = "detail/{jobId}",
                            arguments = listOf(
                                navArgument("jobId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getInt("jobId") ?: 0
                            DetailScreen(
                                jobId = jobId,
                                onBack = { navController.popBackStack() },
                                onToggleFavorite = { jobId, currentFavorite ->
                                    viewModel.viewModelScope.launch {
                                        viewModel.toggleFavorite(jobId, currentFavorite)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NYCOpenJobsTheme {
        Greeting("Android")
    }
}
