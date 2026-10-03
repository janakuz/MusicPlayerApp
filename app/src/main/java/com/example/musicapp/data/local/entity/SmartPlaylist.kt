package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_playlists")
data class SmartPlaylist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val filterJson: String,
    val image: String? = null,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
