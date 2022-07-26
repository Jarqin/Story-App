package com.dicoding.storyapp

import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.databinding.ActivityDetailStoryBinding
import java.util.*

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var stories: ListStoryItem
    private lateinit var binding: ActivityDetailStoryBinding
    private val vm: DetailStoryViewModel by viewModels()

    companion object {
        const val STORY_EXTRA = "story"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            run {
                binding.tvDescription.justificationMode = JUSTIFICATION_MODE_INTER_WORD
            }
        }

        stories = intent.getParcelableExtra(STORY_EXTRA)!!
        vm.setDetailStory(stories)
        displayResult()
        setupToolbar()
    }

    private fun setupToolbar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayResult() {
        with(binding){
            Glide.with(tvPhoto)
                .load(vm.storyItem.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(tvPhoto)
            tvName.text = vm.storyItem.name
            tvCreatedTime.text = getString(R.string.created_add, Helper.formatDate(vm.storyItem.createdAt,
                TimeZone.getDefault().id ))
            tvDescription.text = vm.storyItem.description
        }
    }
}