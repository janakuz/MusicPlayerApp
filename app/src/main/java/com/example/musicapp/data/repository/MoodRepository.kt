package com.example.musicapp.data.repository

import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    suspend fun getOrCreateMood(name: String): Int

    fun findMood(query: String): Flow<List<String>>

}