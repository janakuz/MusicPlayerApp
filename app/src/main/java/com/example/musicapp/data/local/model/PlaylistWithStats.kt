package com.example.musicapp.data.local.model

import androidx.room.Embedded
import com.example.musicapp.data.local.entity.Playlist

data class PlaylistWithStats(
    @Embedded val playlist: Playlist,
    val trackCount: Int,
    val playlistDuration: Long
)

data class PlaylistWithArt(
    @Embedded val stats: PlaylistWithStats,
    val top4Images: List<String>
)