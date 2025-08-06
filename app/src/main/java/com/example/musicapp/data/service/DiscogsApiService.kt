package com.example.musicapp.data.service

import com.example.musicapp.data.dto.ArtistDicogsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscogsApiService {

    @GET("artists/{artistId}")
    suspend fun getImages(@Path("artistId") artistId: String) : ArtistDicogsResponse

}