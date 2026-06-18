package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.model.GenreInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(genre: Genre): Long

    @Delete
    suspend fun delete(genre: Genre)

    @Update
    suspend fun update(genre: Genre)

    @Query("SELECT * FROM genres where name=:name")
    suspend fun getGenreByName(name: String): Genre?

    @Query("SELECT name FROM genres WHERE name LIKE '%' || :searchString || '%'")
    fun findGenre(searchString: String): Flow<List<String>>

    @Query("""
        DELETE FROM album_genres 
        WHERE genreId = :oldGenreId 
          AND albumId IN (SELECT albumId FROM album_genres WHERE genreId = :newGenreId)
    """)
    suspend fun deleteAlbumGenreConflicts(oldGenreId: Int, newGenreId: Int)

    @Query("UPDATE album_genres SET genreId = :newGenreId WHERE genreId = :oldGenreId")
    suspend fun updateAlbumGenreIds(oldGenreId: Int, newGenreId: Int)

    @Query("""
        DELETE FROM artist_genres 
        WHERE genreId = :oldGenreId 
          AND artistId IN (SELECT artistId FROM artist_genres WHERE genreId = :newGenreId)
    """)
    suspend fun deleteArtistGenreConflicts(oldGenreId: Int, newGenreId: Int)

    @Query("UPDATE artist_genres SET genreId = :newGenreId WHERE genreId = :oldGenreId")
    suspend fun updateArtistGenreIds(oldGenreId: Int, newGenreId: Int)


    @Transaction
    suspend fun mergeGenres(oldGenreId: Int, newGenreId: Int) {
        deleteAlbumGenreConflicts(oldGenreId, newGenreId)
        deleteArtistGenreConflicts(oldGenreId, newGenreId)

        updateAlbumGenreIds(oldGenreId, newGenreId)
        updateArtistGenreIds(oldGenreId, newGenreId)
    }

    @Query("""SELECT g.*, COUNT(DISTINCT arg.artistId) as countArtists, COUNT(DISTINCT ag.albumId) as countAlbums 
            FROM genres g 
            LEFT JOIN album_genres ag ON ag.genreId=g.id 
            LEFT JOIN artist_genres arg ON arg.genreId=g.id 
            GROUP BY g.id
            ORDER BY
                CASE WHEN :sortBy = 'name' AND :ascending = true THEN LOWER(g.name) END ASC,
                CASE WHEN :sortBy = 'name' AND :ascending = false THEN LOWER(g.name) END DESC,
                CASE WHEN :sortBy = 'total' AND :ascending = true THEN countArtists+countAlbums END ASC,
                CASE WHEN :sortBy = 'total' AND :ascending = false THEN countArtists+countAlbums END DESC,
                CASE WHEN :sortBy = 'artistCount' AND :ascending = true THEN countArtists END ASC,
                CASE WHEN :sortBy = 'artistCount' AND :ascending = false THEN countArtists END DESC,
                CASE WHEN :sortBy = 'albumCount' AND :ascending = true THEN countAlbums END ASC,
                CASE WHEN :sortBy = 'albumCount' AND :ascending = false THEN countAlbums END DESC
            """
    )
    fun getAllGenres(sortBy: String, ascending: Boolean): Flow<List<GenreInfo>>

    @Query("SELECT name FROM genres WHERE id=:genreId")
    fun getGenreName(genreId: Int): Flow<String>
}