package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.entity.Artist
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun globalSearch(userQuery: String): Flow<SearchResult>
}

data class SearchResult(
    val artists: List<Artist> = emptyList<Artist>(),
    val albums: List<AlbumInfo> = emptyList<AlbumInfo>(),
    val tracks: List<TrackInfo> = emptyList<TrackInfo>()
)