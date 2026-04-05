package com.example.musicapp.data.repository.impl

import com.example.musicapp.data.dao.AlbumGenreDao
import com.example.musicapp.data.dao.GenreDao
import com.example.musicapp.data.entity.Genre
import com.example.musicapp.data.repository.AlbumGenreRepository
import com.example.musicapp.normalizeGenre

class OfflineAlbumGenreRepository(
    private val albumGenreDao: AlbumGenreDao,
    private val genreDao: GenreDao,
) : AlbumGenreRepository {

    override suspend fun updateAlbumGenres(albumId: Int, genres: List<String>) {
        val genreIds = genres.map { name ->
            val normalized = name.normalizeGenre()
            genreDao.getGenreByName(normalized)?.id ?:
            genreDao.insert(Genre(name = normalized)).toInt()
        }

        albumGenreDao.syncGenres(albumId, genreIds)
    }

    override suspend fun getAlbumGenres(albumId: Int) : List<String> {
        return albumGenreDao.getAlbumGenres(albumId)
    }
}