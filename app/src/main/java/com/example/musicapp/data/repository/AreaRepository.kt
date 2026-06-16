package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface AreaRepository {

    fun getCountriesDashboardInfo(sortBy: SortOption): Flow<List<CountryInfo>>

    fun getCountryArtistsAndAlbums(countryCode: String): Flow<SearchResult>

    fun findCity(city: String): Flow<List<AreaHierarchy>>

}