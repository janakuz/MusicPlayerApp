package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.AlbumArtist
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumIdWithArtist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface AlbumArtistRepository {
    fun getAllAlbumsByArtist(artistId: Int): Flow<List<AlbumInfo>>

    fun getAllAlbumsByArtistSorted(artistId: Int, orderBy: SortOption): Flow<List<AlbumInfo>>

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

    suspend fun removeArtistFromAlbum(albumId: Int, artistId: Int)

}