package com.example.musicapp.data.remote.service

import com.example.musicapp.data.remote.dto.AudioFeaturesResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface EssentiaApiService {

    @Multipart
    @POST("/analyze")
    suspend fun getAudioFeatures(@Part audioFile: MultipartBody.Part): AudioFeaturesResponse

}