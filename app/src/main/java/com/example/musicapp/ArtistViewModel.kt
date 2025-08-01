package com.example.musicapp;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.content.Context;
import android.provider.MediaStore;
import com.example.musicapp.data.entity.Artist

import com.example.musicapp.data.repository.ArtistRepository;
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(private val artistRepository: ArtistRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }


    val allArtists = artistRepository.getAllArtists().asLiveData()


    val artistListUiState: StateFlow<ArtistListUiState> =
        artistRepository.getAllArtists().map { ArtistListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ArtistListUiState()
            )


    fun loadFromStorage(context: Context) {
        viewModelScope.launch {
            val names = loadArtistsFromStorage(context)
            artistRepository.insertAll(names)
        }
    }

    suspend fun loadArtistsFromStorage(context: Context): List<String> {
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
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                while (cursor.moveToNext()) {
                    val artist = cursor.getString(artistColumn)
                    if (!artist.isNullOrBlank()) {
                        artistSet.add(artist)
                    }
                }
            }
        }

        return artistSet.toList()
    }

}

data class ArtistListUiState(val artistList: List<Artist> = listOf())
