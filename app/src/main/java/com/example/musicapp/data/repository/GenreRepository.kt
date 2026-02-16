package com.example.musicapp.data.repository

import com.example.musicapp.data.entity.Genre
import kotlinx.coroutines.flow.Flow

interface GenreRepository {

    suspend fun getOrCreateGenre(name: String) : Int

    fun findGenre(query: String) : Flow<List<String>>
}