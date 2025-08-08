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
        val audioEntries = queryMediaStore(context)

        val albumsAndArtists = extractAlbumsAndArtists(audioEntries)
        val albums = albumsAndArtists.first

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


        val resultArtists = mutableMapOf<String, Artist>()
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
