package com.dicoding.storyapp

import com.google.gson.annotations.SerializedName

data class ApiResponse(

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("message")
	val message: String
)
