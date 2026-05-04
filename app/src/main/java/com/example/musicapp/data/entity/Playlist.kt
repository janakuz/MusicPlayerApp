package com.example.musicapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val image: String? = null,
    val description: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isSmart: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long
)