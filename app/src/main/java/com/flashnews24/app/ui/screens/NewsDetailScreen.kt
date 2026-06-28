package com.flashnews24.app.ui.screens

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.flashnews24.app.ui.NewsViewModel
import com.flashnews24.app.ui.components.AdaptiveBannerAd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    articleId: String,
    viewModel: NewsViewModel,
    onBack: () -> Unit
) {
    val article = remember(articleId) { viewModel.getArticleById(articleId) }
    val isBookmarked by viewModel.isBookmarkedFlow(articleId).collectAsState(initial = false)
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    if (article == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Article Detail") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Article not found or loading...")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, article.title?.text ?: "")
                            putExtra(Intent.EXTRA_TEXT, "${article.title?.text}\n\nRead more at: ${article.alternateLink}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = { viewModel.toggleBookmark(article) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                .verticalScroll(rememberScrollState())
        ) {
            val imageUrl = article.imageUrl
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (article.categoryList.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        article.categoryList.forEach { category ->
                            Text(
                                text = category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text(
                    text = article.title?.text ?: "No Title",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By ${article.authorName} • ${article.published?.text?.substringBefore("T") ?: ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val backgroundColorHtml = if (isDark) "#0F172A" else "#F9FAFB"
                val textColorHtml = if (isDark) "#F9FAFB" else "#111827"
                val linkColorHtml = if (isDark) "#38BDF8" else "#1D2939"

                val styledHtml = """
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            background-color: $backgroundColorHtml;
                            color: $textColorHtml;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                            font-size: 16px;
                            line-height: 1.6;
                            margin: 0;
                            padding: 0;
                        }
                        img {
                            max-width: 100% !important;
                            height: auto !important;
                            border-radius: 8px;
                            margin-top: 12px;
                            margin-bottom: 12px;
                        }
                        a {
                            color: $linkColorHtml;
                            text-decoration: none;
                        }
                        iframe {
                            max-width: 100% !important;
                            height: auto !important;
                            border-radius: 8px;
                        }
                        p {
                            margin-bottom: 16px;
                        }
                    </style>
                    </head>
                    <body>
                        ${article.content?.text ?: ""}
                    </body>
                    </html>
                """.trimIndent()

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            setBackgroundColor(0)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 5000.dp)
                )
            }
        }
    }
}
