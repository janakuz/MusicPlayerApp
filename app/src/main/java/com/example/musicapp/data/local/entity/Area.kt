package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "area",
    foreignKeys = [
        ForeignKey(
            entity = AreaType::class,
            parentColumns = ["id"],
            childColumns = ["type"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index("gid", unique = true)]
)
data class Area(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gid: String,
    val name: String?,
    val type: Int,
    )
