package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.dto.AlbumIdWithArtist
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Track
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
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE ar.id = :artistId
    ORDER BY a.releaseDate ASC
    """)
    fun getAlbumsByArtist(artistId: Int): Flow<List<AlbumInfo>>

    @Query("""
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    ORDER BY a.releaseDate ASC
    """)
    fun getAll(): Flow<List<AlbumInfo>>

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
    fun getAllAlbumArtists(albumId: Int): List<Artist>

    @Query("""
    SELECT ar.*, aa.albumId AS albumId
    FROM album_artists aa
    INNER JOIN artists ar ON aa.artistId = ar.id
""")
    suspend fun getAllAlbumArtistsWithArtistInfo(): List<AlbumIdWithArtist>


}