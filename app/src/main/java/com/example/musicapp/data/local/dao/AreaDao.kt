package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.CountryInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("""
    SELECT 
        a.countryCode AS countryCode,
        a.country AS countryName,
        COUNT(DISTINCT a.id) AS artistCount, 
        COUNT(DISTINCT aa.albumId) AS albumCount
    FROM artists a
    LEFT JOIN album_artists aa ON aa.artistId = a.id
    WHERE a.countryCode IS NOT NULL
    GROUP BY a.countryCode, a.country
    ORDER BY
        CASE WHEN :sortBy = 'name' AND :ascending = true THEN LOWER(country) END ASC,
        CASE WHEN :sortBy = 'name' AND :ascending = false THEN LOWER(country) END DESC,
        CASE WHEN :sortBy = 'total' AND :ascending = true THEN artistCount+albumCount END ASC,
        CASE WHEN :sortBy = 'total' AND :ascending = false THEN artistCount+albumCount END DESC,
        CASE WHEN :sortBy = 'artistCount' AND :ascending = true THEN artistCount END ASC,
        CASE WHEN :sortBy = 'artistCount' AND :ascending = false THEN artistCount END DESC,
        CASE WHEN :sortBy = 'albumCount' AND :ascending = true THEN albumCount END ASC,
        CASE WHEN :sortBy = 'albumCount' AND :ascending = false THEN albumCount END DESC
""")
    fun getCountrySummaryDashboard(sortBy: String, ascending: Boolean): Flow<List<CountryInfo>>


    @Query(
        """
            SELECT *
            FROM artists
            WHERE countryCode = :countryCode
        """
    )
    fun getCountryArtists(countryCode: String): Flow<List<Artist>>

    @Query(
        """
            SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
            FROM albums al
            JOIN album_artists aa ON al.id=aa.albumId
            JOIN artists ar on ar.id=aa.artistId
            WHERE ar.countryCode = :countryCode
            GROUP BY al.id
            """
    )
    fun getCountryAlbums(countryCode: String): Flow<List<AlbumInfo>>

}