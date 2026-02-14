package com.example.musicapp

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.musicapp.data.dto.ArtistSummary
import com.example.musicapp.data.dto.DiscogsAlbumArtist
import com.example.musicapp.data.dto.DiscogsSearchResponse
import com.example.musicapp.data.dto.ReleaseSearchResponse
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Track
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.math.round

class LocalLibraryScanner@Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val trackRepository: TrackRepository
) {


    suspend fun scanAll(context: Context, onProgress: (Float) -> Unit) {
        val audioEntries = queryMediaStore(context, isManual = true)

//        val albumsAndArtists = extractAlbumsAndArtists(audioEntries) { progress ->
//            onProgress(progress) }
//        val albums = albumsAndArtists.first

        val tracks = buildTracks(context, audioEntries, onProgress)
    }


    suspend fun findChanges(context: Context): Boolean {
        val audioEntries = queryMediaStore(context = context)
        val actualAudioEntries = audioEntries.filter { entry ->
            File(entry.filePath).exists()
        }
        val fileUris = actualAudioEntries.map { it.fileUri }
        val dbUris = trackRepository.getAllUris()

        if (dbUris.isEmpty()) return false

        val toDelete = dbUris - fileUris.toSet()
        val toAdd = fileUris - dbUris.toSet()

        trackRepository.deleteByUri(toDelete)
        albumRepository.deleteOrphaned()
        artistRepository.deleteOrphaned()

        val toAddAudioEntries = audioEntries.filter { toAdd.contains(it.fileUri) }
        buildTracks(
            context,
            entries = toAddAudioEntries,
            onProgressUpdate = null
        )

        return true

    }


    private fun saveArtworkToStorage(context: Context, folder: File, artwork: ByteArray): String? {
        return try {
            val fileName = "cover_${folder.name}.jpg"
            val directory = File(context.filesDir, "covers")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            file.writeBytes(artwork)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getEmbeddedPicture(context: Context, path: String, folder: File): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val artwork = retriever.embeddedPicture
            if (artwork != null) saveArtworkToStorage(context, folder, artwork) else null
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun findAlbumArt(context: Context, fileUri: String): String? {
        val folder = File(fileUri).parentFile ?: return null
        val commonNames = listOf("cover", "folder", "front", "album", "art")
        val extensions = listOf("jpg", "jpeg", "png")

        val files = folder.listFiles() ?: return null

        val bestMatch = files.find { file ->
            val name = file.nameWithoutExtension.lowercase()
            commonNames.contains(name) && extensions.contains(file.extension.lowercase())
        }

        val altMatch = files.find {
            extensions.contains(it.extension.lowercase())
        }?.absolutePath

        return bestMatch?.absolutePath ?: altMatch ?: getEmbeddedPicture(context, fileUri, folder)
    }


    private suspend fun queryMediaStore(context: Context, isManual: Boolean = false): List<RawAudioEntry> {
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

        var foundCount = 0
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
                    foundCount++
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

                    val rawId = cursor.getLong(idCol)
                    val castedId = rawId.toInt()

                    list += RawAudioEntry(
                        fileUri = contentUri,
                        filePath = filePath,
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
        if (isManual) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Scanned $foundCount tracks", Toast.LENGTH_SHORT).show()
            }
        }
        return list
    }

    private suspend fun buildTracks(
        context: Context,
        entries: List<RawAudioEntry>,
        onProgressUpdate: ((Float) -> Unit)?
    ) {
        var done = 0
        val total = entries.size

        val toInsert = mutableListOf<Track>()
        val linkedPairs = mutableSetOf<String>()

        for (entry in entries){
            val artistName = entry.artistName ?: "Unknown"
            val albumTitle = entry.albumTitle ?: "Unknown"
            val releaseYear = entry.releaseDate?.take(4)
            val trackTitle = entry.title ?: "Unknown"
            val trackUri = entry.fileUri


            val existing = trackRepository.getTrackByUri(trackUri)


            if (existing == null){
                val artistId = artistRepository.getOrCreateArtistByName(artistName, artistName.normalizeForMatching())
                var albumId: Int? = null
                albumId = albumRepository.getByTitle(albumTitle, releaseYear)?.id

                if (albumId == null){
                    val albumArt = findAlbumArt(context, entry.filePath)

                    val newAlbum = Album(
                        title = albumTitle,
                        searchKey = albumTitle.normalizeForMatching(),
                        duration = 0,
                        image = albumArt,
                        numTracks = 0,
                        mbId = null,
                        label = null,
                        discogsId = null,
                        releaseDate = entry.releaseDate
                    )

                    albumId = albumRepository.insertWithReturn(newAlbum).toInt()
                }


                val pairKey = "${artistId}_${albumId}"

                if (!linkedPairs.contains(pairKey)){
                    albumArtistRepository.insert(AlbumArtist(albumId = albumId, artistId = artistId))
                    linkedPairs.add(pairKey)
                }

                val newTrack = Track(
                    title = trackTitle,
                    artistId = artistId,
                    albumId = albumId,
                    duration = entry.duration ?: 0L,
                    plays = 0,
                    mbId = null,
                    lyrics = null,
                    trackNumber = normalizeTrackNumber(entry.trackNumber),
                    lastPlayed = null,
                    fileUri = trackUri,
                    valence = null,
                    energy = null,
                    key = null,
                    bpm = null
                )

                toInsert.add(newTrack)

            }

            done++
            if (done % 100 == 0){
                trackRepository.insertAll(toInsert)
                toInsert.clear()
            }

            if (onProgressUpdate != null && (done % 5 == 0 || done == total - 1)) {
                val percent = (done.toFloat() / total) * 100
                onProgressUpdate(percent)
            }

        }

        if (toInsert.isNotEmpty()){
            trackRepository.insertAll(toInsert)
        }

        val allAlbums = albumRepository.getAll()
        for (album in allAlbums){
            var toUpdate = album
            val tracks = trackRepository.getAlbumTracks(album.id)
            val total = tracks.sumOf { it.duration }
            val numTracks = tracks.size
            if (toUpdate.duration != total)
                toUpdate = toUpdate.copy(duration = total)
            if (toUpdate.numTracks != numTracks)
                toUpdate = toUpdate.copy(numTracks = numTracks)
            albumRepository.update(toUpdate)
        }

        if (onProgressUpdate != null) onProgressUpdate(100F)

    }


    private fun normalizeTrackNumber(rawTrackNumber: Int?): Int? {
        if (rawTrackNumber == null) return null
        return if (rawTrackNumber >= 1000) rawTrackNumber % 1000 else rawTrackNumber
    }



}

