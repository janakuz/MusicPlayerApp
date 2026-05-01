package com.example.musicapp.data.repository

import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(sortBy: String, ascending: Boolean): Flow<List<Playlist>>

    fun getPlaylist(id: Int): Flow<Playlist>

    suspend fun getPlaylistById(id: Int): Playlist

    suspend fun insert(playlist: Playlist): Long

    suspend fun update(playlist: Playlist)

    suspend fun delete(playlist: Playlist)

    suspend fun deleteById(playlistId: Int)


}