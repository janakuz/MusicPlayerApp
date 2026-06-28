package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.Mood
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.data.local.model.MoodInfo
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(mood: Mood): Long

    @Query("SELECT * FROM moods where name=:name")
    suspend fun getMoodByName(name: String): Mood?

    @Query("SELECT name FROM moods WHERE name LIKE '%' || :searchString || '%'")
    fun findMood(searchString: String): Flow<List<String>>


    @Query("""SELECT m.*, COUNT(DISTINCT tm.trackId) as trackCount 
            FROM moods m 
            LEFT JOIN track_moods tm ON tm.moodId=m.id 
            GROUP BY m.id
            ORDER BY
                CASE WHEN :sortBy = 'name' AND :ascending = true THEN LOWER(m.name) END ASC,
                CASE WHEN :sortBy = 'name' AND :ascending = false THEN LOWER(m.name) END DESC,
                CASE WHEN :sortBy = 'count' AND :ascending = true THEN trackCount END ASC,
                CASE WHEN :sortBy = 'count' AND :ascending = false THEN trackCount END DESC
            """
    )
    fun getAllMoods(sortBy: String, ascending: Boolean): Flow<List<MoodInfo>>


    @Query("""
        SELECT a.*
        FROM artists a
        JOIN tracks t ON a.id=t.artistId
        JOIN track_moods tm ON t.id=tm.trackId
        WHERE tm.moodId=:moodId
        GROUP BY a.id
        ORDER BY COUNT(DISTINCT trackId) DESC
        LIMIT :limit
    """)
    fun getMoodArtists(moodId: Int, limit: Int = 20): Flow<List<Artist>>


    @Query("""
        SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
        FROM albums al    
        JOIN album_artists aa ON al.id=aa.albumId
        JOIN artists ar on ar.id=aa.artistId
        JOIN tracks t on t.albumId=al.id
        JOIN track_moods tm on tm.trackId=t.id
        WHERE tm.moodId=:moodId
        GROUP BY al.id
        HAVING COUNT(DISTINCT trackId)*1.0/al.numTracks >= :threshold
        ORDER BY COUNT(DISTINCT trackId)*1.0/al.numTracks
        """)
    fun getMoodAlbums(moodId: Int, threshold: Float = 0.8F):Flow<List<AlbumInfo>>

    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, 
        al.image as albumArt, t.trackNumber as trackNum, t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        JOIN track_moods tm ON tm.trackId=t.id
        WHERE tm.moodId=:moodId
    """)
    fun getMoodTracks(moodId: Int): Flow<List<TrackInfo>>

    @Query("SELECT name FROM moods WHERE id=:moodId")
    fun getMoodName(moodId: Int): Flow<String>
}
