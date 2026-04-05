package com.example.musicapp.data.service

import com.example.musicapp.data.dto.AlbumArtImage
import retrofit2.http.GET
import retrofit2.http.Path

interface CoverArtArchiveApiService {

    @GET("release-group/{mbid}")
    suspend fun getAlbumImage(
        @Path("mbid") mbid: String
    ) : AlbumArtImage
}