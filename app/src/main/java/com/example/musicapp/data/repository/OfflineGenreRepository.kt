package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.GenreDao
import com.example.musicapp.data.entity.Genre
import com.example.musicapp.normalizeGenre
import kotlinx.coroutines.flow.Flow

class OfflineGenreRepository(
    private val genreDao: GenreDao
) : GenreRepository {
    override suspend fun getOrCreateGenre(name: String) : Int {
        val normalized = name.normalizeGenre()
        val existingGenre = genreDao.getGenreByName(normalized)
        return existingGenre?.id ?: genreDao.insert(Genre(name = normalized)).toInt()
    }

    override fun findGenre(query: String): Flow<List<String>> {
        return genreDao.findGenre(query.normalizeGenre())
    }
}