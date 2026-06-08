package com.example.musicapp.data.repository

import android.util.Log
import com.example.musicapp.data.local.dao.AlbumGenreDao
import com.example.musicapp.data.local.dao.ArtistGenreDao
import com.example.musicapp.data.local.dao.GenreDao
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
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

    override fun getAll(sortBy: SortOption): Flow<List<GenreInfo>> {
        Log.d("genre sort", sortBy.field.name)
        return when (sortBy.field) {
            SortField.NAME -> genreDao.getAllGenres("name", sortBy.ascending)
            SortField.TOTAL_COUNT -> genreDao.getAllGenres("total", sortBy.ascending)
            SortField.ARTIST_COUNT -> genreDao.getAllGenres("artistCount", sortBy.ascending)
            SortField.ALBUM_COUNT -> genreDao.getAllGenres("albumCount", sortBy.ascending)
            else -> genreDao.getAllGenres("total", false)
        }
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