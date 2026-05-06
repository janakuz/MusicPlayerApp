package com.example.musicapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.model.PlaylistTrack
import com.example.musicapp.data.local.model.PlaylistWithArt
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(sortBy: SortOption): Flow<List<PlaylistWithArt>>

    fun getPlaylist(id: Int): Flow<Playlist>

    suspend fun getPlaylistById(id: Int): Playlist

    suspend fun insert(playlist: Playlist): Long

    suspend fun update(playlist: Playlist)

    suspend fun delete(playlist: Playlist)

    suspend fun deleteById(playlistId: Int)

    fun getArtForCollage(playlistId: Int): Flow<List<String>>

    fun getPlaylistStats(playlistId: Int): Flow<PlaylistStats>

    suspend fun getPlaylistImages(playlistId: Int): List<String>

    fun savePlaylistImage(context: Context, uri: Uri): String?

    suspend fun importPlaylist(file: Uri)

    fun exportPlaylist(uri: Uri, tracks: List<PlaylistTrack>)
}

data class PlaylistStats(
    val images: List<String> = emptyList<String>(),
    val trackCount: Int = 0,
    val duration: Long = 0,
)