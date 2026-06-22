package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.SimilarArtists
import com.example.musicapp.data.local.model.ArtistWithArea
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.data.remote.dto.ArtistDicogsResponse
import com.example.musicapp.data.remote.dto.ArtistInfoLastfm
import com.example.musicapp.data.remote.dto.ArtistLastfmResponse
import com.example.musicapp.data.remote.dto.ArtistMBResponse
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
import com.example.musicapp.data.remote.dto.SimilarArtist
import com.example.musicapp.data.remote.dto.SimilarArtistsResponse
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    fun getAllArtists(): Flow<List<Artist>>


    suspend fun getAll(): List<Artist>

    fun getAllArtistsDesc(): Flow<List<Artist>>

    fun getAllArtistsSorted(ascending: Boolean): Flow<List<Artist>>

    fun getArtist(id: Int): Flow<Artist>

    fun getArtistWithArea(id: Int): Flow<ArtistWithArea>

    fun searchArtists(query: String): Flow<List<Artist>>

    suspend fun getOrCreateArtistByName(name: String, searchKey: String): Int

    suspend fun getArtistByName(name: String): List<Artist>

    suspend fun getArtistByMbid(mbId: String): Artist?

    suspend fun getArtistMusicbrainzInfo(mbid: String): ArtistMBResponse

    suspend fun getArtistDiscogsInfo(discogsId: String): ArtistDicogsResponse?

    suspend fun getArtistBio(mbid: String?, name: String): String

    suspend fun getArtistLastfmInfo(mbid: String?, name: String): ArtistInfoLastfm?

    suspend fun getAllSimilarArtists(artistId: Int): List<Int>

    suspend fun getSimilarArtistsLastfm(artistName: String): List<SimilarArtist>

    suspend fun insertSimilar(artists: List<SimilarArtists>)

    suspend fun getSimilarArtists(artistId: Int, minSimilarityScore: Double = 0.0): Flow<List<Artist>>

    suspend fun insertSimilarManual(artist1Id: Int, artist2Id: Int)

    suspend fun removeSimilar(artist1Id: Int, artist2Id: Int)

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