package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.model.AlbumInfo
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

    @Query(
        "SELECT * FROM albums ORDER BY " +
                "CASE " +
                "WHEN title LIKE 'The %' THEN SUBSTR(title, 5)" +
                "WHEN title LIKE 'A %' THEN SUBSTR(title, 3)" +
                "WHEN title LIKE 'An %' THEN SUBSTR(title, 4)" +
                "WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)" +
                "ELSE title " +
                "END COLLATE NOCASE ASC"
    )
    fun getAllAlbumsByName(): Flow<List<Album>>

    @Query(
        "SELECT * FROM albums ORDER BY " +
                "CASE " +
                "WHEN title LIKE 'The %' THEN SUBSTR(title, 5)" +
                "WHEN title LIKE 'A %' THEN SUBSTR(title, 3)" +
                "WHEN title LIKE 'An %' THEN SUBSTR(title, 4)" +
                "WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)" +
                "ELSE title " +
                "END COLLATE NOCASE DESC"
    )
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

    @Query(
        "SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks " +
                "FROM albums al " +
                "JOIN album_artists aa ON al.id=aa.albumId " +
                "JOIN artists ar on ar.id=aa.artistId " +
                "WHERE al.id=:id"
    )
    suspend fun getByIdFull(id: Int): List<AlbumInfo>

    @Query(
        "SELECT * " +
                "FROM albums where searchKey=:title and " +
                "releaseDate LIKE :year || '%'" +
                "LIMIT 1"
    )
    suspend fun getAlbumByTitleAndYear(title: String, year: String): Album?

    @Query(
        "SELECT * " +
                "FROM albums where searchKey=:title " +
                "LIMIT 1"
    )
    suspend fun getAlbumByTitle(title: String): Album?


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWithReturn(album: Album): Long

    @Query(
        "DELETE FROM albums WHERE id NOT IN " +
                "(SELECT DISTINCT albumId from tracks)"
    )
    suspend fun deleteOrphaned()

    @Query("DELETE FROM albums WHERE id=:albumId")
    suspend fun deleteById(albumId: Int)

    @Query("SELECT * FROM albums where mbId=:mbId")
    suspend fun getAlbumByMbid(mbId: String): Album?

    @Query(
        "SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks " +
                "FROM albums al " +
                "JOIN album_artists aa ON al.id=aa.albumId " +
                "JOIN artists ar on ar.id=aa.artistId " +
                "WHERE LOWER(al.title) LIKE :query OR al.searchKey LIKE :query " +
                "GROUP BY al.id " +
                "ORDER BY al.searchKey ASC"
    )
    fun searchAlbums(query: String): Flow<List<AlbumInfo>>


    @Query(
        "SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks " +
                "FROM albums al " +
                "JOIN album_artists aa ON al.id=aa.albumId " +
                "JOIN artists ar on ar.id=aa.artistId " +
                "WHERE aa.artistId=:artistId AND (LOWER(al.title) LIKE :query OR al.searchKey LIKE :query) " +
                "ORDER BY al.searchKey ASC"
    )
    fun searchArtistAlbums(query: String, artistId: Int): Flow<List<AlbumInfo>>

    @RawQuery(observedEntities = [Album::class])
    fun getFilteredAlbums(query: SupportSQLiteQuery): Flow<List<AlbumInfo>>

    @Query("SELECT MIN(releaseDate) FROM albums WHERE releaseDate > 0")
    fun getMinYear(): Flow<Int>

    @Query("SELECT MAX(releaseDate) FROM albums WHERE releaseDate > 0")
    fun getMaxYear(): Flow<Int>

    @Query("SELECT DISTINCT label FROM albums WHERE label IS NOT NULL AND label != '' ORDER BY label ASC")
    fun getAllLabels(): Flow<List<String>>

    @Query("SELECT DISTINCT label FROM albums WHERE label LIKE '%' || :searchString || '%'")
    fun findLabel(searchString: String): Flow<List<String>>
}