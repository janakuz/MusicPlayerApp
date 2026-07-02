package com.example.musicapp.data.local.model

import androidx.room.Embedded

data class CompatibleTrack(
    @Embedded val track: TrackInfo,
    val matchDescription: String,
    val tempoDifference: Int,
    val loudnessDifference: Float,
    val currentBlock: Int,
    val halfTime: Boolean,
    val doubleTime: Boolean,
    val wrongKey: Boolean = false,
    val wrongBPM: Boolean = false,
    val wrongLoudness: Boolean = false,
    val inMultiTrackBlock: Boolean,
)