package com.dicoding.storyapp

import android.content.Context

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getInstance(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }
}