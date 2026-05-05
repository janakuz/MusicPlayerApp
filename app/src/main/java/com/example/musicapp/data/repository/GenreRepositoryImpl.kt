package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.GenreDao
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.util.normalizeGenre
import kotlinx.coroutines.flow.Flow

class GenreRepositoryImpl(
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