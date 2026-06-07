package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.AlbumGenreDao
import com.example.musicapp.data.local.dao.ArtistGenreDao
import com.example.musicapp.data.local.dao.GenreDao
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.util.normalizeGenre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GenreRepositoryImpl(
    private val genreDao: GenreDao,
    private val albumGenreDao: AlbumGenreDao,
    private val artistGenreDao: ArtistGenreDao
) : GenreRepository {
    override suspend fun getOrCreateGenre(name: String): Int {
        val normalized = name.normalizeGenre()
        val existingGenre = genreDao.getGenreByName(normalized)
        return existingGenre?.id ?: genreDao.insert(Genre(name = normalized)).toInt()
    }

    override fun findGenre(query: String): Flow<List<String>> {
        return genreDao.findGenre(query.normalizeGenre())
    }

    override fun getAll(): Flow<List<GenreInfo>> {
        return genreDao.getAllGenres()
    }

    override fun getGenreArtistsAndAlbums(genreId: Int): Flow<SearchResult> {
        return combine(
            artistGenreDao.getGenreArtists(genreId),
            albumGenreDao.getGenreAlbums(genreId)
        ) {
            artists, albums ->
            SearchResult(artists, albums)
        }
    }

    override fun getGenreName(genreId: Int): Flow<String> {
        return genreDao.getGenreName(genreId)
    }
}