package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.AlbumArtistDao
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.AlbumArtist
import kotlinx.coroutines.flow.Flow

class OfflineAlbumArtistRepository(private val albumArtistDao : AlbumArtistDao) : AlbumArtistRepository {
    override fun getAllAlbumsByArtist(artistId: Int): Flow<List<AlbumInfo>> {
        return albumArtistDao.getAlbumsByArtist(artistId)
    }

    override fun getAll(): Flow<List<AlbumInfo>> {
        return albumArtistDao.getAll()
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