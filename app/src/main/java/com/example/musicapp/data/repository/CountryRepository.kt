package com.example.musicapp.data.repository

import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface CountryRepository {

    fun getCountriesDashboardInfo(sortBy: SortOption): Flow<List<CountryInfo>>

    fun getCountryArtistsAndAlbums(countryCode: String): Flow<SearchResult>

}