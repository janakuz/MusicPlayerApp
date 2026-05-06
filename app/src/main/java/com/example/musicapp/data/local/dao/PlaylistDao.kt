package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.model.PlaylistWithStats
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

    @Query(
        "SELECT p.*, SUM(t.duration) AS playlistDuration, COUNT(pt.id) as trackCount " +
                "FROM playlists p " +
                "LEFT JOIN playlist_tracks pt on p.id=pt.playlistId " +
                "LEFT JOIN tracks t on pt.trackId=t.id " +
                "GROUP BY p.id " +
                "ORDER BY " +
                "CASE WHEN :sortBy = 'name' AND :ascending=true THEN LOWER(p.name) END ASC, " +
                "CASE WHEN :sortBy = 'name' AND :ascending=false THEN LOWER(p.name) END DESC, " +
                "CASE WHEN :sortBy = 'createdAt' AND :ascending=false THEN p.createdAt END DESC, " +
                "CASE WHEN :sortBy = 'createdAt' AND :ascending=true THEN p.createdAt END ASC, " +
                "CASE WHEN :sortBy = 'lastUpdated' AND :ascending=false THEN p.lastUpdated END DESC, " +
                "CASE WHEN :sortBy = 'lastUpdated' AND :ascending=true THEN p.lastUpdated END ASC "
    )
    fun getAllPlaylists(sortBy: String, ascending: Boolean): Flow<List<PlaylistWithStats>>

    @Query(
        """
        SELECT p.*, SUM(t.duration) AS playlistDuration, COUNT(pt.id) as trackCount
        FROM playlists p
        LEFT JOIN playlist_tracks pt on p.id=pt.playlistId
        LEFT JOIN tracks t on pt.trackId=t.id
        GROUP BY p.id
        ORDER BY 
        CASE WHEN :ascending = true THEN playlistDuration END ASC, 
        CASE WHEN :ascending = false THEN playlistDuration END DESC
        """
    )
    fun getAllPlaylistsByDuration(ascending: Boolean): Flow<List<PlaylistWithStats>>


    @Query(
        """
        SELECT p.*, SUM(t.duration) AS playlistDuration, COUNT(pt.id) as trackCount
        FROM playlists p
        LEFT JOIN playlist_tracks pt on p.id=pt.playlistId
        LEFT JOIN tracks t on pt.trackId=t.id
        GROUP BY p.id
        ORDER BY 
        CASE WHEN :ascending = true THEN trackCount END ASC, 
        CASE WHEN :ascending = false THEN trackCount END DESC
        """
    )
    fun getAllPlaylistsByNumTracks(ascending: Boolean): Flow<List<PlaylistWithStats>>

    @Query("SELECT * from playlists WHERE id = :playlistId")
    fun getPlaylist(playlistId: Int): Flow<Playlist>

    @Query("SELECT * from playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Int): Playlist

}