package com.example.musicapp.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.musicapp.data.local.dao.AlbumDao
import com.example.musicapp.data.local.model.AlbumInfo
import kotlinx.coroutines.flow.Flow

class FilterRepositoryImpl(
    private val albumDao: AlbumDao
) : FilterRepository {

    private fun buildLibraryQuery(filter: LibraryFilter): SimpleSQLiteQuery {
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


        val baseQuery =
            "SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, " +
                    "ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks " +
                    "FROM albums al " +
                    "JOIN album_artists aa ON al.id=aa.albumId " +
                    "JOIN artists ar on ar.id=aa.artistId "
        val joiner = if (filter.logic == FilterLogic.AND) " AND " else " OR "
        val sql = baseQuery + if (conditions.isNotEmpty()) {
            " WHERE ${conditions.joinToString(joiner)}"
        } else ""
        val sqlGrouped = "$sql GROUP BY al.id"

        return SimpleSQLiteQuery(sqlGrouped, bindArgs.toTypedArray())
    }

    override fun getFilteredAlbums(filter: LibraryFilter): Flow<List<AlbumInfo>> {
        val rawQuery = buildLibraryQuery(filter)

        return albumDao.getFilteredAlbums(rawQuery)
    }

    override fun getMinYear(): Flow<Int> {
        return albumDao.getMinYear()
    }

    override fun getMaxYear(): Flow<Int> {
        return albumDao.getMaxYear()
    }

    override fun getAllLabels(): Flow<List<String>> {
        return albumDao.getAllLabels()
    }

    override fun findLabel(query: String): Flow<List<String>> {
        return albumDao.findLabel(query)
    }
}