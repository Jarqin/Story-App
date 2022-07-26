package com.dicoding.storyapp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference(private val dataStore: DataStore<Preferences>) {

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val NAME = stringPreferencesKey("name")
        private val EMAIL = stringPreferencesKey("email")
        private val PASSWORD = stringPreferencesKey("password")
        private val USERID = stringPreferencesKey("userId")
        private val TOKEN = stringPreferencesKey("token")
        private val STATE = booleanPreferencesKey("state")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

    fun getUser(): Flow<UserModel> {
        return dataStore.data.map {
            UserModel(
            it[NAME] ?: "",
            it[EMAIL] ?: "",
            it[PASSWORD] ?: "",
            it[USERID] ?: "",
            it[TOKEN] ?: "",
            it[STATE] ?: false
            )
        }
    }

    suspend fun saveUser(user: UserModel) {
        dataStore.edit {
            it[NAME] = user.name
            it[EMAIL] = user.email
            it[PASSWORD] = user.password
            it[USERID] = user.userId
            it[TOKEN] = user.token
            it[STATE] = user.isLogin
        }
    }

    suspend fun logout() {
        dataStore.edit {
            it[STATE] = false
            it[NAME] = ""
            it[EMAIL] = ""
            it[USERID] = ""
            it[TOKEN] = ""
            it[PASSWORD] = ""
        }
    }
}