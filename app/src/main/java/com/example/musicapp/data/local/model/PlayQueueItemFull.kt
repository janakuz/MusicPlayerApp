package com.example.musicapp.data.local.model

import androidx.room.Embedded
import java.util.UUID

data class PlayQueueItemFull(
    val queueId: String = UUID.randomUUID().toString(),
    val originalOrder: Int,
    val shuffledOrder: Int = -1,
    val playlistEntryId: Int? = null,
    @Embedded val track: TrackInfo
)
