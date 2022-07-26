package com.dicoding.storyapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(private val storyDatabase: StoryDatabase, private val apiService: ApiService) {

    companion object {
        private const val TAG = "StoryRepository"
    }

    fun register(name: String, email: String, pass: String): LiveData<ResultResponse<ApiResponse>> = liveData {
        emit(ResultResponse.Loading)
        try {
            val respond = apiService.register(name, email, pass)
            if (!respond.error) {
                emit(ResultResponse.Success(respond))
            }
            else {
                Log.e(TAG, "Register Fail: ${respond.message}")
                emit(ResultResponse.Error(respond.message))
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Register Exception: ${e.message.toString()} ")
            emit(ResultResponse.Error(e.message.toString()))
        }
    }

    fun login(email: String, password: String): LiveData<ResultResponse<LoginResult>> = liveData {
        emit(ResultResponse.Loading)
        try {
            val respond = apiService.login(email, password)
            if (!respond.error) {
                emit(ResultResponse.Success(respond.loginResult))
            }
            else {
                Log.e(TAG, "Register Fail: ${respond.message}")
                emit(ResultResponse.Error(respond.message))
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Register Exception: ${e.message.toString()} ")
            emit(ResultResponse.Error(e.message.toString()))
        }
    }

    fun getStoryMap(token: String): LiveData<ResultResponse<List<ListStoryItem>>> = liveData {
        emit(ResultResponse.Loading)
        try {
            val respond = apiService.getAllStoriesLocation("Bearer $token")
            if (!respond.error) {
                emit(ResultResponse.Success(respond.listStory))
            }
            else {
                Log.e(TAG, "GetStoryMap Fail: ${respond.message}")
                emit(ResultResponse.Error(respond.message))
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "GetStoryMap Exception: ${e.message.toString()} ")
            emit(ResultResponse.Error(e.message.toString()))
        }
    }

    fun postStory(token: String, description: RequestBody, imageMultipart: MultipartBody.Part, lat: RequestBody? = null, lon: RequestBody? = null): LiveData<ResultResponse<ApiResponse>> = liveData {
        emit(ResultResponse.Loading)
        try {
            val respond = apiService.addStories("Bearer $token", description, imageMultipart, lat, lon)
            if (!respond.error) {
                emit(ResultResponse.Success(respond))
            }
            else {
                Log.e(TAG, "PostStory Fail: ${respond.message}")
                emit(ResultResponse.Error(respond.message))
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "PostStory Exception: ${e.message.toString()} ")
            emit(ResultResponse.Error(e.message.toString()))
        }
    }

    fun getPagingStories(token: String): Flow<PagingData<ListStoryItem>> {
        wrapEspressoIdlingResource {
            @OptIn(ExperimentalPagingApi::class)
            return Pager(config = PagingConfig(pageSize = 5),
                remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getStory()
                }
            ).flow
        }
    }
}