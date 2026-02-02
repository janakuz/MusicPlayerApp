package com.example.musicapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.entity.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(albums: List<Album>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(album: Album)

    @Update
    suspend fun update(album: Album)

    @Delete
    suspend fun delete(album: Album)

    @Query("SELECT * FROM albums ORDER BY " +
            "CASE " +
            "WHEN title LIKE 'The %' THEN SUBSTR(title, 5)" +
            "WHEN title LIKE 'A %' THEN SUBSTR(title, 3)" +
            "WHEN title LIKE 'An %' THEN SUBSTR(title, 4)" +
            "WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)" +
            "ELSE title " +
            "END COLLATE NOCASE ASC")
    fun getAllAlbumsByName(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY " +
            "CASE " +
            "WHEN title LIKE 'The %' THEN SUBSTR(title, 5)" +
            "WHEN title LIKE 'A %' THEN SUBSTR(title, 3)" +
            "WHEN title LIKE 'An %' THEN SUBSTR(title, 4)" +
            "WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)" +
            "ELSE title " +
            "END COLLATE NOCASE DESC")
    fun getAllAlbumsByNameDesc(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY releaseDate ASC")
    fun getAllAlbumsByReleaseDate(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY releaseDate DESC")
    fun getAllAlbumsByReleaseDateDesc(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY duration ASC")
    fun getAllAlbumsByDuration(): Flow<List<Album>>

    @Query("SELECT * FROM albums ORDER BY duration DESC")
    fun getAllAlbumsByDurationDesc(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE id=:id")
    fun getAlbum(id: Int): Flow<Album>

    @Query("SELECT * FROM albums")
    suspend fun getAll(): List<Album>

    @Query("SELECT * FROM albums WHERE id=:id")
    suspend fun getById(id: Int): Album

    @Query("SELECT * " +
            "FROM albums where searchKey=:title and " +
            "releaseDate LIKE :year || '%'" +
            "LIMIT 1")
    suspend fun getAlbumByTitleAndYear(title: String, year: String): Album?

    @Query("SELECT * " +
            "FROM albums where searchKey=:title " +
            "LIMIT 1")
    suspend fun getAlbumByTitle(title: String): Album?


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWithReturn(album: Album): Long

    @Query("DELETE FROM albums WHERE id NOT IN " +
            "(SELECT DISTINCT albumId from tracks)")
    suspend fun deleteOrphaned()

}