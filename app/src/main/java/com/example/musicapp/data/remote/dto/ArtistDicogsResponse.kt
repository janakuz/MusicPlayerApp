package com.example.musicapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ArtistDicogsResponse(
    val id: Int,
    val name: String,
    val profile: String,
    val images: List<DiscogsImage>?
)

data class DiscogsImage(
    val height: Int,
    val width: Int,
    @SerializedName("resource_url") val resourceUrl: String,
    val type: String,
    val uri: String
)
