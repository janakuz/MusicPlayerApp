package com.example.musicapp.data.local.model

import androidx.room.Embedded

data class QueueItemFull(
    val orderIndex: Int,
    val uuid: String,
    val shuffledIndex: Int?,
    @Embedded val trackInfo: TrackInfo
)