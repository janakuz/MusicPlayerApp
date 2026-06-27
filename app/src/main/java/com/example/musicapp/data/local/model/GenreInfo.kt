package com.example.musicapp.data.local.model

import androidx.room.Embedded
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.entity.Mood

data class GenreInfo(
    @Embedded val genre: Genre,
    val countArtists: Int,
    val countAlbums: Int,
)

data class MoodInfo(
    @Embedded val mood: Mood,
    val trackCount: Int,
    )