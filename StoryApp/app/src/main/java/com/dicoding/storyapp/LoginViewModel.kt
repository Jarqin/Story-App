package com.dicoding.storyapp

import androidx.lifecycle.ViewModel

class LoginViewModel(private val storyRepository: StoryRepository): ViewModel()  {

    fun login(email: String, pass: String) = storyRepository.login(email, pass)

}