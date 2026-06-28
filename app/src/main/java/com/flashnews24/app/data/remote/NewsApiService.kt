package com.flashnews24.app.data.remote

import com.flashnews24.app.data.model.BloggerFeedResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("feeds/posts/default")
    suspend fun getNewsFeed(
        @Query("alt") alt: String = "json",
        @Query("max-results") maxResults: Int = 100
    ): BloggerFeedResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://www.flashnews24.site/"

    val apiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}
