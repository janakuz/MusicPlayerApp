package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AreaInfo
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.data.local.model.FullArea
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface AreaRepository {

    fun getCountriesDashboardInfo(sortBy: SortOption): Flow<List<CountryInfo>>

    fun getCountryArtistsAndAlbums(countryCode: String): Flow<SearchResult>

    fun findCity(city: String): Flow<List<AreaHierarchy>>

    fun getAreaDashboard(sortBy: SortOption, limit: Int = 20): Flow<List<AreaInfo>>

    fun getArtistsFromArea(gid: String, countryCode: String, type: AreaType): Flow<SearchResult>

    suspend fun getAreaName(gid: String): String

    suspend fun getAreaHierarchy(gid: String): FullArea
}

enum class AreaType {
    CITY, COUNTY, STATE, COUNTRY
}