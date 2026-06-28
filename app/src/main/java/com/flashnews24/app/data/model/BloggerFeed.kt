package com.flashnews24.app.data.model

import com.google.gson.annotations.SerializedName

data class BloggerFeedResponse(
    @SerializedName("feed") val feed: BloggerFeed?
)

data class BloggerFeed(
    @SerializedName("entry") val entries: List<BloggerEntry>?
)

data class BloggerEntry(
    @SerializedName("id") val id: BloggerId?,
    @SerializedName("published") val published: BloggerText?,
    @SerializedName("updated") val updated: BloggerText?,
    @SerializedName("category") val categories: List<BloggerCategory>?,
    @SerializedName("title") val title: BloggerText?,
    @SerializedName("content") val content: BloggerText?,
    @SerializedName("link") val links: List<BloggerLink>?,
    @SerializedName("author") val authors: List<BloggerAuthor>?,
    @SerializedName("media\$thumbnail") val thumbnail: BloggerThumbnail?
) {
    val cleanId: String
        get() = id?.text?.substringAfterLast("post-") ?: id?.text ?: ""

    val categoryList: List<String>
        get() = categories?.mapNotNull { it.term } ?: emptyList()

    val alternateLink: String
        get() = links?.find { it.rel == "alternate" }?.href ?: ""

    val authorName: String
        get() = authors?.firstOrNull()?.name?.text ?: "FlashNews24"

    val imageUrl: String
        get() {
            val thumbUrl = thumbnail?.url
            if (thumbUrl != null) {
                // Return high quality version of the blogger thumbnail
                return thumbUrl.replace("/s72-c", "/s1600").replace("/s72-w640", "/s1600")
            }
            val contentHtml = content?.text ?: ""
            val regex = "<img[^>]+src=\"([^\"]+)\"".toRegex()
            val match = regex.find(contentHtml)
            return match?.groups?.get(1)?.value ?: ""
        }
}

data class BloggerId(
    @SerializedName("\$t") val text: String?
)

data class BloggerText(
    @SerializedName("\$t") val text: String?
)

data class BloggerCategory(
    @SerializedName("term") val term: String?
)

data class BloggerLink(
    @SerializedName("rel") val rel: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("href") val href: String?
)

data class BloggerAuthor(
    @SerializedName("name") val name: BloggerText?
)

data class BloggerThumbnail(
    @SerializedName("url") val url: String?,
    @SerializedName("width") val width: String?,
    @SerializedName("height") val height: String?
)
