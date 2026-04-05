package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.AlbumIdWithArtist
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface AlbumArtistRepository {
    fun getAllAlbumsByArtist(artistId: Int): Flow<List<AlbumInfo>>

    fun getAllAlbumsByArtistSorted(artistId: Int, oredrBy: SortOption): Flow<List<AlbumInfo>>

    fun getAllAlbumsByArtistFull(artistId: Int): Flow<List<Album>>

    suspend fun getAllAlbumArtists(albumId: Int): List<Artist>

    fun getAll(): Flow<List<AlbumInfo>>

    suspend fun getAllUnenriched(): List<AlbumInfo>

    suspend fun getAllUnattempted(): List<AlbumInfo>

    suspend fun getAllWithArtistInfo(): List<AlbumIdWithArtist>

    suspend fun insertAll(albumArtists: List<AlbumArtist>)

    suspend fun insert(albumArtist: AlbumArtist)

    suspend fun delete(albumArtist: AlbumArtist)

    suspend fun updateAlbumArtist(albumId: Int, oldArtistId: Int, newArtistId: Int)

}