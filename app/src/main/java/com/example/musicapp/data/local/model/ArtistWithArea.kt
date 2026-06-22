package com.example.musicapp.data.local.model

import androidx.room.Embedded
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist

data class ArtistWithArea(
    @Embedded val artist: Artist,
    @Embedded("area_") val area: AreaHierarchy,
)

data class FullArea(
    val city: String?,
    val county: String?,
    val state: String?,
    val country: String?
)
