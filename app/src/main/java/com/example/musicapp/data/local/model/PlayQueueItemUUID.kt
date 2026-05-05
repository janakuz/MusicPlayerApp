package com.example.musicapp.data.local.model

import java.util.UUID

data class PlayQueueItemUUID(
    val queueId: String = UUID.randomUUID().toString(),
    val originalOrder: Int,
    val shuffledOrder: Int = -1,
    val playlistEntryId: Int? = null,
    val track: TrackInfo
)
