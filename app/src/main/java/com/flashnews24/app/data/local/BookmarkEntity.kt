package com.flashnews24.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val published: String,
    val author: String,
    val imageUrl: String,
    val link: String,
    val categories: String // comma separated list
)
