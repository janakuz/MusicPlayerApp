package com.example.musicapp;

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.content.Context;
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.provider.MediaStore;
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.musicapp.data.entity.Artist

import com.example.musicapp.data.repository.ArtistRepository;
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(private val artistRepository: ArtistRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _artistListUiState = MutableStateFlow(ArtistListUiState())
    val artistListUiState: StateFlow<ArtistListUiState> = _artistListUiState.asStateFlow()

    private val _currentArtistUiState = MutableStateFlow(ArtistState())
    val currentArtistUiState: StateFlow<ArtistState> = _currentArtistUiState.asStateFlow()

    init {
        viewModelScope.launch {
            artistRepository.getAllArtists()
                .onStart { _artistListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _artistListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _artistListUiState.update { it.copy(artists = list, isLoading = false, error = null) }
                }
        }
    }

    fun getArtistById(id: Int){
        viewModelScope.launch {
            artistRepository.getArtist(id)
                .collect { artist -> _currentArtistUiState.update { it.copy(artist = artist) } }
        }
    }


//    val artistListUiState: StateFlow<ArtistListUiState> =
//        artistRepository.getAllArtists().map { ArtistListUiState(it) }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
//                initialValue = ArtistListUiState()
//            )


//    fun loadFromStorage(context: Context) {
//        viewModelScope.launch {
//            val names = loadArtistsFromStorage(context)
//            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
//            } else {
//                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//            }
//            Log.d("ScanDebug", "Has permission? $hasPermission")
//            Log.d("ArtistVM", "Found ${names.size} unique artist names: $names")
//            artistRepository.insertAllString(names)
//        }
//    }
//
//    fun triggerScan(context: Context, path: String) {
//        MediaScannerConnection.scanFile(
//            context,
//            arrayOf(path),
//            null,
//            null
//        )
//    }
//
//    suspend fun loadArtistsFromStorage(context: Context): List<String> {
//        val artistSet = mutableSetOf<String>()
//
//        val projection = arrayOf(
//            MediaStore.Audio.Media.ARTIST
//        )
//
//
//
//        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
//        val sortOrder = "${MediaStore.Audio.Media.ARTIST} ASC"
//
//
//
//        withContext(Dispatchers.IO) {
//            context.contentResolver.query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                projection,
//                selection,
//                null,
//                sortOrder
//            )?.use { cursor ->
//                Log.d("ScanDebug", "test")
//                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
//                Log.d("ScanDebug", "test ${cursor.count}")
//                while (cursor.moveToNext()) {
//                    val artist = cursor.getString(artistColumn)
//                    Log.d("ScanDebug", "test $artist")
//                    if (!artist.isNullOrBlank()) {
//                        artistSet.add(artist)
//                    }
//                }
//            }
//        }
//
//        return artistSet.toList()
//    }

}

data class ArtistListUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val error: String? = null)

data class ArtistState(
    val artist: Artist? = null
)
