package com.example.musicapp.data.repository

import com.example.musicapp.data.local.model.PlaylistTrack
import kotlinx.coroutines.flow.Flow

interface PlaylistTracksRepository {

    suspend fun removeTrackFromPlaylist(entryId: Int, playlistId: Int)

    suspend fun removeTracksFromPlaylist(entryIds: List<Int>)

    suspend fun insertTrackToPlaylist(playlistId: Int, trackId: Int)

    fun getAllTracksInPlaylist(playlistId: Int, sortBy: String, ascending: Boolean) : Flow<List<PlaylistTrack>>

    suspend fun getTracksInPlaylist(playlistId: Int) : List<PlaylistTrack>

    suspend fun addTracksToPlaylist(playlistId: Int, trackIds: List<Int>)

    fun getAll() : Flow<List<PlaylistTrack>>

    suspend fun reorder(reordered: List<PlaylistTrack>)
}