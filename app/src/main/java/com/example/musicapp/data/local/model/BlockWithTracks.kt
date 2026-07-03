package com.example.musicapp.data.local.model

import androidx.room.Embedded

data class BlockWithTracks(
    val id: Int,
    val blockNumber: Int,
    val tracks: List<SequencerTrack>
)

data class BlockWithTrackInfo(
    val id: Int,
    val blockNumber: Int,
    @Embedded val trackInfo: TrackInfo
)

data class SequencerTrack(
    @Embedded val trackInfo: TrackInfo,
    val sequencerId: Int,
)