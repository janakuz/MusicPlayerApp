package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.ArtistMBResponse
import com.example.musicapp.data.entity.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    fun getAllArtists(): Flow<List<Artist>>

    fun getAllArtistsDesc(): Flow<List<Artist>>

    fun getArtist(id: Int): Flow<Artist>

    suspend fun getArtistByName(name: String): List<Artist>

    suspend fun getArtistByMbid(mbId: String): Artist?

    suspend fun getArtistMusicbrainzInfo(mbid: String): ArtistMBResponse

    suspend fun getArtistImage(discogsId: String): String

    suspend fun getArtistBio(mbid: String): String

    suspend fun insertAll(artists: List<Artist>)

    suspend fun insertAllString(artists: List<String>)

    suspend fun insert(artist: Artist)

    suspend fun insertWithReturn(artist: Artist): Long


    suspend fun update(artist: Artist)

    suspend fun delete(artist: Artist)

}