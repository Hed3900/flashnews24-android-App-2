package com.flashnews24.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Bookmarks : Screen("bookmarks")
    object Search : Screen("search")
    object Detail : Screen("detail/{articleId}") {
        fun createRoute(articleId: String) = "detail/$articleId"
    }
}
