package com.example.musicapp

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.dto.ArtistDicogsResponse
import com.example.musicapp.data.dto.ArtistMBResponse
import com.example.musicapp.data.dto.DiscogsSearchResponse
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

class LegacyLibraryScanner @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val trackRepository: TrackRepository
) {


    suspend fun scanAll(context: Context, onProgress: (Int) -> Unit) {
        val audioEntries = queryMediaStore(context)

        val albumsAndArtists = extractAlbumsAndArtists(audioEntries) { progress ->
            onProgress(progress) }
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

                    list += RawAudioEntry(
                        fileUri = contentUri,
                        title = title,
                        artistName = artist,
                        albumTitle = album,
                        duration = duration ?: 0L,
                        trackNumber = trackNum ?: 0,
                        albumArt = filePath,
                        releaseDate = year,
                        filePath = filePath
                    )
                }
            }
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Scanned $foundCount tracks", Toast.LENGTH_SHORT).show()
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
                val new = Artist(name = name, searchKey = name.normalizeForMatching())
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


    private suspend fun extractAlbumsAndArtists(
        entries: List<RawAudioEntry>,
        onProgressUpdate: (Int) -> Unit): Pair<Map<AlbumKey, AlbumInfo>, Map<String, Artist>> {
        val albums = entries.map {  entry ->
            val title = entry.albumTitle.toString()
            val artist = entry.artistName.toString()
            AlbumKey(title, artist, entry.releaseDate) }.distinct()
        val existingAlbums = albumArtistRepository.getAll().firstOrNull() ?: emptyList()
        val existingAlbumsByKey = existingAlbums.associateBy { AlbumKey(it.title.lowercase(), it.artistName.lowercase(), it.releaseDate?.lowercase()) }

        val total = albums.size
        var current = 0

        val resultArtists = mutableMapOf<String, Artist>()
        val resultAlbums = mutableMapOf<AlbumKey, AlbumInfo>()

        val toInsertAlbumArtist = mutableListOf<AlbumArtist>()
        for (album in albums) {
            val key = AlbumKey(
                album.title.lowercase(),
                album.artist.lowercase(),
                album.year?.lowercase()
            )

            Log.d("album", album.title)

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
                    numTracks = 0,
                    searchKey = album.title.normalizeForMatching()
                )
                val mbQuery = "release:${album.title} artist:${album.artist} date:${album.year}"
                val mbAlbumSearch = albumRepository.findAlbumMB(mbQuery)
                var discogsResponse: DiscogsSearchResponse? = null

                if (mbAlbumSearch != null && mbAlbumSearch.releases[0].title.lowercase().normalizeForMatching()==album.title.lowercase().normalizeForMatching()) {

                    Log.d("album MB", mbAlbumSearch.releases.joinToString())

                    val mbAlbum = mbAlbumSearch.releases[0]

                    Log.d("album MB", mbAlbum.id)

                    val albumArt = albumRepository.getAlbumArt(mbAlbum.id)

                    Log.d("album MB", albumArt.toString())

                    var labelName = ""
                    if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) {
                        labelName = mbAlbum.labelInfo[0].label!!.name
                    }
                    newAlbum = newAlbum.copy(mbId = mbAlbum.id, image = albumArt, label = labelName)

                    if (newAlbum.releaseDate==null){
                        newAlbum = newAlbum.copy(releaseDate = mbAlbum.date)
                    }
                }

                else {
                    discogsResponse = albumRepository.findAlbumDiscogs(album.artist, album.title, album.year?.take(4))
                    if (discogsResponse != null) {
                        val albumArt = discogsResponse.results[0].cover_image
                        var labelName = ""
                        if (discogsResponse.results[0].label != null && discogsResponse.results[0].label?.isNotEmpty() == true) {
                            labelName = discogsResponse.results[0].label?.get(0) ?: ""
                        }
                        newAlbum = newAlbum.copy(image = albumArt, label = labelName)
                        if (newAlbum.releaseDate==null){
                            newAlbum = newAlbum.copy(releaseDate = discogsResponse.results[0].year)
                        }

                    }


                }


                Log.d("album before insert", newAlbum.title)
                Log.d("album before insert", newAlbum.image.toString())

                val inserted = albumRepository.insertWithReturn(newAlbum).toInt()

                val artistMbid = mbAlbumSearch?.releases[0]?.artistCredit[0]?.artist?.id
                var mbArtist: ArtistMBResponse? = null
                var discogsArtist: ArtistDicogsResponse? = null
                var artist: Artist? = null

                if (artistMbid != null && mbAlbumSearch.releases[0].artistCredit[0].artist.name.lowercase().normalizeForMatching()==album.artist.lowercase().normalizeForMatching()) {
                    delay(1000)
                    mbArtist = artistRepository.getArtistMusicbrainzInfo(artistMbid)
                    artist = artistRepository.getArtistByMbid(mbArtist.id)

                }
                else if (discogsResponse != null) {
                    delay(1000)
                    val discogsAlbum = albumRepository.getAlbumDiscogs(discogsResponse.results[0].resource_url)
                    if (discogsAlbum != null && discogsAlbum.artists.isNotEmpty()) {
                        delay(1000)
                        discogsArtist =
                            artistRepository.getArtistDiscogsInfo(discogsAlbum.artists[0].id)
                    }
                }
                Log.d("artist", album.artist + " " + artistMbid)

                if (artist != null) {
                    resultArtists[artistMbid.toString()] = artist
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

                    val albumArtist = AlbumArtist(
                        artistId = artist.id,
                        albumId = inserted
                    )
                    albumArtistRepository.insert(albumArtist)

                } else {
                    var artistImage = ""
                    var discogsId = ""
                    if (mbArtist != null && mbArtist.urlRelations != null) {
                        val discogs = mbArtist.urlRelations.find { it.type.equals("discogs") }
                        if (discogs != null && discogs.url != null) {
                            val discogsLink = discogs.url.resource.toString().split("/")
                            discogsId = discogsLink[discogsLink.size - 1]
                            val artistInfo = artistRepository.getArtistDiscogsInfo(discogsId)
                            if (artistInfo != null && artistInfo.images != null && artistInfo.images.isNotEmpty()){
                                artistImage = artistInfo.images.get(0).resourceUrl
                            }
                        }
                    }
                    else if (discogsArtist != null){
                        discogsId = discogsArtist.id.toString()

                        if (discogsArtist.images != null && discogsArtist.images.isNotEmpty()) {
                            artistImage = discogsArtist.images.get(0).resourceUrl
                        }
                    }

                    val bio = artistRepository.getArtistBio(artistMbid,album.artist)

                    Log.d("artsit bio", bio)

                    val newArtist = Artist(
                        name = album.artist,
                        bio = bio,
                        mbId = artistMbid,
                        image = artistImage,
                        discogsId = discogsId,
                        searchKey = album.artist.normalizeForMatching()
                    )

                    Log.d("artist before insert", newArtist.name)
                    Log.d("artist before insert", newArtist.image.toString())

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
                    val albumArtist = AlbumArtist(
                        artistId = insertedArtist,
                        albumId = inserted
                    )

                    albumArtistRepository.insert(albumArtist)
                }
            }

            current++
            if (current % 5 == 0 || current == total - 1) {
                val percent = ((current.toFloat() / total) * 100).toInt()
                onProgressUpdate(percent)
            }

        }



        Log.d("album artists", "here")

//        if (toInsertAlbumArtist.isNotEmpty()) {
//            albumArtistRepository.insertAll(toInsertAlbumArtist)
//        }

        Log.d("album artists", "here2")

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

            Log.d("tracks artist", artist?.name ?: "none")

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

        Log.d("tracks before insert", "here")

        if (toInsert.isNotEmpty())
            trackRepository.insertAll(toInsert)
        for (t in toUpdate)
            trackRepository.update(t)

        Log.d("tracks after insert", "here")


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

        Log.d("tracks end", "here")

    }

    fun normalizeTrackNumber(rawTrackNumber: Int?): Int? {
        if (rawTrackNumber == null) return null
        return if (rawTrackNumber >= 1000) rawTrackNumber % 1000 else rawTrackNumber
    }

    fun String.normalizeForMatching(): String {
        return this.lowercase()
            .replace("&", "and")
            .replace(Regex("\\((.*?)\\)"), "")
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }



}



data class RawAudioEntry(
    val fileUri: String,
    val filePath: String,
    val title: String?,
    val artistName: String?,
    val albumTitle: String?,
    val duration: Long?,
    val trackNumber: Int?,
    val albumArt: String?,
    val releaseDate: String?
)

data class AlbumKey(val title: String, val artist: String, val year: String?)
