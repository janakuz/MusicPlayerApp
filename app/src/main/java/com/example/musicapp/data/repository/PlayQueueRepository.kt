package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.QueueItemFull
import com.example.musicapp.data.entity.QueueItem
import kotlinx.coroutines.flow.Flow

interface PlayQueueRepository {

    val currentSession: Flow<PlaybackSession>

    suspend fun saveSession(index: Int, position: Long)

    fun getCurrentQueue() : Flow<List<QueueItemFull>>

    suspend fun saveQueue(tracks: List<QueueItem>)

    suspend fun clearQueue()

    suspend fun replaceQueue(tracks: List<QueueItem>)
}