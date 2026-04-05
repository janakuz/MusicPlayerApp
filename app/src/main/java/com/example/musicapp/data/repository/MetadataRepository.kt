package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.ArtistSearchInfo
import com.example.musicapp.data.dto.DiscogsSearchResponse
import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.dto.ReleaseSearchResponse
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Track
import com.example.musicapp.data.repository.impl.AlbumArtistUpdate
import com.example.musicapp.data.repository.impl.ScanProgress
import kotlinx.coroutines.flow.Flow

interface MetadataRepository {

    suspend fun enrichMetadata(isManual: Boolean) : Flow<ScanProgress>

    suspend fun updateAlbum(
        newAlbumTitle: String,
        oldAlbum: Album,
        newArtistName: String,
        oldArtist: Artist?,
        newReleaseDate: String? = null,
        newAlbumArt: String? = null,
        track: Track? = null
    ) : AlbumArtistUpdate

    suspend fun updateArtist(
        newArtistName: String,
        oldArtist: Artist,
        mbArtist: ArtistSearchInfo? = null,
        albumToMove: Album? = null,
        track: Track? = null,
    ) : Artist

    suspend fun refetchAlbum(
        album: Release,
        currentAlbum: Album

    )

    suspend fun refetchArtist(
        mbArtist: ArtistSearchInfo,
        currentArtist: Artist
    )

    suspend fun moveToAlbum(
        album: Release,
        tracksToMove: List<Int>,
        oldAlbumId: Int

    )

    suspend fun moveToUnenriched(
        album: String,
        artist: String,
        tracksToMove: List<Int>,
        oldAlbumId: Int
    )
}

data class AlbumMetadataResult(
    val mbResponse: ReleaseSearchResponse?,
    val discogsResponse: DiscogsSearchResponse?,
    val album: Album
)