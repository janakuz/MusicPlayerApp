package com.example.musicapp.data.repository

import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow

interface SmartPlaylistRepository {

    suspend fun savePlaylist(filter: LibraryFilter, name: String)

    fun getSmartPlaylist(playlistId: Int): Flow<List<TrackInfo>>
}