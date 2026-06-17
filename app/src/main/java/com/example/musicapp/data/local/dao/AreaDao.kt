package com.example.musicapp.data.local.dao

import android.R
import androidx.room.Dao
import androidx.room.Query
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.AreaInfo
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.data.local.model.FullArea
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


    @Query("""
        SELECT *
        FROM area_hierarchy 
        WHERE city_name LIKE :city || '%' AND (:widerArea IS NULL OR
        county_name LIKE :widerArea || '%' OR
        state_name LIKE :widerArea || '%' OR
        country_name LIKE :widerArea || '%'
        ) OR 
        :widerArea IS NULL AND  state_name LIKE :city || '%'

--OR county_name LIKE :city || '%' OR state_name LIKE :city || '%'
    """)
    fun findCity(city: String, widerArea: String? = null): Flow<List<AreaHierarchy>>


    @Query("""
        WITH unrolled_history AS (
        SELECT h.city_name AS areaName, 'City' AS areaType, h.state_name AS stateName, h.country_name AS countryName, a.id AS artistId, h.city as areaGid
        FROM artists a
        INNER JOIN area_hierarchy h ON a.homeAreaGid = h.gid
        WHERE h.city_name IS NOT NULL AND h.city_name != ''
        
        UNION ALL
        
        SELECT h.county_name AS areaName, 'County' AS areaType, h.state_name AS stateName, h.country_name AS countryName, a.id AS artistId, h.county as areaGid
        FROM artists a
        INNER JOIN area_hierarchy h ON a.homeAreaGid = h.gid
        WHERE h.county_name IS NOT NULL AND h.county_name != ''
        
        UNION ALL
        
        SELECT h.state_name AS areaName, 'State/Subdivision' AS areaType, h.state_name AS stateName, h.country_name AS countryName, a.id AS artistId, h.state as areaGid
        FROM artists a
        INNER JOIN area_hierarchy h ON a.homeAreaGid = H.gid
        WHERE h.state_name IS NOT NULL AND h.state_name != ''
        
        UNION ALL
        
        SELECT h.country_name AS areaName, 'Country' AS areaType, NULL AS stateName, h.country_name AS countryName, a.id AS artistId, h.country as areaGid
        FROM artists a
        INNER JOIN area_hierarchy h ON a.homeAreaGid = h.gid
        WHERE h.country_name IS NOT NULL AND h.country_name != ''
    )
        
    SELECT 
        areaName,
        areaGid,
        areaType,
        stateName,
        countryName,
        a.countryCode,
        COUNT(DISTINCT a.id) AS artistCount,
        COUNT(DISTINCT aa.albumId) as albumCount
    FROM artists a
    LEFT JOIN album_artists aa ON aa.artistId = a.id
    INNER JOIN unrolled_history h ON a.id = h.artistId
    GROUP BY areaName
    ORDER BY 
        CASE WHEN :sortBy = 'total' THEN artistCount+albumCount END DESC,
        CASE WHEN :sortBy = 'artistCount' THEN artistCount END DESC,
        CASE WHEN :sortBy = 'albumCount' THEN albumCount END DESC
    LIMIT :limit
""")
    fun getMostRepresentedAreas(sortBy: String, limit: Int = 20): Flow<List<AreaInfo>>


    @Query("""
        SELECT a.*
        FROM artists a
        JOIN area_hierarchy ah on a.homeAreaGid=ah.gid
        WHERE city = :gid
    """)
    fun getArtistsFromCity(gid: String): Flow<List<Artist>>

    @Query("""
        SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
        FROM albums al
        JOIN album_artists aa ON al.id=aa.albumId
        JOIN artists ar on ar.id=aa.artistId
        JOIN area_hierarchy ah on ar.homeAreaGid=ah.gid
        WHERE city = :gid
        GROUP BY al.id

    """)
    fun getCityAlbums(gid: String): Flow<List<AlbumInfo>>


    @Query("""
        SELECT a.*
        FROM artists a
        JOIN area_hierarchy ah on a.homeAreaGid=ah.gid
        WHERE county = :gid
    """)
    fun getArtistsFromCounty(gid: String): Flow<List<Artist>>


    @Query("""
        SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
        FROM albums al
        JOIN album_artists aa ON al.id=aa.albumId
        JOIN artists ar on ar.id=aa.artistId
        JOIN area_hierarchy ah on ar.homeAreaGid=ah.gid
        WHERE county = :gid
        GROUP BY al.id
    """)
    fun getCountyAlbums(gid: String): Flow<List<AlbumInfo>>


    @Query("""
        SELECT a.*
        FROM artists a
        JOIN area_hierarchy ah on a.homeAreaGid=ah.gid
        WHERE state = :gid
    """)
    fun getArtistsFromState(gid: String): Flow<List<Artist>>

    @Query("""
        SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
        FROM albums al
        JOIN album_artists aa ON al.id=aa.albumId
        JOIN artists ar on ar.id=aa.artistId
        JOIN area_hierarchy ah on ar.homeAreaGid=ah.gid
        WHERE state = :gid
        GROUP BY al.id
    """)
    fun getStateAlbums(gid: String): Flow<List<AlbumInfo>>


    @Query("SELECT name FROM area where gid = :gid")
    suspend fun getAreaName(gid: String): String

    @Query("SELECT city_name as city, county_name as county, state_name as state, country_name as country FROM area_hierarchy where gid = :gid")
    suspend fun getAreaHierarchy(gid: String): FullArea


}