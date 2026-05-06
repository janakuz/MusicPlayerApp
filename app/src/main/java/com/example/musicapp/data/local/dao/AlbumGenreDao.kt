package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.musicapp.data.local.entity.AlbumGenre

@Dao
interface AlbumGenreDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(link: AlbumGenre)

    @Query("DELETE FROM album_genres WHERE albumId = :albumId")
    suspend fun deleteByAlbumId(albumId: Int)

    @Transaction
    suspend fun syncGenres(albumId: Int, genreIds: List<Int>) {
        deleteByAlbumId(albumId)
        genreIds.forEach { id ->
            insert(AlbumGenre(albumId = albumId, genreId = id))
        }
    }

    @Query(
        "SELECT g.name " +
                "FROM album_genres ag JOIN genres g ON ag.genreId=g.id " +
                "WHERE ag.albumId=:albumId"
    )
    suspend fun getAlbumGenres(albumId: Int): List<String>
}

