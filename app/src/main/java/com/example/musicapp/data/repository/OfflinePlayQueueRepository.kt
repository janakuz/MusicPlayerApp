package com.example.musicapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.musicapp.data.dao.QueueDao
import com.example.musicapp.data.dto.QueueItemFull
import com.example.musicapp.data.entity.QueueItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflinePlayQueueRepository (
    private val dataStore: DataStore<Preferences>,
    private val queueDao: QueueDao) : PlayQueueRepository {
    private companion object{
        val LAST_PLAYED_INDEX = intPreferencesKey("last_played_index")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
    }

    override val currentSession: Flow<PlaybackSession> = dataStore.data.map {prefs ->
        PlaybackSession(
            playQueueIndex = prefs[LAST_PLAYED_INDEX] ?: 0,
            position = prefs[LAST_POSITION_MS] ?: 0L
        )
    }

    override suspend fun saveSession(queueIndex: Int, position: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_PLAYED_INDEX] = queueIndex
            prefs[LAST_POSITION_MS] = position
        }
    }

    override fun getCurrentQueue(): Flow<List<QueueItemFull>> {
        return queueDao.getQueue()
    }

    override suspend fun saveQueue(tracks: List<QueueItem>) {
        queueDao.saveQueue(tracks)
    }

    override suspend fun clearQueue() {
        queueDao.clearQueue()
    }

    override suspend fun replaceQueue(tracks: List<QueueItem>) {
        queueDao.replaceQueue(tracks)
    }
}

data class PlaybackSession(
    val playQueueIndex: Int,
    val position: Long
)