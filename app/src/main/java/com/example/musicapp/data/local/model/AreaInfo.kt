package com.example.musicapp.data.local.model

data class AreaInfo(
    val areaName: String,
    val areaGid: String,
    val areaType: String,
    val stateName: String? = null,
    val countryName: String? = null,
    val countryCode: String,
    val artistCount: Int,
    val albumCount: Int,
)
