package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.entity.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tracks: List<Track>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: Track)

    @Update
    suspend fun update(track: Track)

    @Delete
    suspend fun delete(track: Track)

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY title ASC
        """)
    fun getAllTracksByName(): Flow<List<TrackInfo>>

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY title DESC
        """)
    fun getAllTracksByNameDesc(): Flow<List<TrackInfo>>

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY duration ASC
        """)
    fun getAllTracksByDuration(): Flow<List<TrackInfo>>

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY duration DESC
        """)
    fun getAllTracksByDurationDesc(): Flow<List<TrackInfo>>

    @Query("SELECT * FROM tracks WHERE id=:id")
    fun getTrack(id: Int): Flow<Track>

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE t.id = :id
        ORDER BY title ASC
        """)
    fun getTrackInfo(id: Int): Flow<TrackInfo>

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE ar.id = :artistId
        ORDER BY title ASC
        """)
    fun getAllTracksByArtist(artistId: Int): Flow<List<TrackInfo>>

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE al.id = :albumId
        ORDER BY t.trackNumber ASC
        """)
    fun getAllTracksInAlbum(albumId: Int): Flow<List<TrackInfo>>
}