package com.example.musicapp.data.service

import retrofit2.http.GET
import retrofit2.http.Query

interface DiscogsApiService {

    @GET("database/search")
    suspend fun findAlbum(@Query("q") query: String) : String

}