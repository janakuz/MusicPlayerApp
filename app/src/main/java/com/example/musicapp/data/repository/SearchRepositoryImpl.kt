package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.AlbumDao
import com.example.musicapp.data.local.dao.ArtistDao
import com.example.musicapp.data.local.dao.TrackDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SearchRepositoryImpl(
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val trackDao: TrackDao
) : SearchRepository  {

    override fun globalSearch(userQuery: String): Flow<SearchResult> {
        val final = "%${userQuery.lowercase().replace('*', '%')}%"

        return combine(
            artistDao.searchArtists(final),
            albumDao.searchAlbums(final),
            trackDao.searchTracks(final)
        ) { artists, albums, tracks ->
            SearchResult(artists, albums, tracks)
        }
    }

    override fun searchWithinArtist(userQuery: String, artistId: Int): Flow<SearchResult> {
        val final = "%${userQuery.lowercase().replace('*', '%')}%"

        return combine(
            albumDao.searchArtistAlbums(final, artistId),
            trackDao.searchArtistTracks(final, artistId)
        ) { albums, tracks ->
            SearchResult(albums = albums, tracks = tracks)
        }
    }

    override fun searchWithinAlbum(userQuery: String, albumId: Int): Flow<SearchResult> {
        val final = "%${userQuery.lowercase().replace('*', '%')}%"

        return trackDao.searchAlbumTracks(final, albumId).map { trackResults ->
            SearchResult(tracks = trackResults)
        }
    }
}