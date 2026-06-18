package com.example.musicapp.data.repository

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.musicapp.data.local.dao.AlbumDao
import com.example.musicapp.data.local.dao.ArtistDao
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.ui.HomeScreen
import com.example.musicapp.util.normalizeGenre
import kotlinx.coroutines.flow.Flow


class FilterRepositoryImpl(
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
) : FilterRepository {


    private fun buildLibraryQuery(filter: LibraryFilter, type: FilterSection): SimpleSQLiteQuery {
        val conditions = mutableListOf<String>()
        val bindArgs = mutableListOf<Any>()


        if (filter.dateRanges.isNotEmpty()) {
            val rangeClauses = filter.dateRanges.map { range ->
                bindArgs.add(range.first)
                bindArgs.add(range.last)
                "(al.releaseDate BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (filter.selectedLabels.isNotEmpty()) {
            val labels = filter.selectedLabels.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedLabels)
            conditions.add("al.label in ($labels)")
        }

        if (filter.selectedGenres.isNotEmpty()) {
            val genres = filter.selectedGenres.map { it.normalizeGenre() }.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedGenres)
            conditions.add("g.name in ($genres)")
        }

        if (filter.selectedCountries.isNotEmpty()) {
            val countries = filter.selectedCountries.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedCountries)
            conditions.add("ar.countryCode in ($countries)")
        }

        Log.d("area filter", filter.selectedAreas.joinToString())
        if (filter.selectedAreas.isNotEmpty()){
            val cityGids = mutableListOf<String>()
            val countyGids = mutableListOf<String>()
            val stateGids = mutableListOf<String>()
            val countryGids = mutableListOf<String>()

            filter.selectedAreas.forEach { area ->
                when {
                    area.city != null && area.city == area.county -> countyGids.add(area.county)
                    area.city != null -> cityGids.add(area.city)
                    area.county != null -> countyGids.add(area.county)
                    area.state != null -> stateGids.add(area.state)
                    area.country != null -> countryGids.add(area.country)
                }
            }

            val areaConditions = mutableListOf<String>()

            if (cityGids.isNotEmpty()) {
                val placeholders = cityGids.joinToString(",") { "?" }
                bindArgs.addAll(cityGids)
                areaConditions.add("ah.city IN ($placeholders)")
            }

            if (countyGids.isNotEmpty()) {
                val placeholders = countyGids.joinToString(",") { "?" }
                bindArgs.addAll(countyGids)
                areaConditions.add("ah.county IN ($placeholders)")
            }

            if (stateGids.isNotEmpty()) {
                val placeholders = stateGids.joinToString(",") { "?" }
                bindArgs.addAll(stateGids)
                areaConditions.add("ah.state IN ($placeholders)")
            }

            if (countryGids.isNotEmpty()) {
                val placeholders = countryGids.joinToString(",") { "?" }
                bindArgs.addAll(countryGids)
                areaConditions.add("ah.country IN ($placeholders)")
            }

            Log.d("area filter", "(${areaConditions.joinToString(" OR ")})")

            conditions.add("(${areaConditions.joinToString(" OR ")})")
        }

        when (filter.defunctStatus) {
            DefunctFilterStatus.ALL -> {}
            DefunctFilterStatus.ACTIVE -> {conditions.add("ar.isDefunct = false")}
            DefunctFilterStatus.DEFUNCT -> {conditions.add("ar.isDefunct = true")}
        }


        if (filter.artistFormedRanges.isNotEmpty()) {
            val rangeClauses = filter.artistFormedRanges.map { range ->
                bindArgs.add(range.first)
                bindArgs.add(range.last)
                "(ar.activeStartYear BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }


        if (filter.artistEndedRanges.isNotEmpty()) {
            val rangeClauses = filter.artistEndedRanges.map { range ->
                bindArgs.add(range.first)
                bindArgs.add(range.last)
                "(ar.activeEndYear BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }


        val baseQuery =
            when (type) {
             FilterSection.ALBUMS ->
                 """
                     SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
                     FROM albums al
                     JOIN album_artists aa ON al.id=aa.albumId
                     JOIN artists ar ON ar.id=aa.artistId
                     LEFT JOIN album_genres ag ON ag.albumId=al.id
                     LEFT JOIN genres g ON ag.genreId=g.id
                 """.trimIndent()
             FilterSection.ARTISTS ->
                """
                    SELECT ar.*
                    FROM artists ar
                    LEFT JOIN artist_genres ag on ag.artistId=ar.id
                    LEFT JOIN area_hierarchy ah on ah.gid=ar.homeAreaGid
                    LEFT JOIN genres g on ag.genreId=g.id
                """.trimIndent()
                }
        val joiner = if (filter.logic == FilterLogic.AND) " AND " else " OR "
        val sql = baseQuery + if (conditions.isNotEmpty()) {
            " WHERE ${conditions.joinToString(joiner)}"
        } else ""
        val sqlGrouped = if (type == FilterSection.ALBUMS) "$sql GROUP BY al.id" else "$sql GROUP BY ar.id"

        return SimpleSQLiteQuery(sqlGrouped, bindArgs.toTypedArray())
    }

    override fun getFilteredAlbums(filter: LibraryFilter): Flow<List<AlbumInfo>> {
        val rawQuery = buildLibraryQuery(filter, FilterSection.ALBUMS)

        return albumDao.getFilteredAlbums(rawQuery)
    }

    override fun getFilteredArtists(filter: LibraryFilter): Flow<List<Artist>> {
        val rawQuery = buildLibraryQuery(filter, FilterSection.ARTISTS)

        return artistDao.getFilteredArtists(rawQuery)
    }

    override fun getMinYear(): Flow<Int> {
        return albumDao.getMinYear()
    }

    override fun getMaxYear(): Flow<Int> {
        return albumDao.getMaxYear()
    }

    override fun getMinYearArtists(): Flow<Int> {
        return artistDao.getMinYear()
    }

    override fun getMaxYearArtists(): Flow<Int> {
        return artistDao.getMaxYear()
    }

    override fun getAllLabels(): Flow<List<String>> {
        return albumDao.getAllLabels()
    }

    override fun findLabel(query: String): Flow<List<String>> {
        return albumDao.findLabel(query)
    }
}