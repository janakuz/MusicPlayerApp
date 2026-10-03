package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.SmartPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartPlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: SmartPlaylist)

    @Update
    suspend fun update(playlist: SmartPlaylist)

    @Delete
    suspend fun delete(playlist: SmartPlaylist)

    @Query("DELETE FROM playlists WHERE id=:playlistId")
    suspend fun deleteById(playlistId: Int)

    @Query("SELECT * FROM smart_playlists WHERE id = :playlistId")
    suspend fun getSmartPlaylistById(playlistId: Int): SmartPlaylist?

    @Query("SELECT * FROM smart_playlists")
    fun getAll(): Flow<List<SmartPlaylist>>
}