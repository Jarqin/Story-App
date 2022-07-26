package com.dicoding.storyapp

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") pass: String
    ): ApiResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") pass: String
    ): LoginResponse

    @Multipart
    @POST("stories")
    suspend fun addStories(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("lat") latitude: RequestBody?,
        @Part("lon") longitude: RequestBody?
    ): ApiResponse

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): AllStoriesResponse

    @GET("stories?location=1")
    suspend fun getAllStoriesLocation(
        @Header("Authorization") token: String
    ): AllStoriesResponse
}
