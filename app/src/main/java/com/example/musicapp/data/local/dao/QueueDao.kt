package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.musicapp.data.local.entity.QueueItem
import com.example.musicapp.data.local.model.PlayQueueItemFull
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    @Query(
        """
        SELECT q.orderIndex as originalOrder, q.uuid as queueId, q.shuffledIndex as shuffledOrder, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM play_queue q
        JOIN tracks t on q.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY q.orderIndex ASC"""
    )
    fun getQueue(): Flow<List<PlayQueueItemFull>>

    @Query(
        """
        SELECT q.orderIndex as originalOrder, q.uuid as queueId, q.shuffledIndex as shuffledOrder, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM play_queue q
        JOIN tracks t on q.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY q.shuffledIndex ASC"""
    )
    fun getQueueShuffled(): Flow<List<PlayQueueItemFull>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQueue(tracks: List<QueueItem>)

    @Query("DELETE FROM play_queue")
    suspend fun clearQueue()

    @Transaction
    suspend fun replaceQueue(tracks: List<QueueItem>) {
        clearQueue()
        saveQueue(tracks)
    }
}