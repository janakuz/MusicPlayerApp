package com.example.musicapp.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.musicapp.data.local.dao.PlaylistDao
import com.example.musicapp.data.local.dao.PlaylistTracksDao
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.PlaylistTracks
import com.example.musicapp.data.local.model.PlaylistTrack
import com.example.musicapp.data.local.model.PlaylistWithArt
import com.example.musicapp.data.local.model.PlaylistWithStats
import com.example.musicapp.service.ImageStorageManager
import com.example.musicapp.service.ImageTarget
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.components.characteristics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val db: AppDatabase,
    private val imageStorageManager: ImageStorageManager,
    @ApplicationContext private val context: Context
) : PlaylistRepository {

    private fun characteristicSort(characteristic: String): String{
        return "AVG($characteristic)"
    }

    private fun getPlaylists(
        orderBy: SortOption
    ): Flow<List<PlaylistWithStats>> {

        val sqlOrderBy =
            when (orderBy.field) {
                SortField.NAME -> "LOWER(p.name)"
                SortField.DURATION -> "playlistDuration"
                SortField.NUMBER_OF_TRACKS -> "trackCount"
                SortField.DATE_CREATED -> "createdAt"
                SortField.DATE_UPDATED -> "lastUpdated"
                SortField.ENGAGEMENT -> characteristicSort("engagement")
                SortField.APPROACHABILITY -> characteristicSort("approachability")
                SortField.DANCEABILITY -> characteristicSort("danceability")
                SortField.MOOD_AGGRESSIVE -> characteristicSort("moodAggressive")
                SortField.MOOD_RELAXED -> characteristicSort("moodRelaxed")
                SortField.MOOD_SAD -> characteristicSort("moodSad")
                SortField.MOOD_HAPPY -> characteristicSort("moodHappy")
                SortField.MOOD_PARTY -> characteristicSort("moodParty")
                SortField.BPM -> characteristicSort("bpm")
                SortField.LOUDNESS -> characteristicSort("loudness")
                SortField.DYNAMIC_RANGE -> characteristicSort("dynamicComplexity")
                else -> "p.name"
            }

        val direction = if (orderBy.ascending) "ASC" else "DESC"

        val sqlString = """
                         SELECT p.*, SUM(t.duration) AS playlistDuration, COUNT(pt.id) as trackCount
                         FROM playlists p
                         LEFT JOIN playlist_tracks pt on p.id=pt.playlistId
                         LEFT JOIN tracks t on pt.trackId=t.id
                         GROUP BY p.id
                         ORDER BY $sqlOrderBy $direction 
                         ${if (orderBy.field in characteristics) "NULLS LAST" else ""}
                        """.trimIndent()

        val query = SimpleSQLiteQuery(sqlString)

        return playlistDao.getPlaylistsSorted(query)
    }


    private fun addImages(flow: Flow<List<PlaylistWithStats>>): Flow<List<PlaylistWithArt>> {
        return flow.map { playlists ->
            playlists.map { playlistStats ->
                val images = getPlaylistImages(playlistStats.playlist.id)
                PlaylistWithArt(
                    stats = playlistStats,
                    top4Images = images
                )
            }
        }
    }

    override fun getAllPlaylists(sortBy: SortOption): Flow<List<PlaylistWithArt>> {
        return addImages(getPlaylists(sortBy))
    }

    override fun getPlaylist(id: Int): Flow<Playlist> {
        return playlistDao.getPlaylist(id)
    }

    override suspend fun getPlaylistById(id: Int): Playlist {
        return playlistDao.getPlaylistById(id)
    }

    override suspend fun insert(playlist: Playlist): Long {
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun update(playlist: Playlist) {
        val withUpdateTimestamp = playlist.copy(lastUpdated = System.currentTimeMillis())
        playlistDao.updatePlaylist(withUpdateTimestamp)
    }

    override suspend fun delete(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    override suspend fun deleteById(playlistId: Int) {
        playlistDao.deleteById(playlistId)
    }

    override fun getArtForCollage(playlistId: Int): Flow<List<String>> {
        return playlistTracksDao.getTop4ImagesForPlaylist(playlistId)
    }

    override fun getPlaylistStats(playlistId: Int): Flow<PlaylistStats> {
        return combine(
            playlistTracksDao.getTop4ImagesForPlaylist(playlistId),
            playlistTracksDao.getTrackCount(playlistId),
            playlistTracksDao.getDuration(playlistId)
        ) { images, tracks, duration ->
            PlaylistStats(
                images = images,
                trackCount = tracks,
                duration = duration
            )
        }
    }

    override suspend fun getPlaylistImages(playlistId: Int): List<String> {
        return playlistTracksDao.getTop4Images(playlistId)
    }


    override suspend fun savePlaylistImage(
        uri: Uri,
        playlistId: Int,
    ): String? {
        return imageStorageManager.saveCustomArtwork(uri, ImageTarget.PLAYLIST, playlistId)
    }

    private fun readFile(context: Context, file: Uri): List<String> {
        val trackTails = mutableListOf<String>()

        context.contentResolver.openInputStream(file)?.use { inputStream ->
            inputStream.bufferedReader().forEachLine { line ->
                if (line.isNotBlank() && !line.startsWith("#")) {
                    val tail = line.split("/").takeLast(2).joinToString("/")
                    trackTails.add(tail.lowercase())
                }
            }
        }

        return trackTails
    }

    private fun getPlaylistName(context: Context, file: Uri): String? {
        var result: String? = null
        if (file.scheme == "content") {
            val cursor = context.contentResolver.query(file, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex).split(".")[0]
                    }
                }
            }
        }
        return result
    }


    override suspend fun importPlaylist(file: Uri) {
        val trackPaths = readFile(context, file)
        val playlistName = getPlaylistName(context, file)

        if (playlistName == null) return

        db.withTransaction {
            val playlistId = playlistDao.insertPlaylist(
                Playlist(
                    name = playlistName,
                    lastUpdated = System.currentTimeMillis()
                )
            ).toInt()
            val playlistTracks = mutableListOf<PlaylistTracks>()

            trackPaths.forEachIndexed { index, pathTail ->
                val track = db.trackDao().findTrackByPath("%$pathTail")
                if (track != null) {
                    playlistTracks.add(
                        PlaylistTracks(
                            playlistId = playlistId,
                            trackId = track.id,
                            position = index,
                            addedAt = System.currentTimeMillis()
                        )
                    )
                }

            }

            playlistTracksDao.insertAll(playlistTracks)
        }
    }

    override fun exportPlaylist(
        uri: Uri,
        tracks: List<PlaylistTrack>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.bufferedWriter().use { writer ->
                writer.write("#EXTM3U\n")
                tracks.forEach { track ->
                    val durationInSec = track.trackInfo.duration / 1000
                    writer.write("#EXTINF:$durationInSec,${track.trackInfo.artistName} - ${track.trackInfo.title}\n")
                    writer.write("${track.trackInfo.filePath}\n")
                }
            }
        }
    }
}