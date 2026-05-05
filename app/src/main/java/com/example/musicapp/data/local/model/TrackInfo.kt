package com.example.musicapp.data.local.model

data class TrackInfo(
    val trackId: Int,
    val title: String,
    val artistName: String,
    val albumTitle: String,
    val albumArt: String?,
    val trackNum: Int?,
    val duration: Long,
    val fileUri: String,
    val filePath: String,
    val albumId: Int,
    val artistId: Int,

)
