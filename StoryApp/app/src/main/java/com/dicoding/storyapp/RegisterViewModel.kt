package com.dicoding.storyapp

import androidx.lifecycle.ViewModel

class RegisterViewModel(private val storyRepository: StoryRepository) : ViewModel() {

  fun register(name: String, email: String, pass: String) = storyRepository.register(name,email, pass)

}