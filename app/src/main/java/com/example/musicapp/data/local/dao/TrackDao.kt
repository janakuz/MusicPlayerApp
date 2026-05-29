package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.local.model.PlaylistTrack
import com.example.musicapp.data.local.model.TrackInfo
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

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY 
        CASE 
            WHEN t.title LIKE 'The %' THEN SUBSTR(t.title, 5)
            WHEN t.title LIKE 'A %' THEN SUBSTR(t.title, 3)
            WHEN t.title LIKE 'An %' THEN SUBSTR(t.title, 4)
            WHEN t.title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(t.title, 2)
            ELSE t.title 
        END COLLATE NOCASE ASC"""
    )
    fun getAllTracksByName(): Flow<List<TrackInfo>>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY 
        CASE 
            WHEN t.title LIKE 'The %' THEN SUBSTR(t.title, 5)
            WHEN t.title LIKE 'A %' THEN SUBSTR(t.title, 3)
            WHEN t.title LIKE 'An %' THEN SUBSTR(t.title, 4)
            WHEN t.title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(t.title, 2)
            ELSE t.title 
        END COLLATE NOCASE DESC"""
    )
    fun getAllTracksByNameDesc(): Flow<List<TrackInfo>>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY duration ASC
        """
    )
    fun getAllTracksByDuration(): Flow<List<TrackInfo>>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        ORDER BY duration DESC
        """
    )
    fun getAllTracksByDurationDesc(): Flow<List<TrackInfo>>

    @Query("SELECT * FROM tracks WHERE id=:id")
    fun getTrack(id: Int): Flow<Track>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE t.id = :id
        ORDER BY title ASC
        """
    )
    fun getTrackInfo(id: Int): Flow<TrackInfo>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE ar.id = :artistId
        ORDER BY al.releaseDate ASC, t.trackNumber ASC
        """
    )
    fun getAllTracksByArtist(artistId: Int): Flow<List<TrackInfo>>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE al.id = :albumId
        ORDER BY t.trackNumber ASC
        """
    )
    fun getAllTracksInAlbum(albumId: Int): Flow<List<TrackInfo>>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE al.id = :albumId
        ORDER BY t.trackNumber ASC
        """
    )
    suspend fun getAlbumTracks(albumId: Int): List<TrackInfo>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE t.id in (:trackIds)
        ORDER BY t.trackNumber ASC
        """
    )
    suspend fun getTracksByIds(trackIds: List<Int>): List<TrackInfo>

    @Query(
        """
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        JOIN playlist_tracks pt on t.id=pt.trackId
        WHERE pt.id in (:entryIds)
        ORDER BY pt.position ASC
        """
    )
    suspend fun getPlaylistTracksByIds(entryIds: List<Int>): List<PlaylistTrack>


    @Query(
        """
        SELECT * 
        FROM tracks
        """
    )
    fun getAllTracksFull(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE fileUri=:uri")
    suspend fun getTrackByUri(uri: String): Track?

    @Query("SELECT * FROM tracks WHERE LOWER(filePath) LIKE :path")
    suspend fun findTrackByPath(path: String): Track?


    @Query("SELECT fileUri FROM tracks")
    suspend fun getAllUris(): List<String>

    @Query("DELETE FROM tracks WHERE fileUri IN (:uris)")
    suspend fun deleteByUri(uris: List<String>)

    @Query("UPDATE tracks SET artistId = :newArtistId WHERE albumId = :albumId AND artistId = :oldArtistId")
    suspend fun updateArtistForAlbum(albumId: Int, oldArtistId: Int, newArtistId: Int)

    @Query(
        "UPDATE tracks SET albumId = :newAlbumId WHERE albumId = :oldAlbumId AND " +
                "tracks.id in (:tracks)"
    )
    suspend fun moveToAlbum(oldAlbumId: Int, newAlbumId: Int, tracks: List<Int>)


    @Query(
        "UPDATE tracks SET artistId = :newArtistId WHERE artistId = :oldArtistId AND " +
                "tracks.id in (:tracks)"
    )
    suspend fun moveToArtist(oldArtistId: Int, newArtistId: Int, tracks: List<Int>)


    @Query("SELECT fileUri FROM tracks WHERE artistId = :artistId")
    suspend fun getTrackUrisByArtist(artistId: Int): List<String>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE LOWER(t.title) LIKE :query
        ORDER BY t.title ASC
        """
    )
    fun searchTracks(query: String): Flow<List<TrackInfo>>


    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE t.artistId=:artistId AND LOWER(t.title) LIKE :query
        ORDER BY t.title ASC
        """
    )
    fun searchArtistTracks(query: String, artistId: Int): Flow<List<TrackInfo>>

    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE t.albumId=:albumId AND LOWER(t.title) LIKE :query
        ORDER BY t.title ASC
        """
    )
    fun searchAlbumTracks(query: String, albumId: Int): Flow<List<TrackInfo>>

    @RawQuery(observedEntities = [Track::class])
    fun getFilteredTracks(query: SupportSQLiteQuery): Flow<List<TrackInfo>>
}