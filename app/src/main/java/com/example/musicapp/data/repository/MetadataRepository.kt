package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
import com.example.musicapp.data.remote.dto.DiscogsSearchResponse
import com.example.musicapp.data.remote.dto.Release
import com.example.musicapp.data.remote.dto.ReleaseSearchResponse
import kotlinx.coroutines.flow.Flow

interface MetadataRepository {

    suspend fun backfillGenres(): Flow<ScanProgress>

    suspend fun backfillCoutriesAndActivity():  Flow<ScanProgress>

    suspend fun enrichMetadata(isManual: Boolean): Flow<ScanProgress>

    suspend fun updateAlbum(
        newAlbumTitle: String,
        oldAlbum: Album,
        newArtistName: String,
        oldArtist: Artist?,
        newReleaseDate: String? = null,
        newAlbumArt: String? = null,
        track: Track? = null
    ): AlbumArtistUpdate

    suspend fun updateArtist(
        newArtistName: String,
        oldArtist: Artist,
        mbArtist: ArtistSearchInfo? = null,
        albumToMove: Album? = null,
        track: Track? = null,
    ): Artist

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
        oldAlbumId: Int,
        markEnriched: Boolean = false,
    )
}

data class AlbumMetadataResult(
    val mbResponse: ReleaseSearchResponse?,
    val discogsResponse: DiscogsSearchResponse?,
    val album: Album
)