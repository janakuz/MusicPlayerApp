package com.example.musicapp.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.musicapp.data.local.dao.AlbumArtistDao
import com.example.musicapp.data.local.dao.TrackDao
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.AlbumArtist
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumIdWithArtist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

class AlbumArtistRepositoryImpl(
    private val albumArtistDao: AlbumArtistDao,
    private val trackDao: TrackDao
) : AlbumArtistRepository {
    override fun getAllAlbumsByArtistSorted(
        artistId: Int,
        orderBy: SortOption
    ): Flow<List<AlbumInfo>> {
        val titleSort = """
                         CASE 
                            WHEN title LIKE 'The %' THEN SUBSTR(title, 5)
                            WHEN title LIKE 'A %' THEN SUBSTR(title, 3)
                            WHEN title LIKE 'An %' THEN SUBSTR(title, 4)
                            WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)
                            ELSE title 
                         END COLLATE NOCASE
                        """.trimIndent()

        val sqlOrderBy =
            when (orderBy.field) {
                SortField.NAME -> titleSort
                SortField.DURATION -> "duration"
                SortField.RELEASE_DATE -> "releaseDate"
                else -> titleSort
            }

        val direction = if (orderBy.ascending) "ASC" else "DESC"

        val sqlString = """
                         SELECT a.id as albumId, a.title, a.releaseDate, a.image, ar.name as artistName, ar.id as artistId, a.duration, a.numTracks
                         FROM albums a
                         JOIN album_artists aa ON aa.albumId = a.id
                         JOIN artists ar ON aa.artistId = ar.id
                         WHERE ar.id = $artistId
                         ORDER BY $sqlOrderBy $direction
                        """.trimIndent()

        val query = SimpleSQLiteQuery(sqlString)

        return albumArtistDao.getArtistAlbumsSorted(query)
    }

    override fun getAllAlbumsByArtistFull(artistId: Int): Flow<List<Album>> {
        return albumArtistDao.getAlbumsByArtistFull(artistId)
    }

    override suspend fun getAllAlbumArtists(albumId: Int): List<Artist> {
        return albumArtistDao.getAllAlbumArtists(albumId)
    }

    override suspend fun getAllUnenriched(): List<AlbumInfo> {
        return albumArtistDao.getAllUnenriched()
    }

    override suspend fun getAllUnattempted(): List<AlbumInfo> {
        return albumArtistDao.getAllUnattempted()
    }

    override suspend fun getAllWithArtistInfo(): List<AlbumIdWithArtist> {
        return albumArtistDao.getAllAlbumArtistsWithArtistInfo()
    }

    override suspend fun insertAll(albumArtists: List<AlbumArtist>) {
        albumArtistDao.insertAll(albumArtists)
    }

    override suspend fun insert(albumArtist: AlbumArtist) {
        albumArtistDao.insert(albumArtist)
    }

    override suspend fun delete(albumArtist: AlbumArtist) {
        albumArtistDao.delete(albumArtist)
    }

    override suspend fun updateAlbumArtist(
        albumId: Int,
        oldArtistId: Int,
        newArtistId: Int
    ) {
        albumArtistDao.updateArtistForAlbum(albumId, oldArtistId, newArtistId)
        trackDao.updateArtistForAlbum(albumId, oldArtistId, newArtistId)
    }

    override suspend fun removeArtistFromAlbum(albumId: Int, artistId: Int) {
        albumArtistDao.deleteArtistFromAlbum(albumId, artistId)
    }
}