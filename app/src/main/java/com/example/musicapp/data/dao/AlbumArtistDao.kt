package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
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


}