package com.example.musicapp.data.local.model

import androidx.room.Embedded

data class BlockWithTracks(
    val blockNumber: Int,
    val tracks: List<TrackInfo>
)

data class BlockWithTrackInfo(
    val blockNumber: Int,
    @Embedded val trackInfo: TrackInfo
)