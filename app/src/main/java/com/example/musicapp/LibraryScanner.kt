package com.example.musicapp

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LibraryScanner @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository
) {

    suspend fun scanAll(context: Context) {
//        val audioEntries = queryMediaStore(context) // returns raw metadata for files

        // group/normalize
        val artists = extractArtists(context)
        artistRepository.insertAllString(artists)

  //      val albums = extractAlbums(audioEntries)
  //      albumRepository.insertAll(albums)

  //      val tracks = buildTracks(audioEntries)
  //      trackRepository.insertAll(tracks)
    }

    // internal helpers: queryMediaStore, extractArtists, extractAlbums, buildTracks...

    suspend fun extractArtists(context: Context): List<String> {
        val artistSet = mutableSetOf<String>()

        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST
        )


        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.ARTIST} ASC"



        withContext(Dispatchers.IO) {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                Log.d("ScanDebug", "test")
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                Log.d("ScanDebug", "test ${cursor.count}")
                while (cursor.moveToNext()) {
                    val artist = cursor.getString(artistColumn)
                    Log.d("ScanDebug", "test $artist")
                    if (!artist.isNullOrBlank()) {
                        artistSet.add(artist)
                    }
                }
            }
        }

        return artistSet.toList()
    }
}
