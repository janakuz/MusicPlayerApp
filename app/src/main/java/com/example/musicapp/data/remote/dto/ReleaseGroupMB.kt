package com.example.musicapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReleaseGroupMB(
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCredit>,
    val tags: List<Tag>?,
)
