package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.AlbumArtistDao
import com.example.musicapp.data.dto.AlbumIdWithArtist
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

class OfflineAlbumArtistRepository(private val albumArtistDao : AlbumArtistDao) : AlbumArtistRepository {
    override fun getAllAlbumsByArtist(artistId: Int): Flow<List<AlbumInfo>> {
        return albumArtistDao.getAlbumsByArtist(artistId)
    }

    override fun getAllAlbumsByArtistSorted(artistId: Int, orderBy: SortOption): Flow<List<AlbumInfo>> {
        return when (orderBy.field) {
            SortField.NAME -> if (orderBy.ascending) albumArtistDao.getAlbumsByArtistTitle(artistId) else albumArtistDao.getAlbumsByArtistTitleDesc(artistId)
            SortField.DURATION -> if (orderBy.ascending) albumArtistDao.getAlbumsByArtistDuration(artistId) else albumArtistDao.getAlbumsByArtistDurationDesc(artistId)
            SortField.RELEASE_DATE -> if (orderBy.ascending) albumArtistDao.getAlbumsByArtist(artistId) else albumArtistDao.getAlbumsByArtistDesc(artistId)
        }
    }

    override fun getAllAlbumsByArtistFull(artistId: Int): Flow<List<Album>> {
        return albumArtistDao.getAlbumsByArtistFull(artistId)
    }

    override fun getAllAlbumArtists(albumId: Int): List<Artist> {
        return albumArtistDao.getAllAlbumArtists(albumId)
    }

    override fun getAll(): Flow<List<AlbumInfo>> {
        return albumArtistDao.getAll()
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
}