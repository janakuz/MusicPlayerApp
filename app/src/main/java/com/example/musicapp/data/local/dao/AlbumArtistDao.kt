package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.local.model.AlbumIdWithArtist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.AlbumArtist
import com.example.musicapp.data.local.entity.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumArtistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(albumArtists: List<AlbumArtist>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(albumArtist: AlbumArtist)

    @Delete
    suspend fun delete(albumArtist: AlbumArtist)

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE ar.id = :artistId
    ORDER BY a.releaseDate ASC
    """)
    fun getAlbumsByArtist(artistId: Int): Flow<List<AlbumInfo>>

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE ar.id = :artistId
    ORDER BY a.releaseDate DESC
    """)
    fun getAlbumsByArtistDesc(artistId: Int): Flow<List<AlbumInfo>>


    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE ar.id = :artistId
    ORDER BY 
        CASE 
            WHEN title LIKE 'The %' THEN SUBSTR(title, 5)
            WHEN title LIKE 'A %' THEN SUBSTR(title, 3)
            WHEN title LIKE 'An %' THEN SUBSTR(title, 4)
            WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)
            ELSE title 
        END COLLATE NOCASE ASC""")
    fun getAlbumsByArtistTitle(artistId: Int): Flow<List<AlbumInfo>>

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE ar.id = :artistId
    ORDER BY 
        CASE 
            WHEN title LIKE 'The %' THEN SUBSTR(title, 5)
            WHEN title LIKE 'A %' THEN SUBSTR(title, 3)
            WHEN title LIKE 'An %' THEN SUBSTR(title, 4)
            WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)
            ELSE title 
        END COLLATE NOCASE DESC""")
    fun getAlbumsByArtistTitleDesc(artistId: Int): Flow<List<AlbumInfo>>


    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE ar.id = :artistId
    ORDER BY a.releaseDate ASC
    """)
    fun getAlbumsByArtistDuration(artistId: Int): Flow<List<AlbumInfo>>

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE ar.id = :artistId
    ORDER BY a.duration DESC
    """)
    fun getAlbumsByArtistDurationDesc(artistId: Int): Flow<List<AlbumInfo>>

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    ORDER BY a.duration ASC
    """)
    fun getAll(): Flow<List<AlbumInfo>>

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE a.isEnriched = FALSE OR ar.isEnriched = FALSE
    ORDER BY a.duration ASC
    """)
    suspend fun getAllUnenriched(): List<AlbumInfo>

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE a.enrichmentAttempted = FALSE OR ar.enrichmentAttempted = FALSE
    ORDER BY a.duration ASC
    """)
    suspend fun getAllUnattempted(): List<AlbumInfo>

    @Query("""
    SELECT a.*
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    WHERE aa.artistId = :artistId
    ORDER BY a.releaseDate ASC
    """)
    fun getAlbumsByArtistFull(artistId: Int): Flow<List<Album>>

    @Query("""
    SELECT ar.*
    FROM artists ar
    JOIN album_artists aa ON aa.artistId = ar.id
    WHERE aa.albumId = :albumId
    ORDER BY ar.name ASC
    """)
    suspend fun getAllAlbumArtists(albumId: Int): List<Artist>

    @Query("""
    SELECT ar.*, aa.albumId AS albumId
    FROM album_artists aa
    INNER JOIN artists ar ON aa.artistId = ar.id
""")
    suspend fun getAllAlbumArtistsWithArtistInfo(): List<AlbumIdWithArtist>

    @Query("UPDATE album_artists SET artistId = :newArtistId WHERE albumId = :albumId AND artistId = :oldArtistId")
    suspend fun updateArtistForAlbum(albumId: Int, oldArtistId: Int, newArtistId: Int)

    @Query("DELETE FROM album_artists WHERE albumId = :albumId AND artistId = :artistId")
    suspend fun deleteArtistFromAlbum(albumId: Int, artistId: Int)
}