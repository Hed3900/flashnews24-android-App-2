package com.flashnews24.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flashnews24.app.data.model.*
import com.flashnews24.app.ui.NewsViewModel
import com.flashnews24.app.ui.components.AdaptiveBannerAd
import com.flashnews24.app.ui.components.InterstitialAdHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: NewsViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Bookmarks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AdaptiveBannerAd()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (bookmarks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "You don't have any bookmarks yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookmarks) { bookmark ->
                        val entry = remember(bookmark) {
                            BloggerEntry(
                                id = BloggerId("tag:blogger.com,1999:blog-post-${bookmark.id}"),
                                published = BloggerText(bookmark.published),
                                updated = BloggerText(bookmark.published),
                                categories = bookmark.categories.split(",").filter { it.isNotEmpty() }.map { BloggerCategory(it) },
                                title = BloggerText(bookmark.title),
                                content = BloggerText(bookmark.content),
                                links = listOf(BloggerLink("alternate", "text/html", bookmark.link)),
                                authors = listOf(BloggerAuthor(BloggerText(bookmark.author))),
                                thumbnail = BloggerThumbnail(bookmark.imageUrl, "72", "72")
                            )
                        }

                        NewsItemCard(
                            article = entry,
                            isBookmarked = true,
                            onBookmarkToggle = { viewModel.toggleBookmark(entry) },
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    InterstitialAdHelper.showAd(activity) {
                                        onNavigateToDetail(bookmark.id)
                                    }
                                } else {
                                    onNavigateToDetail(bookmark.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
