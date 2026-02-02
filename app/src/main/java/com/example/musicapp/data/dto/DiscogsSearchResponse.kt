package com.example.musicapp.data.dto


data class DiscogsSearchResponse(
    val results: List<DiscogsResult>
)

data class DiscogsResult(
    val year: String,
    val label: List<String>?,
    val cover_image: String,
    val resource_url: String,
    val title: String
)