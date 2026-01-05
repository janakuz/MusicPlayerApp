package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.AlbumDao
import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.dto.ReleaseSearchResponse
import com.example.musicapp.data.dto.TrackInfo
import kotlinx.coroutines.flow.Flow
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.service.CoverArtArchiveApiService
import com.example.musicapp.data.service.DiscogsApiService
import com.example.musicapp.data.service.LastfmApiService
import com.example.musicapp.data.service.MusicbrainzApiService
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.components.SortField


class OfflineAlbumRepository(
    private val albumDao: AlbumDao,
    private val musicbrainzApiService: MusicbrainzApiService,
    private val coverArtArchiveApiService: CoverArtArchiveApiService) : AlbumRepository {

    override fun getAllAlbumsByName(): Flow<List<Album>> =
        albumDao.getAllAlbumsByName()

    override fun getAllAlbumsByNameDesc(): Flow<List<Album>> =
        albumDao.getAllAlbumsByNameDesc()

    override fun getAllAlbumsByReleaseDate(): Flow<List<Album>> =
        albumDao.getAllAlbumsByReleaseDate()

    override fun getAllAlbumsByReleaseDateDesc(): Flow<List<Album>> =
        albumDao.getAllAlbumsByReleaseDateDesc()

    override fun getAllAlbumsByDuration(): Flow<List<Album>> =
        albumDao.getAllAlbumsByDuration()

    override fun getAllAlbumsByDurationDesc(): Flow<List<Album>> =
        albumDao.getAllAlbumsByDurationDesc()

    override fun getAllAlbums(orderBy: SortOption): Flow<List<Album>> {
        return when (orderBy.field) {
            SortField.NAME -> if (orderBy.ascending) albumDao.getAllAlbumsByName() else albumDao.getAllAlbumsByNameDesc()
            SortField.DURATION -> if (orderBy.ascending) albumDao.getAllAlbumsByDuration() else albumDao.getAllAlbumsByDurationDesc()
            SortField.RELEASE_DATE -> if (orderBy.ascending) albumDao.getAllAlbumsByReleaseDate() else albumDao.getAllAlbumsByReleaseDateDesc()
        }
    }

    override fun getAlbum(id: Int): Flow<Album> =
        albumDao.getAlbum(id)

    override suspend fun findAlbumMB(query: String) : ReleaseSearchResponse {
        return musicbrainzApiService.findAlbum(query)
    }

    override suspend fun getAlbumArt(mbid: String): String {
        return coverArtArchiveApiService.getAlbumImage(mbid).images[0].image.replace("http://", "https://")
    }

    override suspend fun insertAll(albums: List<Album>) {
        albumDao.insertAll(albums)
    }

    override suspend fun insert(album: Album) {
        albumDao.insert(album)
    }

    override suspend fun insertWithReturn(album: Album): Long {
        return albumDao.insertWithReturn(album)
    }

    override suspend fun update(album: Album) {
        albumDao.update(album)
    }

    override suspend fun delete(album: Album) {
        albumDao.delete(album)
    }
}
