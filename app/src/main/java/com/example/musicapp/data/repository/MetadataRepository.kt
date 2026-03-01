package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.DiscogsSearchResponse
import com.example.musicapp.data.dto.ReleaseSearchResponse
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import kotlinx.coroutines.flow.Flow

interface MetadataRepository {

    suspend fun enrichMetadata(isManual: Boolean) : Flow<ScanProgress>

    suspend fun updateAlbum(
        newAlbumTitle: String,
        oldAlbum: Album,
        newArtistName: String,
        oldArtist: Artist,
        newReleaseDate: String?,
        newAlbumArt: String?
    )

    suspend fun updateArtist(
        newArtistName: String,
        oldArtist: Artist,
        newAlbumMeta: AlbumMetadataResult? = null
    )
}

data class AlbumMetadataResult(
    val mbResponse: ReleaseSearchResponse?,
    val discogsResponse: DiscogsSearchResponse?,
    val album: Album
)