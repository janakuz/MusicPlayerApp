package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.CountryDao
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CountryRepositoryImpl(
    private val countryDao: CountryDao
) : CountryRepository {
    override fun getCountriesDashboardInfo(sortBy: SortOption): Flow<List<CountryInfo>> {
        return when (sortBy.field) {
            SortField.NAME -> countryDao.getCountrySummaryDashboard("name", sortBy.ascending)
            SortField.TOTAL_COUNT -> countryDao.getCountrySummaryDashboard("total", sortBy.ascending)
            SortField.ARTIST_COUNT -> countryDao.getCountrySummaryDashboard("artistCount", sortBy.ascending)
            SortField.ALBUM_COUNT -> countryDao.getCountrySummaryDashboard("albumCount", sortBy.ascending)
            else -> countryDao.getCountrySummaryDashboard("total", false)
        }
    }

    override fun getCountryArtistsAndAlbums(countryCode: String): Flow<SearchResult> {
        return combine(
            countryDao.getCountryArtists(countryCode),
            countryDao.getCountryAlbums(countryCode)
        ) {
                artists, albums ->
            SearchResult(artists, albums)
        }
    }
}