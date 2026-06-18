package com.example.musicapp.data.local.dao

import androidx.compose.ui.graphics.Matrix
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.ArtistWithArea
import com.example.musicapp.data.local.model.CountryInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<Artist>>

    @Query("SELECT * FROM artists ORDER BY name DESC")
    fun getAllArtistsDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM artists")
    suspend fun getAll(): List<Artist>

    @Query(
        "SELECT * FROM artists ORDER BY " +
                "CASE " +
                "WHEN name LIKE 'The %' THEN SUBSTR(name, 5)" +
                "WHEN name LIKE 'A %' THEN SUBSTR(name, 3)" +
                "WHEN name LIKE 'An %' THEN SUBSTR(name, 4)" +
                "WHEN name GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(name, 2)" +
                "ELSE name " +
                "END COLLATE NOCASE ASC"
    )
    fun getAllArtistsSortedAsc(): Flow<List<Artist>>

    @Query(
        "SELECT * FROM artists ORDER BY " +
                "CASE " +
                "WHEN name LIKE 'The %' THEN SUBSTR(name, 5)" +
                "WHEN name LIKE 'A %' THEN SUBSTR(name, 3)" +
                "WHEN name LIKE 'An %' THEN SUBSTR(name, 4)" +
                "WHEN name GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(name, 2)" +
                "ELSE name " +
                "END COLLATE NOCASE DESC"
    )
    fun getAllArtistsSortedDesc(): Flow<List<Artist>>


    @Query("SELECT * FROM artists where id=:id")
    fun getArtist(id: Int): Flow<Artist>

    @Query("SELECT a.*, ah.city_name as area_city, ah.county_name as area_county, " +
            "ah.state_name as area_state, ah.country_name as area_country " +
            "FROM artists a " +
            "LEFT JOIN area_hierarchy ah ON a.homeAreaGid=ah.gid " +
            "WHERE a.id=:id")
    fun getArtistWithArea(id: Int): Flow<ArtistWithArea>


    @Query("SELECT * FROM artists where LOWER(searchKey)=LOWER(:name)")
    suspend fun getArtistByName(name: String): List<Artist>

    @Query(
        "SELECT * FROM artists where searchKey=:name " +
                "LIMIT 1"
    )
    suspend fun getSingleArtistByName(name: String): Artist?

    @Query("SELECT * FROM artists where mbId=:mbId")
    suspend fun getArtistByMbid(mbId: String): Artist?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(artists: List<Artist>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: Artist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWithReturn(artist: Artist): Long

    @Update
    suspend fun update(artist: Artist)

    @Delete
    suspend fun delete(artist: Artist)

    @Query("DELETE FROM artists WHERE id=:artistId")
    suspend fun deleteById(artistId: Int)

    @Query(
        "DELETE FROM artists WHERE id NOT IN " +
                "(SELECT DISTINCT artistId from album_artists)"
    )
    suspend fun deleteOrphaned()

    @Query(
        "DELETE FROM artists WHERE id NOT IN " +
                "(SELECT DISTINCT artistId FROM tracks)"
    )
    suspend fun deleteOrphanedTracks()


    @Query(
        "SELECT * FROM artists " +
                "WHERE searchKey LIKE :query OR LOWER(name) LIKE :query " +
                "ORDER BY searchKey ASC"
    )
    fun searchArtists(query: String): Flow<List<Artist>>


    @RawQuery(observedEntities = [Artist::class])
    fun getFilteredArtists(query: SupportSQLiteQuery): Flow<List<Artist>>

    @Query(" SELECT MIN(CAST(SUBSTR(activeStartYear, 1, 4) AS INTEGER)) FROM artists WHERE activeStartYear > 0 and activeStartYear IS NOT NULL")
    fun getMinYear(): Flow<Int>


    @Query("""
    SELECT 
        MAX(
            CASE 
                WHEN activeEndYear IS NULL THEN 2026 
                ELSE CAST(SUBSTR(activeEndYear, 1, 4) AS INTEGER) 
            END
        )
    FROM artists
    """)
    fun getMaxYear(): Flow<Int>

}
