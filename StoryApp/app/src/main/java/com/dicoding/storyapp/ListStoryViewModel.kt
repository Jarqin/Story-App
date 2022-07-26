package com.dicoding.storyapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn

class ListStoryViewModel(
    private val storyRepository: StoryRepository, ) : ViewModel() {

    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        return storyRepository.getPagingStories(token).cachedIn(viewModelScope).asLiveData()
    }
}
