package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "moods",
    indices = [Index(value = ["name"], unique = true)]
)
data class Mood(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)