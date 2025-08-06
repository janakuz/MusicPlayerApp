package com.example.musicapp.data.service

import com.example.musicapp.data.dto.ArtistMBResponse
import com.example.musicapp.data.dto.ReleaseSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicbrainzApiService {

    @GET("release/")
    suspend fun findAlbum(
        @Query("query") query: String,
        @Query("fmt") format: String = "json"
    ) : ReleaseSearchResponse

    @GET("artist/{mbid}")
    suspend fun getArtist(
        @Path("mbid") mbid: String,
        @Query("fmt") format: String = "json",
        @Query("inc") inc: String = "url-rels+tags"
    ) : ArtistMBResponse
}