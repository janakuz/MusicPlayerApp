package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "sequencer_blocks",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SequencerBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockNumber: Int,
    val blockOrder: Int,
    val trackId: Int,
    )
