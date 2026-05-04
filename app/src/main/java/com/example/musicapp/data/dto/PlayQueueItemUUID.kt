package com.example.musicapp.data.dto

data class PlayQueueItemUUID(
    val queueId: String = java.util.UUID.randomUUID().toString(),
    val originalOrder: Int,
    val shuffledOrder: Int = -1,
    val playlistEntryId: Int? = null,
    val track: TrackInfo
)
