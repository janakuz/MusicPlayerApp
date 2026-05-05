package com.example.musicapp.data.repository

import kotlinx.coroutines.flow.Flow

interface GenreRepository {

    suspend fun getOrCreateGenre(name: String) : Int

    fun findGenre(query: String) : Flow<List<String>>
}