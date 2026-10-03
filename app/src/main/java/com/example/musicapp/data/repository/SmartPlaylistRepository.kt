package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.SmartPlaylist
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow

interface SmartPlaylistRepository {

    suspend fun savePlaylist(filter: LibraryFilter, name: String)

    fun getSmartPlaylistTracks(filterJson: String): Flow<List<TrackInfo>>

    fun getAll(): Flow<List<SmartPlaylist>>

    fun getSmartPlaylist(playlistId: Int): Flow<SmartPlaylist>
}