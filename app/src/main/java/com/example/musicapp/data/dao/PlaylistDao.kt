package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.entity.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id=:playlistId")
    suspend fun deleteById(playlistId: Int)

    @Query("SELECT * FROM playlists ORDER BY " +
            "CASE WHEN :sortBy = 'name' AND :ascending=true THEN name END ASC, " +
            "CASE WHEN :sortBy = 'name' AND :ascending=false THEN name END DESC, " +
            "CASE WHEN :sortBy = 'createdAt' AND :ascending=false THEN createdAt END DESC, " +
            "CASE WHEN :sortBy = 'createdAt' AND :ascending=true THEN createdAt END ASC, " +
            "CASE WHEN :sortBy = 'lastUpdated' AND :ascending=false THEN lastUpdated END DESC, " +
            "CASE WHEN :sortBy = 'lastUpdated' AND :ascending=true THEN lastUpdated END ASC "
    )
    fun getAllPlaylists(sortBy: String, ascending: Boolean): Flow<List<Playlist>>

    @Query("SELECT * from playlists WHERE id = :playlistId")
    fun getPlaylist(playlistId: Int): Flow<Playlist>

    @Query("SELECT * from playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Int): Playlist

}