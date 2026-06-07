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
            JOIN album_genres ag ON ag.genreId=g.id 
            JOIN artist_genres arg ON arg.genreId=g.id 
            GROUP BY g.id
            """
    )
    fun getAllGenres(): Flow<List<GenreInfo>>

    @Query("SELECT name FROM genres WHERE id=:genreId")
    fun getGenreName(genreId: Int): Flow<String>
}