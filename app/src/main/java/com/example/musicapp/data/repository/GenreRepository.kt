package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface GenreRepository {

    suspend fun getOrCreateGenre(name: String): Int

    fun findGenre(query: String): Flow<List<String>>

    fun getAll(sortBy: SortOption): Flow<List<GenreInfo>>

    fun getGenreArtistsAndAlbums(genreId: Int): Flow<SearchResult>

    fun getGenreName(genreId: Int): Flow<String>
}