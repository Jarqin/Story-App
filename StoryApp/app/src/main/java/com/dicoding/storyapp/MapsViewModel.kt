package com.dicoding.storyapp

import androidx.lifecycle.ViewModel

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {

  fun getStories(token: String) = storyRepository.getStoryMap(token)

}