package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.local.entity.Genre
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(genre: Genre) : Long

    @Query("SELECT * FROM genres where name=:name")
    suspend fun getGenreByName(name: String): Genre?

    @Query("SELECT name FROM genres WHERE name LIKE '%' || :searchString || '%'")
    fun findGenre(searchString: String): Flow<List<String>>
}