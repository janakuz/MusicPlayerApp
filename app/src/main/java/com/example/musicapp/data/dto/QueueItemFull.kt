package com.example.musicapp.data.dto

import androidx.room.Embedded

data class QueueItemFull(
    val orderIndex: Int,
    val uuid: String,
    val shuffledIndex: Int?,
    @Embedded val trackInfo: TrackInfo
)