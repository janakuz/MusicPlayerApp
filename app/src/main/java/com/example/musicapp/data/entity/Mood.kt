package com.example.musicapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
class Mood(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)