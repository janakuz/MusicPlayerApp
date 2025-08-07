package com.example.musicapp

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.musicapp.data.dto.AlbumInfo
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.toString
import kotlin.collections.groupBy

class LibraryScanner @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val trackRepository: TrackRepository
) {


    suspend fun scanAll(context: Context) {
        val audioEntries = queryMediaStore(context) // returns raw metadata for files

        // group/normalize
 //       val artists = extractArtists(audioEntries)
 //       artistRepository.insertAll(artists)

        val albumsAndArtists = extractAlbumsAndArtists(audioEntries)
        val albums = albumsAndArtists.first
        val artists = albumsAndArtists.second
  //      albumRepository.insertAll(albums)

        val tracks = buildTracks(audioEntries, artists, albums)
  //      trackRepository.insertAll(tracks)
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


    private suspend fun extractArtists(entries: List<RawAudioEntry>): Map<String, Artist> {
        val names = entries.mapNotNull { it.artistName?.takeIf { it.isNotBlank() } }.distinct()
        val existing = artistRepository.getAllArtists()
            .firstOrNull() ?: emptyList()
        val existingByName = existing.associateBy { it.name.lowercase() }

        val result = mutableMapOf<String, Artist>()

        val toInsert = mutableListOf<Artist>()
        for (name in names) {
            val key = name.lowercase()
            val existingArtist = existingByName[key]
            if (existingArtist != null) {
                result[name] = existingArtist
            } else {
                val new = Artist(name = name)
                toInsert += new
            }
        }
        if (toInsert.isNotEmpty()) {
            artistRepository.insertAll(toInsert)
        }

        val updated = artistRepository.getAllArtists().firstOrNull() ?: emptyList()
        for (artist in updated) {
            result[artist.name] = artist
        }

        return result
    }


    private suspend fun extractAlbumsAndArtists(entries: List<RawAudioEntry>): Pair<Map<AlbumKey, AlbumInfo>, Map<String, Artist>> {
        val albums = entries.map {  entry ->
            val title = entry.albumTitle.toString()
            val artist = entry.artistName.toString()
            AlbumKey(title, artist, entry.releaseDate) }.distinct()
        val existingAlbums = albumArtistRepository.getAll().firstOrNull() ?: emptyList()
        val existingAlbumsByKey = existingAlbums.associateBy { AlbumKey(it.title.lowercase(), it.artistName.lowercase(), it.releaseDate?.lowercase()) }

//        val names = entries.mapNotNull { it.artistName?.takeIf { it.isNotBlank() } }.distinct()
//        val existingArtists = artistRepository.getAllArtists()
//            .firstOrNull() ?: emptyList()
//        val existingArtistsByName = existingArtists.associateBy { it.name.lowercase() }

        val resultArtists = mutableMapOf<String, Artist>()

        val toInsert = mutableListOf<Artist>()

//
//        val testAlbum = albums[0]
//        val testQuery = "release:${testAlbum.title} artist:${testAlbum.artist} date:${testAlbum.year}"
//
//        val res = albumRepository.findAlbumMB(testQuery)
//        val artistmbid = res.releases[0].artistCredit[0].artist.id
//        delay(1000)
//        val ares = artistRepository.getArtistMusicbrainzInfo(artistmbid)
//        Log.d("TestAPI", ares.id)
//        if (ares.urlRelations != null){
//            Log.d("TestAPI", ares.urlRelations.size.toString())
//            val discogs = ares.urlRelations.find { it.type.equals("discogs") }
//            Log.d("TestAPI", discogs.toString())
//            if (discogs != null && discogs.url != null) {
//                val discogsLink = discogs.url.resource.toString().split("/")
//                val discogsId = discogsLink[discogsLink.size-1]
//                val url = artistRepository.getArtistImage(discogsId)
//                Log.d("TestAPI", url)
//
//            }
//        }

//        val bio = artistRepository.getArtistBio(artistmbid)

//        val albumart = albumRepository.getAlbumArt(res.releases[0].id)


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
                var newAlbum = Album(
                    title = album.title,
                    image = null,
                    duration = 0L,
                    mbId = null,
                    discogsId = null,
                    releaseDate = album.year,
                    label = null,
                    numTracks = 0
                )
                val testQuery = "release:${album.title} artist:${album.artist} date:${album.year}"
                val mbAlbumSearch = albumRepository.findAlbumMB(testQuery)
                val mbAlbum = mbAlbumSearch.releases[0]

                val albumArt = albumRepository.getAlbumArt(mbAlbum.id)

                var labelName = ""
                if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) {
                    labelName = mbAlbum.labelInfo[0].label!!.name
                }
                newAlbum = newAlbum.copy(mbId = mbAlbum.id, image = albumArt, label = labelName)

                val inserted = albumRepository.insertWithReturn(newAlbum).toInt()

                val artistMbid = mbAlbumSearch.releases[0].artistCredit[0].artist.id
                delay(1000)
                val mbArtist = artistRepository.getArtistMusicbrainzInfo(artistMbid)
                val artist = artistRepository.getArtistByMbid(mbArtist.id)

                if (artist != null) {
                    resultArtists[artistMbid] = artist
                    toInsertAlbumArtist += AlbumArtist(
                        artistId = artist.id,
                        albumId = inserted
                    )
                    resultAlbums[key] = AlbumInfo(
                        albumId = inserted,
                        title = newAlbum.title,
                        releaseDate = newAlbum.releaseDate,
                        artistName = album.artist,
                        artistId = artist.id,
                        image = newAlbum.image,
                        duration = newAlbum.duration,
                        label = newAlbum.label
                    )
                } else {
                    var artistImage = ""
                    var discogsId = ""
                    if (mbArtist.urlRelations != null) {
                        val discogs = mbArtist.urlRelations.find { it.type.equals("discogs") }
                        if (discogs != null && discogs.url != null) {
                            val discogsLink = discogs.url.resource.toString().split("/")
                            discogsId = discogsLink[discogsLink.size - 1]
                            artistImage = artistRepository.getArtistImage(discogsId)
                        }

                    }

                    val bio = artistRepository.getArtistBio(artistMbid)

                    val newArtist = Artist(
                        name = album.artist,
                        bio = bio,
                        mbId = artistMbid,
                        image = artistImage,
                        discogsId = discogsId
                    )

                    val insertedArtist = artistRepository.insertWithReturn(newArtist).toInt()


                    resultAlbums[key] = AlbumInfo(
                        albumId = inserted,
                        title = newAlbum.title,
                        releaseDate = newAlbum.releaseDate,
                        artistName = album.artist,
                        artistId = insertedArtist,
                        image = newAlbum.image,
                        duration = newAlbum.duration,
                        label = newAlbum.label
                    )
                    toInsertAlbumArtist += AlbumArtist(
                        artistId = insertedArtist,
                        albumId = inserted
                    )
                }
            }
        }

        if (toInsertAlbumArtist.isNotEmpty()) {
            albumArtistRepository.insertAll(toInsertAlbumArtist)
        }


        return Pair(resultAlbums, resultArtists)
    }

    private suspend fun buildTracks(
        entries: List<RawAudioEntry>,
        artistMap: Map<String, Artist>,
        albumMap: Map<AlbumKey, AlbumInfo>
    ) {
        val existingTracks = trackRepository.getAllTracksFull().firstOrNull() ?: emptyList()
        val existingByUri = existingTracks.associateBy { it.fileUri }

        val toInsert = mutableListOf<Track>()
        val toUpdate = mutableListOf<Track>()

        Log.d("ScanTracks", "before repo call")

        val allAlbumArtists = albumArtistRepository.getAllWithArtistInfo()
        val albumArtistsByAlbum: Map<Int, List<Artist>> = allAlbumArtists
            .groupBy { it.albumId }
            .mapValues { (_, group) -> group.map { it.artist } }

        Log.d("ScanTracks", "after repo call ${entries.size}")
        Log.d("ScanTracks", "${albumMap.keys.joinToString()}")
        Log.d("ScanTracks", "${albumMap.values.joinToString()}")

        for (entry in entries) {
            val album = if (entry.albumTitle != null && entry.artistName != null) {
                val key = AlbumKey(entry.albumTitle.lowercase(), entry.artistName.lowercase(), entry.releaseDate)
                albumMap[key]
            } else {
                null
            }

            Log.d("ScanTracks", "${entry.albumTitle} ${album==null}")

            if (album == null || albumArtistsByAlbum == null) continue

            Log.d("ScanTracks", albumArtistsByAlbum.get(album.albumId)?.get(0)?.name ?: "not found")

            val possibleArtists: List<Artist> = albumArtistsByAlbum[album.albumId] ?: emptyList()

            Log.d("ScanTracks", possibleArtists.size.toString())
 //           val possibleArtists = albumArtistRepository.getAllAlbumArtists(album.albumId)
            if (possibleArtists.isEmpty()) continue
            val artist = possibleArtists.find { it.id == album.artistId }
            Log.d("ScanTracks", "${artist == null}")

            if (artist == null) continue



            val existing = existingByUri[entry.fileUri]
            val title = entry.title ?: "Unknown"
            val trackNumber = normalizeTrackNumber(entry.trackNumber) ?: 0
            val duration = entry.duration ?: 0L

//            Log.d("ImageScan", "albumartfile ${entry.albumArt != null}")

//            if (album.image == null && entry.albumArt != null){
//                val possibleCover = findCoverImageForTrackFile(entry.albumArt)
//                val fullAlbum = albumRepository.getAlbum(album.albumId).firstOrNull()
//                if (fullAlbum != null) {
//                    val updatedAlbum = fullAlbum.copy(
//                        image = possibleCover ?: album.image // preserve if already set
//                    )
//
//                    albumRepository.update(updatedAlbum)
//                }
//            }

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

    fun findCoverImageForTrackFile(trackFilePath: String): String? {
        val parentDir = File(trackFilePath).parentFile ?: return null
        Log.d("ImageScan", trackFilePath)
        Log.d("ImageScan", parentDir.name)
        val imageExtensions = listOf("jpg", "jpeg", "png")

        Log.d("ImageScan", parentDir.listFiles().joinToString())


//        MediaScannerConnection.scanFile(
//            context,
//            arrayOf(parentDir.absolutePath),
//            null
//        ) { path, uri ->
//            Log.d("ImageScan", "Scanned $path: $uri")
//        }

        val possibleCovers = parentDir.listFiles()?.filter { file ->
            imageExtensions.any { ext -> file.name.endsWith(".$ext", ignoreCase = true) }
        } ?: emptyList()

        Log.d("ImageScan", possibleCovers.map { file -> file.name }.joinToString())

        val prioritizedCovers = possibleCovers.sortedBy { file ->
            when {
                file.name.contains("cover", ignoreCase = true) -> 0
                file.name.contains("folder", ignoreCase = true) -> 1
                else -> 2
            }
        }
        val chosenCover = prioritizedCovers.firstOrNull()

        Log.d("ImageScan", "$chosenCover.name")

        if (chosenCover != null && chosenCover.canRead()) {
            return chosenCover.absolutePath // store this in Album.image
        }

        return null
    }

    fun normalizeTrackNumber(rawTrackNumber: Int?): Int? {
        if (rawTrackNumber == null) return null
        return if (rawTrackNumber >= 1000) rawTrackNumber % 1000 else rawTrackNumber
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
