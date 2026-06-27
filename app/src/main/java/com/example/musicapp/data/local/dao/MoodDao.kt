package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.local.entity.Mood
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.data.local.model.MoodInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(mood: Mood): Long

    @Query("SELECT * FROM moods where name=:name")
    suspend fun getMoodByName(name: String): Mood?

    @Query("SELECT name FROM moods WHERE name LIKE '%' || :searchString || '%'")
    fun findMood(searchString: String): Flow<List<String>>


    @Query("""SELECT m.*, COUNT(DISTINCT tm.trackId) as trackCount 
            FROM moods m 
            LEFT JOIN track_moods tm ON tm.moodId=m.id 
            GROUP BY m.id
            ORDER BY
                CASE WHEN :sortBy = 'name' AND :ascending = true THEN LOWER(m.name) END ASC,
                CASE WHEN :sortBy = 'name' AND :ascending = false THEN LOWER(m.name) END DESC,
                CASE WHEN :sortBy = 'count' AND :ascending = true THEN trackCount END ASC,
                CASE WHEN :sortBy = 'count' AND :ascending = false THEN trackCount END DESC
            """
    )
    fun getAllMoods(sortBy: String, ascending: Boolean): Flow<List<MoodInfo>>

}
