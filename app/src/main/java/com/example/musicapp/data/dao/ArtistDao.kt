package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<Artist>>

    @Query("SELECT * FROM artists ORDER BY name DESC")
    fun getAllArtistsDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM artists where id=:id")
    fun getArtist(id: Int): Flow<Artist>

    @Query("SELECT * FROM artists where LOWER(name)=LOWER(:name)")
    suspend fun getArtistByName(name: String): List<Artist>

    @Query("SELECT * FROM artists where mbId=:mbId")
    suspend fun getArtistByMbid(mbId: String): Artist?


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(artists: List<Artist>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: Artist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWithReturn(artist: Artist): Long


    @Update
    suspend fun update(artist: Artist)

    @Delete
    suspend fun delete(artist: Artist)
}
