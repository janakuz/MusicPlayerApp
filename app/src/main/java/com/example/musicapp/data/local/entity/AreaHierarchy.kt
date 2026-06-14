package com.example.musicapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "area_hierarchy",
    foreignKeys = [
        ForeignKey(
            entity = Area::class,
            parentColumns = ["gid"],
            childColumns = ["gid"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index("gid")]
)
data class AreaHierarchy(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gid: String,
    val city: String?,
    @ColumnInfo(name = "city_name") val cityName: String?,
    val municipality: String?,
    @ColumnInfo(name = "municipality_name") val municipalityName: String?,
    val county: String?,
    @ColumnInfo(name = "county_name") val countyName: String?,
    val state: String?,
    @ColumnInfo(name = "state_name") val stateName: String?,
    val country: String?,
    @ColumnInfo(name = "country_name") val countryName: String?,
    )
