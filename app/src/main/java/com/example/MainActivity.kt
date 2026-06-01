package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.repository.ProfileRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.ProfileViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Let system draw background overlays natively
        enableEdgeToEdge()

        // Core Local Storage setup
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ProfileRepository(database.profileReviewDao())

        // ViewModel compilation
        val viewModel: ProfileViewModel by viewModels {
            ProfileViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                MainAppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppNavigation(viewModel: ProfileViewModel) {
    val navController = rememberNavController()

    // Determine initial route based on active sign-in
    val initialRoute = if (viewModel.isUserSignedIn.value) "dashboard" else "welcome"

    NavHost(
        navController = navController,
        startDestination = initialRoute,
        modifier = Modifier.fillMaxSize()
    ) {
        composable("welcome") {
            WelcomeScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToNewReview = { navController.navigate("new_review") },
                onNavigateToReviewDetail = { navController.navigate("analysis_result") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("new_review") {
            NewReviewScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate("analysis_result") }
            )
        }

        composable("analysis_result") {
            AnalysisResultScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigate("dashboard") {
                    popUpTo("dashboard") { inclusive = true }
                } },
                onNavigateToOptimized = { navController.navigate("optimized_content") }
            )
        }

        composable("optimized_content") {
            OptimizedContentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate("analysis_result") }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("welcome") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}
