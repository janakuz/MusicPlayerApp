package com.example.musicapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_queue",
    foreignKeys = [
        ForeignKey(entity = Track::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
    ],
    indices = [Index("trackId")]
)
data class QueueItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String,
    val trackId: Int,
    val orderIndex: Int,
    val shuffledIndex: Int?
)
