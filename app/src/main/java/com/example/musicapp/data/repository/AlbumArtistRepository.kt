package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import kotlinx.coroutines.flow.Flow

interface AlbumArtistRepository {
    fun getAllAlbumsByArtist(artistId: Int): Flow<List<AlbumInfo>>

    fun getAllAlbumsByArtistFull(artistId: Int): Flow<List<Album>>

    fun getAll(): Flow<List<AlbumInfo>>

    suspend fun insertAll(albumArtists: List<AlbumArtist>)

    suspend fun insert(albumArtist: AlbumArtist)

    suspend fun delete(albumArtist: AlbumArtist)

}