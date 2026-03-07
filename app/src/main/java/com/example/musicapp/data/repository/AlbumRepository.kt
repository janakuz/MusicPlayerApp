package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.AlbumDiscogsResponse
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.dto.DiscogsSearchResponse
import com.example.musicapp.data.dto.ReleaseSearchResponse
import com.example.musicapp.data.entity.Album
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {

    fun getAllAlbumsByName(): Flow<List<Album>>

    fun getAllAlbumsByNameDesc(): Flow<List<Album>>

    fun getAllAlbumsByReleaseDate(): Flow<List<Album>>

    fun getAllAlbumsByReleaseDateDesc(): Flow<List<Album>>

    fun getAllAlbumsByDuration(): Flow<List<Album>>

    fun getAllAlbumsByDurationDesc(): Flow<List<Album>>

    fun getAllAlbums(orderBy: SortOption): Flow<List<Album>>

    fun getAlbum(id: Int): Flow<Album>

    suspend fun getAll(): List<Album>

    suspend fun getById(id: Int): Album

    suspend fun getByIdFull(id: Int): List<AlbumInfo>

    suspend fun getByTitle(title: String, year: String?): Album?

    suspend fun findAlbumMB(query: String): ReleaseSearchResponse?

    suspend fun findAlbumDiscogs(artist: String, album: String, year: String?): DiscogsSearchResponse?

    suspend fun getAlbumDiscogs(releaseId: String): AlbumDiscogsResponse?

    suspend fun getAlbumArt(mbid: String): String?

    suspend fun getAllCAAOptions(mbid: String): List<String>

    suspend fun insertAll(albums: List<Album>)

    suspend fun insert(album: Album)

    suspend fun insertWithReturn(album: Album): Long

    suspend fun update(album: Album)

    suspend fun delete(album: Album)

    suspend fun deleteOrphaned()

    suspend fun moveTracks(oldAlbumId: Int, newAlbumId: Int)
}
