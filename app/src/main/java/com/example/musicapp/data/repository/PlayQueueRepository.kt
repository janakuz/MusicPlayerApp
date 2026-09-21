package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.QueueItem
import com.example.musicapp.data.local.model.PlayQueueItemFull
import kotlinx.coroutines.flow.Flow

interface PlayQueueRepository {

    val currentSession: Flow<PlaybackSession>

    val shuffleOn: Flow<Boolean>

    val repeatMode: Flow<Int>

    suspend fun saveSession(index: Int, position: Long)

    suspend fun updateShuffle(on: Boolean)

    suspend fun updateRepeat(mode: Int)

    fun getCurrentQueue(shuffleOn: Boolean): Flow<List<PlayQueueItemFull>>

    suspend fun saveQueue(tracks: List<QueueItem>)

    suspend fun clearQueue()

    suspend fun replaceQueue(tracks: List<QueueItem>)

    suspend fun shuffleQueue(currentUUID: String)
}