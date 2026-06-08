package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.model.GenreInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(genre: Genre): Long

    @Query("SELECT * FROM genres where name=:name")
    suspend fun getGenreByName(name: String): Genre?

    @Query("SELECT name FROM genres WHERE name LIKE '%' || :searchString || '%'")
    fun findGenre(searchString: String): Flow<List<String>>

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