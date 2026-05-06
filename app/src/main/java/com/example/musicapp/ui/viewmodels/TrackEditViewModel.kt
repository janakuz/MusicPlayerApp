package com.example.musicapp.ui.viewmodels

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
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
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class TrackEditViewModel @Inject constructor(
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

    private val _workflowState =
        MutableStateFlow<AlbumArtistEditUiState>(AlbumArtistEditUiState.Idle)
    val workflowState = _workflowState.asStateFlow()

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
            _uiState.update {
                it.copy(
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

    fun onMoodQueryChange(newQuery: String) {
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


    fun resetName() {
        _uiState.update {
            it.copy(
                artist = initialArtist ?: ""
            )
        }
    }

    fun onArtistSelected(artistResult: ArtistSearchInfo, onBack: () -> Unit) {
        viewModelScope.launch {
            val album = (_workflowState.value as AlbumArtistEditUiState.DisambiguationNeeded).album
            _workflowState.value = AlbumArtistEditUiState.Saving(album)
            val currentTrack = trackRepository.getTrackById(trackId).first()
            val trackInfo = trackRepository.getTrackInfo(trackId).first()
            val currentArtist = artistRepository.getArtist(trackInfo.artistId).first()
            performFinalSave(
                artistResult,
                currentArtist,
                currentTrack,
                onBack
            )

        }
    }

    suspend fun performFinalSave(
        artistResult: ArtistSearchInfo,
        oldArtist: Artist,
        track: Track,
        onBack: () -> Unit
    ) {
        metadataRepository.updateArtist(
            newArtistName = _uiState.value.artist,
            oldArtist = oldArtist,
            mbArtist = artistResult,
            track = track
        )
        _workflowState.value = AlbumArtistEditUiState.Saved
        onBack()
    }

    fun onSave(onBack: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }


            val currentTrack = trackRepository.getTrackById(trackId).first()

            val newTrack = currentTrack.copy(
                title = _uiState.value.title,
                trackNumber = _uiState.value.draftTrackNumber.toInt(),
            )
            trackRepository.update(newTrack)
            trackMoodRepository.updateTrackMoods(trackId, _uiState.value.draftMoods)

            currentTrack.albumId


            if (initialAlbum != _uiState.value.album) {
//                    val track = trackRepository.getTrackInfo(trackId).first()
                val currentAlbum = albumRepository.getAlbum(currentTrack.albumId).first()
                val currentAlbumInfo = albumRepository.getByIdFull(currentTrack.albumId)
                val currentArtist =
                    if (currentAlbumInfo.size == 1) artistRepository.getArtist(currentAlbumInfo[0].artistId)
                        .first() else null

                try {
                    _workflowState.value = AlbumArtistEditUiState.Saving(currentAlbum)
                    metadataRepository.updateAlbum(
                        newAlbumTitle = _uiState.value.album,
                        oldAlbum = currentAlbum,
                        newArtistName = _uiState.value.artist,
                        oldArtist = currentArtist,
                        track = currentTrack
                    )
                    _workflowState.value = AlbumArtistEditUiState.Saved
                    _uiState.update { it.copy(isSaving = false) }
                    onBack()
                } catch (e: SocketTimeoutException) {
                    _workflowState.value =
                        AlbumArtistEditUiState.Error("MusicBrainz is taking too long. Please try again.")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    _workflowState.value =
                        AlbumArtistEditUiState.Error("Network error: ${e.message}")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    Log.d("SaveError", "Failed to save: ${e.message}", e)
                    _uiState.update { it.copy(isSaving = false) }
                }


            }
            if (initialArtist != _uiState.value.artist) {
                val track = trackRepository.getTrackInfo(trackId).first()
                val currentArtist = artistRepository.getArtist(currentTrack.artistId).first()
                try {
                    _workflowState.value = AlbumArtistEditUiState.Saving()

                    val searchResults = artistRepository.findArtistMB(_uiState.value.artist)
                    if (searchResults.isEmpty()) {
                        _workflowState.value = AlbumArtistEditUiState.Error("No artist found")
                    } else if (searchResults.size > 1) {
                        _workflowState.value =
                            AlbumArtistEditUiState.DisambiguationNeeded(searchResults)
                        return@launch
                    } else {
                        metadataRepository.updateArtist(
                            newArtistName = _uiState.value.artist,
                            oldArtist = currentArtist,
                            mbArtist = searchResults[0],
                            track = currentTrack.copy(albumId = track.albumId)
                        )
                    }
                    _workflowState.value = AlbumArtistEditUiState.Saved
                    _uiState.update { it.copy(isSaving = false) }
                    onBack()
                } catch (e: SocketTimeoutException) {
                    _workflowState.value =
                        AlbumArtistEditUiState.Error("MusicBrainz is taking too long. Please try again.")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    _workflowState.value =
                        AlbumArtistEditUiState.Error("Network error: ${e.message}")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    Log.d("SaveError", "Failed to save: ${e.message}", e)
                    _uiState.update { it.copy(isSaving = false) }
                }

            }
            _uiState.update { it.copy(isSaving = false) }
            onBack()


        }
    }

    fun resetToOriginal() {
        _uiState.update {
            it.copy(
                title = initialTitle ?: "",
                draftTrackNumber = initialNumber ?: "",
                draftMoods = initialMoods
            )
        }
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