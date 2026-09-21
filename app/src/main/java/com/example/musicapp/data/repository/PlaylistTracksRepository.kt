package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.PlaylistTracks
import com.example.musicapp.data.local.model.PlaylistTrack
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow

interface PlaylistTracksRepository {

    suspend fun removeTrackFromPlaylist(entryId: Int, playlistId: Int)

    suspend fun removeTracksFromPlaylist(entryIds: List<Int>)

    suspend fun insertTrackToPlaylist(playlistId: Int, trackId: Int)

    suspend fun removeDuplicates(deduplicated: List<PlaylistTracks>, playlistId: Int)

    suspend fun findDuplicates(playlistId: Int): DuplicatesWithCount

    fun getAllTracksInPlaylist(
        playlistId: Int,
        sortBy: String,
        ascending: Boolean
    ): Flow<List<PlaylistTrack>>

    suspend fun getTracksInPlaylist(playlistId: Int): List<PlaylistTrack>

    suspend fun addTracksToPlaylist(playlistId: Int, trackIds: List<Int>)

    suspend fun getDuplicates(playlistId: Int, trackIds: List<Int>): List<TrackInfo>

    fun getAll(): Flow<List<PlaylistTrack>>

    suspend fun reorder(reordered: List<PlaylistTrack>)

    suspend fun getEntry(entryId: Int): PlaylistTracks?
}

data class DuplicatesWithCount(
    val deduplicated: List<PlaylistTracks>,
    val countDuplicates: Int
)