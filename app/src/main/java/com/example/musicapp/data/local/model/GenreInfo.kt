package com.example.musicapp.data.local.model

import androidx.room.Embedded
import com.example.musicapp.data.local.entity.Genre

data class GenreInfo(
    @Embedded val genre: Genre,
    val countArtists: Int,
    val countAlbums: Int,
)
