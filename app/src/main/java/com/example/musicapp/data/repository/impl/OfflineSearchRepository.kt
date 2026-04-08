package com.example.musicapp.data.repository.impl

import com.example.musicapp.data.dao.AlbumDao
import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.repository.SearchRepository
import com.example.musicapp.data.repository.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class OfflineSearchRepository(
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val trackDao: TrackDao
) : SearchRepository  {

    override fun globalSearch(userQuery: String): Flow<SearchResult> {
        val final = "%${userQuery.lowercase().replace('*', '%')}%"

        return combine (
            artistDao.searchArtists(final),
            albumDao.searchAlbums(final),
            trackDao.searchTracks(final)) { artists, albums, tracks ->
            SearchResult(artists, albums, tracks)
        }
    }
}