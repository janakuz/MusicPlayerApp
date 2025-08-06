package com.example.musicapp.data.service

import retrofit2.http.GET
import retrofit2.http.Query

interface MusicbrainzApiService {

    @GET("release/")
    suspend fun findAlbum(@Query("query") query: String,
                  @Query("fmt") format: String = "json") : String
}