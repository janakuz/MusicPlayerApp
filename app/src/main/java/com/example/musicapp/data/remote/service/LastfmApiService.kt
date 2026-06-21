package com.example.musicapp.data.remote.service

import com.example.musicapp.BuildConfig
import com.example.musicapp.data.remote.dto.ArtistLastfmResponse
import com.example.musicapp.data.remote.dto.SimilarArtistsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastfmApiService {

    @GET("2.0/")
    suspend fun getArtistInfo(
        @Query("method") method: String = "artist.getInfo",
        @Query("api_key") apiKey: String = BuildConfig.LASTFM_KEY,
        @Query("mbid") mbid: String?,
        @Query("artist") artist: String?,
        @Query("format") format: String = "json"
    ): ArtistLastfmResponse


    @GET("2.0/")
    suspend fun getSimilarArtists(
        @Query("method") method: String = "artist.getSimilar",
        @Query("api_key") apiKey: String = BuildConfig.LASTFM_KEY,
        @Query("mbid") mbid: String?,
        @Query("artist") artist: String?,
        @Query("format") format: String = "json"
    ): SimilarArtistsResponse

}