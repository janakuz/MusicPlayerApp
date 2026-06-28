package com.example.musicapp.data.repository

import com.example.musicapp.data.local.model.MoodInfo
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    suspend fun getOrCreateMood(name: String): Int

    fun findMood(query: String): Flow<List<String>>

    fun getAll(sortBy: SortOption): Flow<List<MoodInfo>>

    fun getItemsForMood(moodId: Int, artistLimit: Int, albumThreshold: Float): Flow<SearchResult>

    fun getMoodName(moodId: Int): Flow<String>
}