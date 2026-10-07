package com.example.musicapp.data.local.model

import androidx.room.Embedded

data class PlaylistTrack(
    val entryId: Int,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis(),
    val playlistId: Int,
    @Embedded val trackInfo: TrackInfo
)
