package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.entity.Mood
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(mood: Mood) : Long

    @Query("SELECT * FROM genres where name=:name")
    suspend fun getMoodByName(name: String): Mood?

    @Query("SELECT name FROM moods WHERE name LIKE '%' || :searchString || '%'")
    fun findMood(searchString: String): Flow<List<String>>

}
