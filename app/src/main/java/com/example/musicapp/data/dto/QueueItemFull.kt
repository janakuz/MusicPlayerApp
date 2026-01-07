package com.example.musicapp.data.dto

import androidx.room.Embedded

data class QueueItemFull(
    val orderIndex: Int,
    val uuid: String,
    @Embedded val trackInfo: TrackInfo
)