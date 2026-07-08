package com.example.musicapp.data.local.model

import androidx.room.Embedded

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
    val instrumental: Boolean? = null,
    val voice: String? = null,
    val bpm: Int? = null,
    val key: String? = null
    )
