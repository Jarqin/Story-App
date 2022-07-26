package com.dicoding.storyapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class RoutingActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) { installSplashScreen()
        super.onCreate(savedInstanceState)
        setupView()
    }

    private fun setMainActivity(boolean: Boolean){
        if (boolean) {
            startActivity(Intent(this, MainActivity::class.java), ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle())
            finish()
        }
        else {
            startActivity(Intent(this, WelcomeActivity::class.java), ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle())
            finish()
        }
    }

    private fun setupView() {
        mainViewModel = ViewModelProvider(this, ViewModelUserFactory(UserPreference.getInstance(dataStore)))[MainViewModel::class.java]

        lifecycleScope.launchWhenCreated {
            launch {
                mainViewModel.getUser().collect {
                    if(it.isLogin){
                        setMainActivity(true)
                    }
                    else setMainActivity(false)
                }
            }
        }
    }
}