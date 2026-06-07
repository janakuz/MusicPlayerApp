package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.musicapp.data.local.entity.ArtistGenre

@Dao
interface ArtistGenreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(link: ArtistGenre)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(genres: List<ArtistGenre>)


    @Query("DELETE FROM artist_genres WHERE artistId = :artistId")
    suspend fun deleteByArtistId(artistId: Int)

    @Transaction
    suspend fun syncGenres(artistId: Int, genreIds: List<Int>) {
        deleteByArtistId(artistId)
        genreIds.forEach { id ->
            insert(ArtistGenre(artistId = artistId, genreId = id))
        }
    }

    @Query(
        "SELECT g.name " +
                "FROM artist_genres ag JOIN genres g ON ag.genreId=g.id " +
                "WHERE ag.artistId=:artistId"
    )
    suspend fun getArtistGenres(artistId: Int): List<String>

}