package com.flashnews24.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flashnews24.app.data.local.ArticleDatabase
import com.flashnews24.app.data.local.BookmarkEntity
import com.flashnews24.app.data.model.*
import com.flashnews24.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ArticleDatabase.getDatabase(application)
    private val dao = db.articleDao()

    private val _articles = MutableStateFlow<List<BloggerEntry>>(emptyList())
    val articles: StateFlow<List<BloggerEntry>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = _articles.map { entries ->
        entries.flatMap { it.categoryList }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredArticles: StateFlow<List<BloggerEntry>> = combine(
        _articles, _selectedCategory, _searchQuery
    ) { entries, category, query ->
        var list = entries
        if (!category.isNullOrEmpty()) {
            list = list.filter { it.categoryList.contains(category) }
        }
        if (query.isNotEmpty()) {
            list = list.filter {
                (it.title?.text?.contains(query, ignoreCase = true) == true) ||
                (it.content?.text?.contains(query, ignoreCase = true) == true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.apiService.getNewsFeed()
                val entries = response.feed?.entries ?: emptyList()
                _articles.value = entries
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to fetch news feed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(entry: BloggerEntry) {
        viewModelScope.launch {
            val isBookmarked = dao.isBookmarked(entry.cleanId)
            if (isBookmarked) {
                dao.deleteBookmarkById(entry.cleanId)
            } else {
                dao.insertBookmark(
                    BookmarkEntity(
                        id = entry.cleanId,
                        title = entry.title?.text ?: "",
                        content = entry.content?.text ?: "",
                        published = entry.published?.text ?: "",
                        author = entry.authorName,
                        imageUrl = entry.imageUrl,
                        link = entry.alternateLink,
                        categories = entry.categoryList.joinToString(",")
                    )
                )
            }
        }
    }

    fun isBookmarkedFlow(articleId: String): Flow<Boolean> {
        return dao.isBookmarkedFlow(articleId)
    }

    fun getArticleById(articleId: String): BloggerEntry? {
        val remoteArticle = _articles.value.find { it.cleanId == articleId }
        if (remoteArticle != null) return remoteArticle

        val bookmark = bookmarks.value.find { it.id == articleId }
        return bookmark?.let {
            BloggerEntry(
                id = BloggerId("tag:blogger.com,1999:blog-post-${it.id}"),
                published = BloggerText(it.published),
                updated = BloggerText(it.published),
                categories = it.categories.split(",").filter { c -> c.isNotEmpty() }.map { c -> BloggerCategory(c) },
                title = BloggerText(it.title),
                content = BloggerText(it.content),
                links = listOf(BloggerLink("alternate", "text/html", it.link)),
                authors = listOf(BloggerAuthor(BloggerText(it.author))),
                thumbnail = BloggerThumbnail(it.imageUrl, "72", "72")
            )
        }
    }
}
