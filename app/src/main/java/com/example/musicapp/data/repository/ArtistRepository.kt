package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.ArtistDicogsResponse
import com.example.musicapp.data.dto.ArtistMBResponse
import com.example.musicapp.data.dto.ArtistSearchInfo
import com.example.musicapp.data.dto.ArtistSearchResponse
import com.example.musicapp.data.entity.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    fun getAllArtists(): Flow<List<Artist>>

    fun getAllArtistsDesc(): Flow<List<Artist>>

    fun getAllArtistsSorted(ascending: Boolean): Flow<List<Artist>>

    fun getArtist(id: Int): Flow<Artist>

    suspend fun getOrCreateArtistByName(name: String, searchKey: String): Int

    suspend fun getArtistByName(name: String): List<Artist>

    suspend fun getArtistByMbid(mbId: String): Artist?

    suspend fun getArtistMusicbrainzInfo(mbid: String): ArtistMBResponse

    suspend fun getArtistDiscogsInfo(discogsId: String): ArtistDicogsResponse?

    suspend fun getArtistBio(mbid: String?, name: String): String

    suspend fun findArtistMB(artistName: String): List<ArtistSearchInfo>

    suspend fun insertAll(artists: List<Artist>)

    suspend fun insertAllString(artists: List<String>)

    suspend fun insert(artist: Artist)

    suspend fun insertByName(name: String): Long

    suspend fun insertWithReturn(artist: Artist): Long

    suspend fun update(artist: Artist)

    suspend fun delete(artist: Artist)

    suspend fun deleteById(artistId: Int)

    suspend fun deleteOrphaned()

    suspend fun moveTracks(oldArtistId: Int, newArtistId: Int, tracks: List<Int>? = emptyList())

    suspend fun getTrackUrisByArtist(artistId: Int): List<String>
}