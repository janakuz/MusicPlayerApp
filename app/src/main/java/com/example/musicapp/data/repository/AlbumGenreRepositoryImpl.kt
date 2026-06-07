package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.AlbumGenreDao
import com.example.musicapp.data.local.dao.GenreDao
import com.example.musicapp.data.local.entity.AlbumGenre
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.util.normalizeGenre

class AlbumGenreRepositoryImpl(
    private val albumGenreDao: AlbumGenreDao,
    private val genreDao: GenreDao,
) : AlbumGenreRepository {

    override suspend fun updateAlbumGenres(albumId: Int, genres: List<String>) {
        val genreIds = genres.map { name ->
            val normalized = name.normalizeGenre()
            genreDao.getGenreByName(normalized)?.id ?: genreDao.insert(Genre(name = normalized))
                .toInt()
        }

        albumGenreDao.syncGenres(albumId, genreIds)
    }

    override suspend fun getAlbumGenres(albumId: Int): List<String> {
        return albumGenreDao.getAlbumGenres(albumId)
    }

    override suspend fun insertAlbumGenres(
        albumId: Int,
        genres: List<String>
    ) {
        val genreIds = genres.map { name ->
            val normalized = name.normalizeGenre()
            genreDao.getGenreByName(normalized)?.id ?: genreDao.insert(Genre(name = normalized))
                .toInt()
        }

        val entries = genreIds.map { AlbumGenre(albumId = albumId, genreId = it) }

        albumGenreDao.insertAll(entries)
    }
}