package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.musicapp.data.local.entity.TrackMood

@Dao
interface TrackMoodDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(link: TrackMood)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(links: List<TrackMood>)

    @Query("DELETE FROM track_moods WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Int)

    @Transaction
    suspend fun syncMoods(trackId: Int, moodIds: List<Int>) {
        deleteByTrackId(trackId)
        moodIds.forEach { id ->
            insert(TrackMood(trackId = trackId, moodId = id))
        }
    }

    @Query(
        "SELECT m.name " +
                "FROM track_moods tm JOIN moods m ON tm.moodId = m.id " +
                "WHERE tm.trackId=:trackId"
    )
    suspend fun getTrackMoods(trackId: Int): List<String>

}