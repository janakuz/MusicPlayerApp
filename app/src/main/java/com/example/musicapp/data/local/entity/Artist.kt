package com.example.musicapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    indices = [Index(value = ["searchKey"])]
)
data class Artist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val searchKey: String,
    val bio: String? = null,
    val image: String? = null,
    val mbId: String? = null,
    val discogsId: String? = null,
    val lastFmPage: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isEnriched: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val enrichmentAttempted: Boolean = false,
    val country: String? = null,
    val countryCode: String? = null,
    val homeCity: String? = null,
    val currentCity: String? = null,
    val activeStartYear: String? = null,
    val activeEndYear: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isDefunct: Boolean = false
)
//TODO: add option to use home city or current city. add subdivisions. map country from code.
