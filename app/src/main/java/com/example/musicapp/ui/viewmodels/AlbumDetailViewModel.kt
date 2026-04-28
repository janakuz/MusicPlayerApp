package com.example.musicapp.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.DynamicThemeRepository
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.PlayerColors
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.normalizeForMatching
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.joinToString

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository,
    private val metadataRepository: MetadataRepository,
    private val dynamicThemeRepository: DynamicThemeRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val albumId: Int = savedStateHandle.get<String>("albumId")?.toInt()
        ?: throw IllegalStateException("albumId not found in SavedStateHandle")

    private val _currentAlbumUiState = MutableStateFlow(AlbumState())
    val currentAlbumUiState: StateFlow<AlbumState> = _currentAlbumUiState.asStateFlow()

    private val _albumTracksUiState = MutableStateFlow(TracksUiState())
    val albumTracksUiState: StateFlow<TracksUiState> = _albumTracksUiState.asStateFlow()

    private val _moveState = MutableStateFlow<RefetchAlbumTracksState>(RefetchAlbumTracksState.Idle)
    val moveState = _moveState.asStateFlow()

    private val _pendingMoveIds = MutableStateFlow<List<Int>>(emptyList())
    val pendingMoveIds = _pendingMoveIds.asStateFlow()

    private val _currentNewAlbum = MutableStateFlow(NewAlbum())
    val currentNewAlbum: StateFlow<NewAlbum> = _currentNewAlbum.asStateFlow()


    var albumColors by mutableStateOf(PlayerColors(
        mainColor = Color(0xFF121212),
        secondaryColor = Color.Cyan,
        onColor = Color.White
    ))

    init {
        viewModelScope.launch {
            val albumJob = launch {
                getAlbumById(albumId)
            }
            val tracksJob = launch {
                getTracksInAlbum(albumId)
            }

            joinAll(albumJob, tracksJob)
        }
    }


    fun getAlbumById(id: Int){
        viewModelScope.launch {
            albumRepository.getAlbum(id)
                .collect { album -> _currentAlbumUiState.update { it.copy(album = album) } }
        }
    }

    fun getTracksInAlbum(albumId: Int){
        viewModelScope.launch {
            trackRepository.getTracksInAlbum(albumId)
                .onStart { _albumTracksUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _albumTracksUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _albumTracksUiState.update {
                        it.copy(
                            tracks = list,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }

    }

    fun getAlbumColors(imagePath: String) {
        viewModelScope.launch {
            dynamicThemeRepository.extractColorsFromUrl(imagePath, context = context)?.let {
                albumColors = it
            }
        }
    }


    fun prepareMove(ids: List<Int>) {
        viewModelScope.launch {
            _pendingMoveIds.value = ids
            _moveState.value = RefetchAlbumTracksState.InputExpected
        }
    }


    fun splitToAlbum(ids: List<Int>, artist: String, album: String){
        viewModelScope.launch {
            _moveState.value = RefetchAlbumTracksState.Saving
            val query = """artist:${artist.normalizeForMatching()} AND release:${album.normalizeForMatching()}"""
            val searchResults = albumRepository.findAlbumMB(query)
            if (searchResults == null || searchResults.releases.isEmpty() || (artist.isEmpty() && album.isEmpty())) {
                metadataRepository.moveToUnenriched(album, artist, ids, albumId)
                _moveState.value = RefetchAlbumTracksState.Error("No album found")
            } else if (searchResults.releases.size > 1) {
                _moveState.value = RefetchAlbumTracksState.DisambiguationNeeded(searchResults.releases, ids)
                return@launch
            } else {
                performFinalSave(searchResults.releases[0], ids)
            }
            _moveState.value = RefetchAlbumTracksState.Saved


        }
    }

    fun splitToUnenriched(ids: List<Int>, artist: String, album: String){
        viewModelScope.launch {
            metadataRepository.moveToUnenriched(album, artist, ids, albumId)
        }
    }

    fun reset(){
        _moveState.value = RefetchAlbumTracksState.Idle
    }

    suspend fun performFinalSave(
        release: Release,
        ids: List<Int>
    ) {
        metadataRepository.moveToAlbum(
            album = release,
            tracksToMove = ids,
            oldAlbumId = albumId
        )
        _moveState.value = RefetchAlbumTracksState.Saved
    }

    fun onAlbumSelected(release: Release){
        viewModelScope.launch {
            val tracksToMove = (_moveState.value as RefetchAlbumTracksState.DisambiguationNeeded).tracks
            _moveState.value = RefetchAlbumTracksState.Saving
            performFinalSave(
                release,
                tracksToMove,
            )

        }
    }


    fun onTitleChange(newTitle: String) {
        _currentNewAlbum.update { it.copy(title = newTitle) }
    }

    fun onArtistChange(newArtist: String) {
        _currentNewAlbum.update { it.copy(artist = newArtist) }
    }



}

data class AlbumState(
    val album: Album? = null
)

data class NewAlbum(
    val title: String? = null,
    val artist: String? = null
)

sealed class RefetchAlbumTracksState {
    object Idle: RefetchAlbumTracksState()
    object InputExpected: RefetchAlbumTracksState()
    data class DisambiguationNeeded(val matches: List<Release>, val tracks: List<Int>) : RefetchAlbumTracksState()
    object Saving: RefetchAlbumTracksState()
    object Saved : RefetchAlbumTracksState()
    data class Error(val message: String) : RefetchAlbumTracksState()
}