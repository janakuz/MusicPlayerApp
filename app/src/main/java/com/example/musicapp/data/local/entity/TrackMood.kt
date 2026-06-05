package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_moods",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Mood::class,
            parentColumns = ["id"],
            childColumns = ["moodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value=["trackId", "moodId"], unique = true), Index("moodId")]
)
data class TrackMood(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackId: Int,
    val moodId: Int
)