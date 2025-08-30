package com.example.musicapp

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Track
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.toString
import kotlin.collections.groupBy

class LibraryScanner @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val trackRepository: TrackRepository,
    private val albumDataService: AlbumDataService,
    private val artistDataService: ArtistDataService
) {


    suspend fun scanAll(context: Context) {
        val audioEntries = queryMediaStore(context)

        val albums = extractAlbumsAndArtists(audioEntries)

        val tracks = buildTracks(audioEntries, albums)
    }


    private suspend fun queryMediaStore(context: Context): List<RawAudioEntry> {
        val list = mutableListOf<RawAudioEntry>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        withContext(Dispatchers.IO) {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    ).toString()
                    val title = cursor.getString(titleCol)
                    val artist = cursor.getString(artistCol)
                    val album = cursor.getString(albumCol)
                    val duration = cursor.getLong(durationCol).takeIf { it > 0 }
                    val trackNum = cursor.getInt(trackCol).takeIf { it > 0 }
                    val filePath = cursor.getString(dataCol)
                    val year = cursor.getString(yearCol)

                    list += RawAudioEntry(
                        fileUri = contentUri,
                        title = title,
                        artistName = artist,
                        albumTitle = album,
                        duration = duration ?: 0L,
                        trackNumber = trackNum ?: 0,
                        albumArt = filePath,
                        releaseDate = year

                    )
                }
            }
        }
        return list
    }

    private suspend fun extractAlbumsAndArtists(entries: List<RawAudioEntry>): Map<AlbumKey, AlbumInfo> {
        val albums = getAlbumKeys(entries)
        val existingAlbums = albumArtistRepository.getAll().firstOrNull() ?: emptyList()
        val existingAlbumsByKey = getExistingAlbumKeys(existingAlbums)

        val resultAlbums = mutableMapOf<AlbumKey, AlbumInfo>()

        val toInsertAlbumArtist = mutableListOf<AlbumArtist>()
        for (album in albums) {
            val key = AlbumKey(
                album.title.lowercase(),
                album.artist.lowercase(),
                album.year?.lowercase()
            )
            val existingAlbum = existingAlbumsByKey[key]
            if (existingAlbum != null) {
                resultAlbums[key] = existingAlbum
            } else {
                val mbAlbum = albumDataService.getMbAlbumData(album)
                var newAlbum = albumDataService.createNewAlbum(mbAlbum, album)
                val inserted = albumDataService.insertNewAlbum(newAlbum).toInt()

                val artists = artistDataService.getArtists(mbAlbum)

                for (artist in artists){
                    toInsertAlbumArtist += AlbumArtist(
                        artistId = artist.second.id,
                        albumId = inserted
                    )
                    resultAlbums[key] = AlbumInfo(
                        albumId = inserted,
                        title = newAlbum.title,
                        releaseDate = newAlbum.releaseDate,
                        artistName = album.artist,
                        artistId = artist.second.id,
                        image = newAlbum.image,
                        duration = newAlbum.duration,
                        label = newAlbum.label
                    )
                }
            }
        }

        if (toInsertAlbumArtist.isNotEmpty()) {
            albumArtistRepository.insertAll(toInsertAlbumArtist)
        }

        return resultAlbums
    }

    private fun getExistingAlbumKeys(existingAlbums: List<AlbumInfo>): Map<AlbumKey, AlbumInfo> {
        val existingAlbumsByKey = existingAlbums.associateBy {
            AlbumKey(
                it.title.lowercase(),
                it.artistName.lowercase(),
                it.releaseDate?.lowercase()
            )
        }
        return existingAlbumsByKey
    }

    private fun getAlbumKeys(entries: List<RawAudioEntry>): List<AlbumKey> {
        val albums = entries.map { entry ->
            val title = entry.albumTitle.toString()
            val artist = entry.artistName.toString()
            AlbumKey(title, artist, entry.releaseDate)
        }.distinct()
        return albums
    }

    private suspend fun buildTracks(
        entries: List<RawAudioEntry>,
        albumMap: Map<AlbumKey, AlbumInfo>
    ) {
        val existingTracks = trackRepository.getAllTracksFull().firstOrNull() ?: emptyList()
        val existingByUri = existingTracks.associateBy { it.fileUri }

        val toInsert = mutableListOf<Track>()
        val toUpdate = mutableListOf<Track>()

        val allAlbumArtists = albumArtistRepository.getAllWithArtistInfo()
        val albumArtistsByAlbum: Map<Int, List<Artist>> = allAlbumArtists
            .groupBy { it.albumId }
            .mapValues { (_, group) -> group.map { it.artist } }


        for (entry in entries) {
            val album = if (entry.albumTitle != null && entry.artistName != null) {
                val key = AlbumKey(entry.albumTitle.lowercase(), entry.artistName.lowercase(), entry.releaseDate)
                albumMap[key]
            } else {
                null
            }


            if (album == null || albumArtistsByAlbum == null) continue

            val possibleArtists: List<Artist> = albumArtistsByAlbum[album.albumId] ?: emptyList()

            if (possibleArtists.isEmpty()) continue
            val artist = possibleArtists.find { it.id == album.artistId }

            if (artist == null) continue



            val existing = existingByUri[entry.fileUri]
            val title = entry.title ?: "Unknown"
            val trackNumber = normalizeTrackNumber(entry.trackNumber) ?: 0
            val duration = entry.duration ?: 0L

            if (existing == null) {
                val newTrack = Track(
                    title = title,
                    albumId = album.albumId,
                    artistId = artist.id,
                    duration = duration,
                    plays = 0,
                    mbId = null,
                    lyrics = null,
                    trackNumber = trackNumber,
                    lastPlayed = null,
                    fileUri = entry.fileUri,
                    valence = null,
                    energy = null,
                    key = null,
                    bpm = null
                )
                toInsert += newTrack
            } else {
                if (existing.title != title ||
                    existing.albumId != album.albumId ||
                    existing.artistId != artist.id ||
                    existing.duration != duration ||
                    existing.trackNumber != trackNumber
                ) {
                    val updatedTrack = existing.copy(
                        title = title,
                        albumId = album.albumId,
                        artistId = artist.id,
                        duration = duration,
                        trackNumber = trackNumber
                    )
                    toUpdate += updatedTrack
                }
            }
        }

        Log.d("ScanTracks", toInsert.joinToString())

        if (toInsert.isNotEmpty())
            trackRepository.insertAll(toInsert)
        for (t in toUpdate)
            trackRepository.update(t)

        for ((album, info) in albumMap) {
            var toUpdate = albumRepository.getAlbum(info.albumId).firstOrNull()
            if (toUpdate != null) {
                val tracks = trackRepository.getTracksInAlbum(info.albumId).firstOrNull() ?: emptyList()
                val total = tracks.sumOf { it.duration }
                val numTracks = tracks.size
                if (toUpdate.duration != total)
                    toUpdate = toUpdate.copy(duration = total)
                if (toUpdate.numTracks != numTracks)
                    toUpdate = toUpdate.copy(numTracks = numTracks)
                albumRepository.update(toUpdate)
            }
        }
    }

    companion object {
        fun normalizeTrackNumber(rawTrackNumber: Int?): Int? {
            if (rawTrackNumber == null) return null
            return if (rawTrackNumber >= 1000) rawTrackNumber % 1000 else rawTrackNumber
        }
    }



}



data class RawAudioEntry(
    val fileUri: String,
    val title: String?,
    val artistName: String?,
    val albumTitle: String?,
    val duration: Long?,
    val trackNumber: Int?,
    val albumArt: String?,
    val releaseDate: String?
)

data class AlbumKey(val title: String, val artist: String, val year: String?)
