package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun globalSearch(userQuery: String): Flow<SearchResult>

    fun searchWithinArtist(userQuery: String, artistId: Int): Flow<SearchResult>

    fun searchWithinAlbum(userQuery: String, albumId: Int): Flow<SearchResult>
}

data class SearchResult(
    val artists: List<Artist> = emptyList<Artist>(),
    val albums: List<AlbumInfo> = emptyList<AlbumInfo>(),
    val tracks: List<TrackInfo> = emptyList<TrackInfo>()
)