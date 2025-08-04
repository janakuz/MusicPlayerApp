package com.example.musicapp

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LibraryScanner @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val trackRepository: TrackRepository
) {

    suspend fun scanAll(context: Context) {
        val audioEntries = queryMediaStore(context) // returns raw metadata for files

        // group/normalize
        val artists = extractArtists(audioEntries)
 //       artistRepository.insertAll(artists)

        val albums = extractAlbums(audioEntries)
  //      albumRepository.insertAll(albums)

  //      val tracks = buildTracks(audioEntries)
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
                    val year = cursor.getString(yearCol)

                    list += RawAudioEntry(
                        fileUri = contentUri,
                        title = title,
                        artistName = artist,
                        albumTitle = album,
                        duration = duration ?: 0L,
                        trackNumber = trackNum ?: 0,
                        albumArt = null,
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


    private suspend fun extractAlbums(entries: List<RawAudioEntry>): Map<AlbumKey, AlbumInfo> {
        val albums = entries.map {  entry ->
            val title = entry.albumTitle.toString()
            val artist = entry.artistName.toString()
            AlbumKey(title, artist, entry.releaseDate) }.distinct()
        val existing = albumArtistRepository.getAll().firstOrNull() ?: emptyList()
        val existingByKey = existing.associateBy { AlbumKey(it.title.lowercase(), it.artistName.lowercase(), it.releaseDate?.lowercase()) }

        val result = mutableMapOf<AlbumKey, AlbumInfo>()

   //     val toInsert = mutableListOf<Album>()
        val toInsertAlbumArtist = mutableListOf<AlbumArtist>()

        for (album in albums) {
            val key = AlbumKey(
                album.title.lowercase(),
                album.artist.lowercase(),
                album.year?.lowercase()
            )
            val existingAlbum = existingByKey[key]
            if (existingAlbum != null) {
                result[key] = existingAlbum
            } else {
                val new = Album(
                    title = album.title,
                    image = null,
                    duration = 0L,
                    mbId = null,
                    discogsId = null,
                    releaseDate = album.year
                )
                val inserted = albumRepository.insertWithReturn(new).toInt()
                val artistId = artistRepository.getArtistByName(album.artist).id
                toInsertAlbumArtist += AlbumArtist(
                    artistId = artistId,
                    albumId = inserted
                )
            }
        }

        if (toInsertAlbumArtist.isNotEmpty()) {
            albumArtistRepository.insertAll(toInsertAlbumArtist)
        }

        return result
    }

//    fun findCoverImageForTrackFile(trackFilePath: String): String? {
//        val parent = File(trackFilePath).parentFile ?: return null
//        val candidates = listOf("cover.jpg", "folder.jpg", "front.jpg", "album.jpg")
//        for (name in candidates) {
//            val f = File(parent, name)
//            if (f.exists() && f.canRead()) {
//                return f.absolutePath // store this in Album.image
//            }
//        }
//        return null
//    }

//    val possibleCover = findCoverImageForTrackFile(trackFilePath)
//    val album = existingAlbum.copy(
//        image = possibleCover ?: existingAlbum.image // preserve if already set
//    )
//    albumRepo.update(album)

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
