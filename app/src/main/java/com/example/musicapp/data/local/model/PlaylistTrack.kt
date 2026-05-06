package com.example.musicapp.data.local.model

import androidx.room.Embedded

data class PlaylistTrack(
    val entryId: Int,
    val position: Int,
    val addedAt: Long,
    val playlistId: Int,
    @Embedded val trackInfo: TrackInfo
)
