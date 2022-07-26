package com.dicoding.storyapp

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class MainActivity : AppCompatActivity() {
    private lateinit var user: UserModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()
        viewModel()
        setButtonListener()
        supportActionBar?.hide()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -38f, 38f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

    private fun setButtonListener() {
        binding.btnListStory.setOnClickListener {
            val moveToListStoryActivity = Intent(this@MainActivity, ListStoryActivity::class.java)
            moveToListStoryActivity.putExtra(ListStoryActivity.USER_EXTRA, user)
            startActivity(moveToListStoryActivity)
        }
        binding.ivSetting.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
        binding.logoutButton.setOnClickListener {
            mainViewModel.logout()
            AlertDialog.Builder(this).apply {
                setTitle("Yeah!")
                setMessage("Yakin Keluar?")
                setPositiveButton("Lanjut") { _, _ ->
                    startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                    finish()
                }
                create()
                show()
            }
        }
    }

    private fun viewModel() {
        mainViewModel = ViewModelProvider(this, ViewModelUserFactory(UserPreference.getInstance(dataStore)))[MainViewModel::class.java]

        lifecycleScope.launchWhenCreated {
            launch {
                mainViewModel.getUser().collect {
                    user = UserModel(
                        it.name,
                        it.email,
                        it.password,
                        it.userId,
                        it.token,
                        true
                    )
                    binding.nameTextView.text = getString(R.string.greeting, user.name)
                }
            }
        }
    }
}