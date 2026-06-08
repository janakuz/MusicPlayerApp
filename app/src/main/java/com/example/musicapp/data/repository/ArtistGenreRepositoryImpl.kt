package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.ArtistGenreDao
import com.example.musicapp.data.local.dao.GenreDao
import com.example.musicapp.data.local.entity.ArtistGenre
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.util.normalizeGenre

class ArtistGenreRepositoryImpl(
    private val artistGenreDao : ArtistGenreDao,
    private val genreDao: GenreDao
) : ArtistGenreRepository {
    override suspend fun updateArtistGenres(
        artistId: Int,
        genres: List<String>
    ) {
        val genreIds = genres.map { name ->
            val normalized = name.normalizeGenre()
            genreDao.getGenreByName(normalized)?.id ?: genreDao.insert(Genre(name = normalized))
                .toInt()
        }

        artistGenreDao.syncGenres(artistId, genreIds)

    }

    override suspend fun getArtistGenres(artistId: Int): List<String> {
        return artistGenreDao.getArtistGenres(artistId)
    }

    override suspend fun insertArtistGenres(
        artistId: Int,
        genres: List<String>
    ) {
        val genreIds = genres.map { name ->
            val normalized = name.normalizeGenre()
            genreDao.getGenreByName(normalized)?.id ?: genreDao.insert(Genre(name = normalized))
                .toInt()
        }

        val entries = genreIds.map { ArtistGenre(artistId = artistId, genreId = it) }

        artistGenreDao.insertAll(entries)
    }
}