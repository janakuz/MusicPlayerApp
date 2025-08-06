package com.example.musicapp.data.repository

import com.example.musicapp.data.entity.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {

    fun getAllAlbumsByName(): Flow<List<Album>>

    fun getAllAlbumsByNameDesc(): Flow<List<Album>>

    fun getAllAlbumsByReleaseDate(): Flow<List<Album>>

    fun getAllAlbumsByReleaseDateDesc(): Flow<List<Album>>

    fun getAllAlbumsByDuration(): Flow<List<Album>>

    fun getAllAlbumsByDurationDesc(): Flow<List<Album>>

    fun getAllAlbums(orderBy: SortFieldAlbum, descending: Boolean): Flow<List<Album>>

    fun getAlbum(id: Int): Flow<Album>

    suspend fun findAlbumMB(query: String) : String

    suspend fun findAlbumDG(query: String) : String

    suspend fun findAlbumLFM(artist: String, album: String) : String


    suspend fun insertAll(albums: List<Album>)

    suspend fun insert(album: Album)

    suspend fun insertWithReturn(album: Album): Long

    suspend fun update(album: Album)

    suspend fun delete(album: Album)
}

enum class SortFieldAlbum{
    TITLE,
    DURATION,
    RELEASE_DATE
}