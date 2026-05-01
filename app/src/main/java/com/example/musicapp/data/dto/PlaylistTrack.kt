package com.example.musicapp.data.dto

import androidx.room.Embedded

data class PlaylistTrack(
    val entryId: Int,
    val position: Int,
    @Embedded val trackInfo: TrackInfo
)
