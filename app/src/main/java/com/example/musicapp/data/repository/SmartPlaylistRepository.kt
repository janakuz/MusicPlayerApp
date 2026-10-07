package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.SmartPlaylist
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow

interface SmartPlaylistRepository {

    suspend fun savePlaylist(filter: LibraryFilter, name: String)

    suspend fun updatePlaylist(playlist: SmartPlaylist)

    suspend fun delete(playlistId: Int)

    fun getSmartPlaylistTracks(filterJson: String, initialTimestamp: Long): Flow<List<TrackInfo>>

    suspend fun getSmartPlaylistTracksFromId(playlistId: Int, initialTimestamp: Long): List<TrackInfo>

    fun getAll(): Flow<List<SmartPlaylist>>

    fun getSmartPlaylist(playlistId: Int): Flow<SmartPlaylist>

    suspend fun getSmartPlaylistById(playlistId: Int): SmartPlaylist

    fun getSmartPlaylistStats(filterJson: String, initialTimestamp: Long): Flow<PlaylistStats>
}

data class RawPlaylistStats(
    val trackCount: Int = 0,
    val duration: Long = 0
)