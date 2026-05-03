package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.musicapp.data.dto.PlaylistTrack
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.data.entity.PlaylistTracks
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTracksDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackToPlaylist(entry: PlaylistTracks)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<PlaylistTracks>)

    @Delete
    suspend fun removeTrackFromPlaylist(entry: PlaylistTracks)

    @Update
    suspend fun updateTrackPosition(entry: PlaylistTracks)

    @Query("DELETE FROM playlist_tracks WHERE id = :entryId")
    suspend fun removeTrackFromPlaylistByIds(entryId: Int)

    @Query("DELETE FROM playlist_tracks WHERE id in (:entryIds)")
    suspend fun removeTracksFromPlaylist(entryIds: List<Int>)

    @Query("""
    UPDATE playlist_tracks 
    SET position = position - 1 
    WHERE playlistId = :playlistId AND position > :deletedPosition
""")
    suspend fun shiftPositionsDown(playlistId: Int, deletedPosition: Int)

    @Update
    suspend fun reorder(reordered: List<PlaylistTracks>)

    @Query("SELECT position FROM playlist_tracks WHERE id = :entryId")
    suspend fun getTrackPosition(entryId: Int): Int?

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Int): Int?

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
    """)
    fun getALl() : Flow<List<PlaylistTrack>>



    @Query("""
    SELECT DISTINCT a.image
    FROM tracks t
    JOIN albums a ON t.albumId=a.id
    JOIN playlist_tracks pt ON t.id = pt.trackId
    WHERE pt.playlistId = :playlistId AND a.image IS NOT NULL AND a.image != ""
    ORDER BY pt.position ASC
    LIMIT 4
    """)
    fun getTop4ImagesForPlaylist(playlistId: Int): Flow<List<String>>

    @Query("""
        SELECT COUNT(*)
        FROM playlist_tracks
        WHERE playlistId = :playlistId
    """)
    fun getTrackCount(playlistId: Int): Flow<Int>

    @Query("""
        SELECT SUM(t.duration)
        FROM playlist_tracks pt
        JOIN tracks t ON pt.trackId=t.id
        WHERE playlistId = :playlistId
    """)
    fun getDuration(playlistId: Int): Flow<Long>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY pt.position ASC
    """)
    suspend fun getTracksForPlaylist(playlistId: Int): List<PlaylistTrack>


    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY pt.position ASC
    """)
    fun getTracksForPlaylistByPosition(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY pt.addedAt DESC
    """)
    fun getTracksForPlaylistByTimeAdded(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY pt.addedAt ASC
    """)
    fun getTracksForPlaylistByTimeAddedAsc(playlistId: Int): Flow<List<PlaylistTrack>>


    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY
        CASE 
            WHEN t.title LIKE 'The %' THEN SUBSTR(t.title, 5)
            WHEN t.title LIKE 'A %' THEN SUBSTR(t.title, 3)
            WHEN t.title LIKE 'An %' THEN SUBSTR(t.title, 4)
            WHEN t.title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(t.title, 2)
            ELSE t.title 
        END COLLATE NOCASE ASC
    """)
    fun getTracksForPlaylistByTrackTitle(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY
        CASE 
            WHEN t.title LIKE 'The %' THEN SUBSTR(t.title, 5)
            WHEN t.title LIKE 'A %' THEN SUBSTR(t.title, 3)
            WHEN t.title LIKE 'An %' THEN SUBSTR(t.title, 4)
            WHEN t.title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(t.title, 2)
            ELSE t.title 
        END COLLATE NOCASE DESC
    """)
    fun getTracksForPlaylistByTrackTitleDesc(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY
        CASE
            WHEN al.title LIKE 'The %' THEN SUBSTR(al.title, 5)
            WHEN al.title LIKE 'A %' THEN SUBSTR(al.title, 3)
            WHEN al.title LIKE 'An %' THEN SUBSTR(al.title, 4)
            WHEN al.title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(al.title, 2)
            ELSE al.title 
            END COLLATE NOCASE ASC
    """)
    fun getTracksForPlaylistByAlbumTitle(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY
        CASE
            WHEN al.title LIKE 'The %' THEN SUBSTR(al.title, 5)
            WHEN al.title LIKE 'A %' THEN SUBSTR(al.title, 3)
            WHEN al.title LIKE 'An %' THEN SUBSTR(al.title, 4)
            WHEN al.title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(al.title, 2)
            ELSE al.title 
            END COLLATE NOCASE DESC
    """)
    fun getTracksForPlaylistByAlbumTitleDesc(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY
        CASE
            WHEN ar.name LIKE 'The %' THEN SUBSTR(ar.name, 5)
            WHEN ar.name LIKE 'A %' THEN SUBSTR(ar.name, 3)
            WHEN ar.name LIKE 'An %' THEN SUBSTR(ar.name, 4)
            WHEN ar.name GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(ar.name, 2)
            ELSE ar.name
        END COLLATE NOCASE ASC
    """)
    fun getTracksForPlaylistByArtist(playlistId: Int): Flow<List<PlaylistTrack>>

    @Query("""
        SELECT pt.id as entryId, pt.position, pt.addedAt, pt.playlistId, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId
        FROM playlist_tracks pt
        JOIN tracks t on pt.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        WHERE pt.playlistId = :playlistId 
        ORDER BY
        CASE
            WHEN ar.name LIKE 'The %' THEN SUBSTR(ar.name, 5)
            WHEN ar.name LIKE 'A %' THEN SUBSTR(ar.name, 3)
            WHEN ar.name LIKE 'An %' THEN SUBSTR(ar.name, 4)
            WHEN ar.name GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(ar.name, 2)
            ELSE ar.name
        END COLLATE NOCASE DESC
    """)
    fun getTracksForPlaylistByArtistDesc(playlistId: Int): Flow<List<PlaylistTrack>>

}