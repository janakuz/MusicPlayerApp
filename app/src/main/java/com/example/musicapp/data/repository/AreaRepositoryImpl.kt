package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.AreaDao
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AreaRepositoryImpl(
    private val areaDao: AreaDao
) : AreaRepository {
    override fun getCountriesDashboardInfo(sortBy: SortOption): Flow<List<CountryInfo>> {
        return when (sortBy.field) {
            SortField.NAME -> areaDao.getCountrySummaryDashboard("name", sortBy.ascending)
            SortField.TOTAL_COUNT -> areaDao.getCountrySummaryDashboard("total", sortBy.ascending)
            SortField.ARTIST_COUNT -> areaDao.getCountrySummaryDashboard("artistCount", sortBy.ascending)
            SortField.ALBUM_COUNT -> areaDao.getCountrySummaryDashboard("albumCount", sortBy.ascending)
            else -> areaDao.getCountrySummaryDashboard("total", false)
        }
    }

    override fun getCountryArtistsAndAlbums(countryCode: String): Flow<SearchResult> {
        return combine(
            areaDao.getCountryArtists(countryCode),
            areaDao.getCountryAlbums(countryCode)
        ) {
                artists, albums ->
            SearchResult(artists, albums)
        }
    }
}