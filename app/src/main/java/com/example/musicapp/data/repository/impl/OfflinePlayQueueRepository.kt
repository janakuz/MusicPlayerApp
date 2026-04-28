package com.example.musicapp.data.repository.impl

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.musicapp.data.dao.QueueDao
import com.example.musicapp.data.dto.QueueItemFull
import com.example.musicapp.data.entity.QueueItem
import com.example.musicapp.data.repository.PlayQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class OfflinePlayQueueRepository (
    private val dataStore: DataStore<Preferences>,
    private val queueDao: QueueDao) : PlayQueueRepository {
    private companion object{
        val LAST_PLAYED_INDEX = intPreferencesKey("last_played_index")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val SHUFFLE_ON = booleanPreferencesKey("shuffle_on")
        val REPEAT_TYPE = intPreferencesKey("repeat_type")
    }

    override val currentSession: Flow<PlaybackSession> = dataStore.data.map {prefs ->
        PlaybackSession(
            playQueueIndex = prefs[LAST_PLAYED_INDEX] ?: 0,
            position = prefs[LAST_POSITION_MS] ?: 0L,
            shuffleOn = prefs[SHUFFLE_ON] ?: false,
            repeatType = prefs[REPEAT_TYPE] ?: 0
        )
    }

    override val shuffleOn: Flow<Boolean> = dataStore.data.map {  prefs ->
        prefs[SHUFFLE_ON] ?: false
    }.distinctUntilChanged()

    override val repeatMode: Flow<Int> = dataStore.data.map { prefs ->
        prefs[REPEAT_TYPE] ?: 0
    }.distinctUntilChanged()

    override suspend fun saveSession(queueIndex: Int, position: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_PLAYED_INDEX] = queueIndex
            prefs[LAST_POSITION_MS] = position
        }
    }

    override suspend fun updateShuffle(on: Boolean) {
        dataStore.edit { prefs ->
            prefs[SHUFFLE_ON] = on
        }
    }

    override suspend fun updateRepeat(mode: Int) {
        dataStore.edit { prefs ->
            prefs[REPEAT_TYPE] = mode
        }
    }


    override fun getCurrentQueue(shuffleOn: Boolean): Flow<List<QueueItemFull>> {
        if (shuffleOn) return queueDao.getQueueShuffled()
        else return queueDao.getQueue()
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

    override suspend fun shuffleQueue(currentUUID: String) {
        val items = queueDao.getQueue().first().map { item ->
            QueueItem(
                trackId = item.trackInfo.trackId,
                orderIndex = item.orderIndex,
                uuid = item.uuid,
                shuffledIndex = item.shuffledIndex
            )
        }

        Log.d("shuffle", items.joinToString())

        val currentItem = items.find { it.uuid == currentUUID }
        val others = items.filter { it.uuid != currentUUID }.shuffled()

        Log.d("shuffle", currentItem?.uuid ?: "")

        val updatedList = mutableListOf<QueueItem>()


        currentItem?.let {
            updatedList.add(it.copy(shuffledIndex = 0))
        }

        others.forEachIndexed { index, item ->
            val newIndex = if (currentItem != null) index + 1 else index
            updatedList.add(item.copy(shuffledIndex = newIndex))
        }

        Log.d("shuffle", updatedList.joinToString())

        replaceQueue(updatedList)
    }
}

data class PlaybackSession(
    val playQueueIndex: Int,
    val position: Long,
    val shuffleOn: Boolean = false,
    val repeatType: Int = 0
)