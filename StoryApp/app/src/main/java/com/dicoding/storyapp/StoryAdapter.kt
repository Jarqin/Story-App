package com.dicoding.storyapp

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.databinding.ItemListStoryBinding
import androidx.core.util.Pair
import androidx.paging.PagingData
import java.util.*
import kotlin.collections.ArrayList

class StoryAdapter : PagingDataAdapter<ListStoryItem, StoryAdapter.ViewHolder>(CALLBACK) {

    private val list = ArrayList<ListStoryItem>()

    companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    fun setList(users: List<ListStoryItem>){
        list.clear()
        list.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
              holder.bind(data)
        }
    }

    inner class ViewHolder(private var binding: ItemListStoryBinding) :

    RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            with(binding) {
                Glide.with(tvPhoto)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(tvPhoto)

                tvName.text = story.name
                tvDeskripsi.text = story.description
                tvCreatedAt.text = binding.root.resources.getString(R.string.created_add, Helper.formatDate(story.createdAt, TimeZone.getDefault().id))
                tvPhoto.setOnClickListener {
                    val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                    itemView.context as Activity,
                    Pair(tvPhoto, "image"),
                    Pair(tvName, "name"),
                    Pair(tvDeskripsi, "description"),
                    Pair(tvCreatedAt, "created"),)

                    val intent = Intent(it.context, DetailStoryActivity::class.java)
                    intent.putExtra(DetailStoryActivity.STORY_EXTRA, story)
                    it.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }
}