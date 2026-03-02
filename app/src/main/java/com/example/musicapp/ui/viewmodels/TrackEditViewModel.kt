package com.example.musicapp.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.MoodRepository
import com.example.musicapp.data.repository.TrackMoodRepository
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.toInt

@HiltViewModel
class TrackEditViewModel  @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackRepository: TrackRepository,
    private val moodRepository: MoodRepository,
    private val trackMoodRepository: TrackMoodRepository,
    private val metadataRepository: MetadataRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trackId: Int = savedStateHandle.get<String>("trackId")?.toInt()
        ?: throw IllegalStateException("trackId not found in SavedStateHandle")

    private val _uiState = MutableStateFlow(TrackEditUiState())
    val uiState = _uiState.asStateFlow()


    private val _moodQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val moodSuggestions: StateFlow<List<String>> = _moodQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                flowOf(emptyList())
            } else {
                moodRepository.findMood(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var initialArtist: String? = null
    private var initialAlbum: String? = null
    private var initialNumber: String? = ""
    private var initialTitle: String? = ""
    private var initialMoods: List<String> = emptyList()

    val canSave: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges = state.draftTrackNumber != initialNumber || state.title != initialTitle
                || state.draftMoods != initialMoods || state.album != initialAlbum || state.artist != initialArtist
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadAlbumData()
    }

    private fun loadAlbumData() {
        viewModelScope.launch {
            val track = trackRepository.getTrackInfo(trackId).first()
            val moods = trackMoodRepository.getTrackMoods(trackId)
            initialAlbum = track.albumTitle
            initialArtist = track.artistName
            initialNumber = track.trackNum.toString()
            initialTitle = track.title
            initialMoods = moods
            _uiState.update { it.copy(
                title = track.title,
                draftTrackNumber = track.trackNum.toString(),
                artist = track.artistName,
                album = track.albumTitle,
                filePath = getPathFromUri(context, track.fileUri),
                draftMoods = moods
            )
            }

        }

    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }


    fun onAlbumChange(newAlbum: String) {
        _uiState.update { it.copy(album = newAlbum) }
    }


    fun onArtistChange(newArtist: String) {
        _uiState.update { it.copy(artist = newArtist) }
    }


    fun onTrackNumChange(newTrackNum: String) {
        _uiState.update { it.copy(draftTrackNumber = newTrackNum) }
    }


    fun onMoodsChange(newMoods: List<String>) {
        _uiState.update { it.copy(draftMoods = newMoods.distinct()) }
        _moodQuery.value = ""
    }

    fun onMoodQueryChange(newQuery: String){
        _moodQuery.value = newQuery
    }


    fun getPathFromUri(context: Context, uriString: String): String {
        val uri = uriString.toUri()
        if (uri.scheme == "file") return uri.path ?: uriString

        var path = uriString
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            if (cursor.moveToFirst()) {
                path = cursor.getString(columnIndex)
            }
        }
        return path
    }

    fun onSave(onBack: () -> Unit){
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }


            val currentTrack = trackRepository.getTrackById(trackId).first()

            val newTrack = currentTrack.copy(
                title = _uiState.value.title,
                trackNumber = _uiState.value.draftTrackNumber.toInt(),
            )
            trackRepository.update(newTrack)
            trackMoodRepository.updateTrackMoods(trackId, _uiState.value.draftMoods)


            try {

                if (initialAlbum != _uiState.value.album) {
                    val track = trackRepository.getTrackInfo(trackId).first()
                    val currentAlbum = albumRepository.getAlbum(track.albumId).first()
                    val currentAlbumInfo = albumRepository.getByIdFull(track.albumId)
                    val currentArtist =
                        artistRepository.getArtist(currentAlbumInfo.artistId).first()

//                    metadataRepository.updateAlbum(
//                        newAlbumTitle = _uiState.value.album,
//                        oldAlbum = currentAlbum,
//                        newArtistName = _uiState.value.artist,
//                        oldArtist = currentArtist,
//                        newReleaseDate = null,
//                        newAlbumArt = null
//                    )
                } else if (initialArtist != _uiState.value.artist) {
                    val track = trackRepository.getTrackInfo(trackId).first()
                    val currentArtist = artistRepository.getArtist(track.artistId).first()
//                    metadataRepository.updateArtist(
//                        newArtistName = _uiState.value.artist,
//                        oldArtist = currentArtist
//                    )
//
                }
                _uiState.update { it.copy(isSaving = false) }
                onBack()
            }
            catch (e: Exception){
                Log.d("SaveError", "Failed to save: ${e.message}", e)
                _uiState.update { it.copy(isSaving = false) }

            }


        }
    }

    fun resetToOriginal() {
        _uiState.update { it.copy(
            title = initialTitle ?: "",
            draftTrackNumber = initialNumber ?: "",
            draftMoods = initialMoods
        )}
    }
}


data class TrackEditUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val filePath: String = "",
    val draftTrackNumber: String = "",
    val draftMoods: List<String> = emptyList(),
    val isSaving: Boolean = false
)