package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.PlaylistTracks
import com.example.musicapp.data.local.entity.Track
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

    @RawQuery(observedEntities = [Playlist::class, PlaylistTracks::class, Track::class])
    fun getPlaylistsSorted(query: SupportSQLiteQuery): Flow<List<PlaylistWithStats>>

    @Query("SELECT * from playlists WHERE id = :playlistId")
    fun getPlaylist(playlistId: Int): Flow<Playlist>

    @Query("SELECT * from playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Int): Playlist

}