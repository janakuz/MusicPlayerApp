package com.example.musicapp.data.service

import com.example.musicapp.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface LastfmApiService {

    @GET("2.0/")
    suspend fun findAlbum(
        @Query("method") method: String = "album.getinfo",
        @Query("api_key") apiKey: String = BuildConfig.LASTFM_KEY,
        @Query("artist") artist: String,
        @Query("album") album: String,
        @Query("format") format: String = "json"
    ) : String

}