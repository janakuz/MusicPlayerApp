package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.musicapp.data.local.entity.AlbumGenre
import com.example.musicapp.data.local.model.AlbumInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumGenreDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(link: AlbumGenre)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(genres: List<AlbumGenre>)


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


    @Query(
        """
            SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
            FROM albums al
            JOIN album_artists aa ON al.id=aa.albumId
            JOIN artists ar on ar.id=aa.artistId
            JOIN album_genres ag on al.id=ag.albumId
            WHERE ag.genreId=:genreId
            GROUP BY al.id
            """
    )
    fun getGenreAlbums(genreId: Int): Flow<List<AlbumInfo>>
}

