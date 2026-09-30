package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.AlbumArtist
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumIdWithArtist
import com.example.musicapp.data.local.model.AlbumInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumArtistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(albumArtists: List<AlbumArtist>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(albumArtist: AlbumArtist)

    @Delete
    suspend fun delete(albumArtist: AlbumArtist)


    @RawQuery(observedEntities = [AlbumArtist::class])
    fun getArtistAlbumsSorted(query: SupportSQLiteQuery): Flow<List<AlbumInfo>>

    @Query(
        """
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE a.isEnriched = FALSE OR ar.isEnriched = FALSE
    ORDER BY a.duration ASC
    """
    )
    suspend fun getAllUnenriched(): List<AlbumInfo>

    @Query(
        """
    SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    JOIN artists ar ON aa.artistId = ar.id
    WHERE a.enrichmentAttempted = FALSE OR ar.enrichmentAttempted = FALSE
    ORDER BY a.duration ASC
    """
    )
    suspend fun getAllUnattempted(): List<AlbumInfo>

    @Query(
        """
    SELECT a.*
    FROM albums a
    JOIN album_artists aa ON aa.albumId = a.id
    WHERE aa.artistId = :artistId
    ORDER BY a.releaseDate ASC
    """
    )
    fun getAlbumsByArtistFull(artistId: Int): Flow<List<Album>>

    @Query(
        """
    SELECT ar.*
    FROM artists ar
    JOIN album_artists aa ON aa.artistId = ar.id
    WHERE aa.albumId = :albumId
    ORDER BY ar.name ASC
    """
    )
    suspend fun getAllAlbumArtists(albumId: Int): List<Artist>

    @Query(
        """
    SELECT ar.*, aa.albumId AS albumId
    FROM album_artists aa
    INNER JOIN artists ar ON aa.artistId = ar.id
"""
    )
    suspend fun getAllAlbumArtistsWithArtistInfo(): List<AlbumIdWithArtist>

    @Query("UPDATE album_artists SET artistId = :newArtistId WHERE albumId = :albumId AND artistId = :oldArtistId")
    suspend fun updateArtistForAlbum(albumId: Int, oldArtistId: Int, newArtistId: Int)

    @Query("DELETE FROM album_artists WHERE albumId = :albumId AND artistId = :artistId")
    suspend fun deleteArtistFromAlbum(albumId: Int, artistId: Int)
}