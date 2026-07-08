package com.example.musicapp.data.repository

import android.util.Log
import com.example.musicapp.data.local.dao.AreaDao
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.model.AreaInfo
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.data.local.model.FullArea
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.example.musicapp.data.local.entity.Artist


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

    override fun findCity(city: String): Flow<List<AreaHierarchy>> {
        val tokens = city.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val widerArea = if (tokens.size > 1) tokens[1] else null
        val specificArea = if (tokens.isNotEmpty()) tokens[0] else city

        return areaDao.findCity(specificArea, widerArea)
    }

    override fun getAreaDashboard(sortBy: SortOption, limit: Int): Flow<List<AreaInfo>> {
        return when(sortBy.field) {
            SortField.TOTAL_COUNT -> areaDao.getMostRepresentedAreas("total", limit)
            SortField.ARTIST_COUNT -> areaDao.getMostRepresentedAreas("artistCount", limit)
            SortField.ALBUM_COUNT -> areaDao.getMostRepresentedAreas("albumCount", limit)
            else -> areaDao.getMostRepresentedAreas("total", limit)
        }
    }

    override fun getArtistsAndAlbumsFromArea(
        gid: String,
        countryCode: String,
        type: AreaType
    ): Flow<SearchResult> {
        val artistsFlow =
            when(type){
                AreaType.CITY -> areaDao.getArtistsFromCity(gid)
                AreaType.COUNTY -> areaDao.getArtistsFromCounty(gid)
                AreaType.STATE -> areaDao.getArtistsFromState(gid)
                AreaType.COUNTRY -> areaDao.getCountryArtists(countryCode)
            }


        val albumsFlow =
            when(type){
                AreaType.CITY -> areaDao.getCityAlbums(gid)
                AreaType.COUNTY -> areaDao.getCountyAlbums(gid)
                AreaType.STATE -> areaDao.getStateAlbums(gid)
                AreaType.COUNTRY -> areaDao.getCountyAlbums(countryCode)
            }

        return combine(
            artistsFlow,
            albumsFlow
        ) {
                artists, albums ->
            SearchResult(artists, albums)
        }
    }

    override fun getArtistsFromArea(
        gid: String,
        countryCode: String,
        type: AreaType
    ): Flow<List<Artist>> {
        return when(type){
            AreaType.CITY -> areaDao.getArtistsFromCity(gid)
            AreaType.COUNTY -> areaDao.getArtistsFromCounty(gid)
            AreaType.STATE -> areaDao.getArtistsFromState(gid)
            AreaType.COUNTRY -> areaDao.getCountryArtists(countryCode)
        }

    }

    override suspend fun getAreaName(gid: String): String {
        return areaDao.getAreaName(gid)
    }

    override suspend fun getAreaHierarchy(gid: String): FullArea {
        return areaDao.getAreaHierarchy(gid)
    }
}