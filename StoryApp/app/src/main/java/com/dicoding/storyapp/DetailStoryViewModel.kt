package com.dicoding.storyapp

import androidx.lifecycle.ViewModel

class DetailStoryViewModel: ViewModel() {
    lateinit var storyItem: ListStoryItem

    fun setDetailStory(story: ListStoryItem) : ListStoryItem { storyItem = story
        return storyItem
    }
}