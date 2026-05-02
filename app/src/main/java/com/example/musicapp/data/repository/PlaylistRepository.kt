package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.PlaylistTrack
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.data.entity.PlaylistTracks
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(sortBy: String, ascending: Boolean): Flow<List<Playlist>>

    fun getPlaylist(id: Int): Flow<Playlist>

    suspend fun getPlaylistById(id: Int): Playlist

    suspend fun insert(playlist: Playlist): Long

    suspend fun update(playlist: Playlist)

    suspend fun delete(playlist: Playlist)

    suspend fun deleteById(playlistId: Int)

    fun getArtForCollage(playlistId: Int) : Flow<List<String>>

    fun getPlaylistStats(playlistId: Int) : Flow<PlaylistStats>

}

data class PlaylistStats(
    val images: List<String> = emptyList<String>(),
    val trackCount: Int,
    val duration: Long,
)