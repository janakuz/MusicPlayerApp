package com.example.musicapp.data.remote.service

import com.example.musicapp.data.remote.dto.LRCLibResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LRCLibApiService {

    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String,
        @Query("duration") durationSec: Long,
    ) : LRCLibResponse

    @GET("api/get-cached")
    suspend fun getLyricsCached(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String,
        @Query("duration") durationSec: Long,
    ) : LRCLibResponse

}