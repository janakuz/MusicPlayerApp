package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "genres",
    indices = [Index(value = ["name"], unique = true)]
)
data class Genre(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)