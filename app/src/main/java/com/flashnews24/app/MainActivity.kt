package com.flashnews24.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flashnews24.app.ui.theme.FlashNews24Theme
import com.flashnews24.app.ui.NewsViewModel
import com.flashnews24.app.ui.components.InterstitialAdHelper
import com.flashnews24.app.ui.navigation.Screen
import com.flashnews24.app.ui.screens.BookmarksScreen
import com.flashnews24.app.ui.screens.HomeScreen
import com.flashnews24.app.ui.screens.NewsDetailScreen
import com.flashnews24.app.ui.screens.SearchScreen
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}
        InterstitialAdHelper.loadAd(this)

        askNotificationPermission()

        setContent {
            FlashNews24Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: NewsViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetail = { articleId ->
                    navController.navigate(Screen.Detail.createRoute(articleId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToBookmarks = {
                    navController.navigate(Screen.Bookmarks.route)
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = viewModel,
                onNavigateToDetail = { articleId ->
                    navController.navigate(Screen.Detail.createRoute(articleId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                viewModel = viewModel,
                onNavigateToDetail = { articleId ->
                    navController.navigate(Screen.Detail.createRoute(articleId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            NewsDetailScreen(
                articleId = articleId,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
