package com.dicoding.storyapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ScrollView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.databinding.ActivityListStoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ListStoryActivity : AppCompatActivity() {

    private var _binding: ActivityListStoryBinding? = null
    private val binding get() = _binding
    private lateinit var user: UserModel
    private lateinit var adapter: StoryAdapter
    private val viewModel: ListStoryViewModel by viewModels { ViewModelFactory.getInstance(this) }

    companion object {
        const val USER_EXTRA = "user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        user = intent.getParcelableExtra(USER_EXTRA)!!

        setAdapter()
        setRefresh()
        setToolbar()
        setButtonListener()
        val handler = Looper.myLooper()?.let { Handler(it) }
        handler?.postDelayed({
            binding?.refresh?.setOnRefreshListener{ adapter.refresh() }
        }, 100)
        binding?.rvStory?.post { // Call smooth scroll
            binding?.rvStory?.smoothScrollToPosition(0)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.refresh()
        AlertDialog.Builder(this).apply {
            setTitle("Yeah!")
            setMessage("Selamat Datang di Story Dicoding, Ingin Lanjut?")
            setPositiveButton("Lanjut") { _, _ ->
                binding?.rvStory?.post { // Call smooth scroll
                    binding?.rvStory?.smoothScrollToPosition(0)
                }
            }
            create()
            show()
        }
    }

    private fun setRefresh() {
        binding?.refresh?.setOnRefreshListener{ adapter.refresh() }
    }

    private fun setButtonListener() {
        binding?.btnAddStory?.setOnClickListener {
            val moveToAddStoryActivity = Intent(this, AddStoryActivity::class.java)
            moveToAddStoryActivity.putExtra(AddStoryActivity.USER_EXTRA, user)
            startActivity(moveToAddStoryActivity)
        }
        binding?.btnToMap?.setOnClickListener {
            val moveToMapStory = Intent(this, MapsActivity::class.java)
            moveToMapStory.putExtra(AddStoryActivity.USER_EXTRA, user)
            startActivity(moveToMapStory)
        }
    }

    private fun setAdapter() {
        adapter = StoryAdapter()
        binding?.rvStory?.adapter = adapter.withLoadStateHeaderAndFooter(
            footer = LoadingStateAdapter{ adapter.retry() },
            header = LoadingStateAdapter { adapter.retry() }
        )
        binding?.rvStory?.layoutManager = LinearLayoutManager(this)
        binding?.rvStory?.setHasFixedSize(true)

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collect {
                binding?.refresh?.isRefreshing = it.mediator?.refresh is LoadState.Loading
            }
        }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                binding?.viewError?.root?.isVisible = loadStates.refresh is LoadState.Error
            }
            if (adapter.itemCount < 1) binding?.viewError?.root?.visibility = View.VISIBLE
            else binding?.viewError?.root?.visibility = View.GONE
        }

        viewModel.getStory(user.token).observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun setToolbar() {
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}