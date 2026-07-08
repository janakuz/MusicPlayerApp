package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_lyrics",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index("trackId", unique = true)]
)
data class TrackLyrics(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackId: Int,
    val plainLyrics: String?,
    val syncedLyrics: String?
)
