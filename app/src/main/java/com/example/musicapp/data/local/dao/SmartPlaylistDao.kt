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
import com.example.musicapp.data.local.entity.AlbumArtist
import com.example.musicapp.data.local.entity.AlbumGenre
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.ArtistGenre
import com.example.musicapp.data.local.entity.Mood
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.SmartPlaylist
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.local.entity.TrackMood
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.repository.PlaylistStats
import com.example.musicapp.data.repository.RawPlaylistStats
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
    suspend fun getSmartPlaylistById(playlistId: Int): SmartPlaylist

    @Query("SELECT * FROM smart_playlists WHERE id = :playlistId")
    fun getById(playlistId: Int): Flow<SmartPlaylist>

    @Query("SELECT * FROM smart_playlists")
    fun getAll(): Flow<List<SmartPlaylist>>

    @RawQuery(observedEntities = [
        Track::class,
        Artist::class,
        Album::class,
        AlbumGenre::class,
        ArtistGenre::class,
        TrackMood::class,
        Mood::class,
    ])
    suspend fun getFilteredTracksSuspend(query: SupportSQLiteQuery): List<TrackInfo>

    @RawQuery(observedEntities = [
        Track::class,
        Artist::class,
        Album::class,
        AlbumGenre::class,
        ArtistGenre::class,
        TrackMood::class,
        Mood::class,
    ])
    fun getSmartPlaylistStats(query: SupportSQLiteQuery): Flow<RawPlaylistStats>

    @RawQuery(observedEntities = [
        Track::class,
        Artist::class,
        Album::class,
        AlbumGenre::class,
        ArtistGenre::class,
        TrackMood::class,
        Mood::class,
    ])
    fun getSmartPlaylistCollageArtwork(query: SupportSQLiteQuery): Flow<List<String>>

}