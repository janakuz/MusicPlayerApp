package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.entity.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(albums: List<Album>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(album: Album)

    @Update
    suspend fun update(album: Album)

    @Delete
    suspend fun delete(album: Album)

    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbumsByName(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY title DESC")
    fun getAllAlbumsByNameDesc(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY releaseDate ASC")
    fun getAllAlbumsByReleaseDate(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY releaseDate DESC")
    fun getAllAlbumsByReleaseDateDesc(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY duration ASC")
    fun getAllAlbumsByDuration(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY duration DESC")
    fun getAllAlbumsByDurationDesc(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE id=:id")
    fun getAlbum(id: Int): Flow<Album>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWithReturn(album: Album): Long

}