package com.example.musicapp.ui.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository,
    private val metadataRepository: MetadataRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
    ) : ViewModel() {

        companion object {
            private const val TIMEOUT_MILLIS = 5_000L
        }

    private val artistId: Int = savedStateHandle.get<String>("artistId")?.toInt()
        ?: throw IllegalStateException("artistId not found in SavedStateHandle")


    private val _pendingDeleteUris = MutableStateFlow<List<String>>(emptyList())
    val pendingDeleteUris = _pendingDeleteUris.asStateFlow()

    private val _refetchState = MutableStateFlow<RefetchAlbumState>(RefetchAlbumState.Idle)
    val refetchState = _refetchState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val artistDetailUiState: StateFlow<ArtistDetailUiState> = combine(
        userPreferencesRepository.artistAlbumsSortOption,
        flowOf(artistId)
    ) { sort, id ->
        Pair(id, sort)
    }.flatMapLatest { (id, sort) ->
        combine(
            artistRepository.getArtist(id),
            albumArtistRepository.getAllAlbumsByArtistSorted(id, sort)
        ) { artist, albums ->
            ArtistDetailUiState(
                artist = artist,
                albums = albums,
                isLoading = false
            )
        }.onStart { emit(ArtistDetailUiState(isLoading = true)) }
            .catch { e -> emit(ArtistDetailUiState(error = e.message, isLoading = false)) }
    }
        .distinctUntilChanged()
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArtistDetailUiState(isLoading = true)
    )

    fun setSort(option: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.updateArtistAlbumsSort(option)
        }
    }

    fun prepareDeletion(albumId: Int) {
        viewModelScope.launch {
            val uris = trackRepository.getAlbumTracks(albumId).map { it.fileUri }
            _pendingDeleteUris.value = uris
        }
    }


    fun finalizeDeletion(albumId: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            albumRepository.deleteById(albumId)
            artistRepository.deleteOrphaned()
            albumRepository.deleteOrphaned()

            clearPendingDeletion()
        }

    }

    fun clearPendingDeletion() {
        _pendingDeleteUris.value = emptyList()
    }

    fun getDeleteIntent(context: Context, uriStrings: List<String>): PendingIntent {
        val uris = uriStrings.map { it.toUri() }
        return MediaStore.createDeleteRequest(context.contentResolver, uris)
    }


    fun refetchMetadata(id: Int){
        viewModelScope.launch {
            val album = albumRepository.getAlbum(id).first()
            var albumArtist = artistDetailUiState.value.artist
            if (albumArtist == null){
                val artists = albumArtistRepository.getAllAlbumArtists(id)
                albumArtist = artists.first { it.mbId != null }
            }
            _refetchState.value = RefetchAlbumState.Saving
            val query = if (albumArtist.mbId != null) "arid:${albumArtist.mbId} AND release:${album.searchKey}" else """artist:${albumArtist.searchKey} AND release:${album.searchKey}"""
            val searchResults = albumRepository.findAlbumMB(query)
            if (searchResults == null || searchResults.releases.isEmpty()) {
                _refetchState.value = RefetchAlbumState.Error("No album found")
            } else if (searchResults.releases.size > 1) {
                _refetchState.value = RefetchAlbumState.DisambiguationNeeded(searchResults.releases, album)
                return@launch
            } else {
                performFinalSave(searchResults.releases[0], album)
            }
            _refetchState.value = RefetchAlbumState.Saved

        }
    }

    fun reset(){
        _refetchState.value = RefetchAlbumState.Idle
    }

    suspend fun performFinalSave(
        release: Release,
        oldAlbum: Album,
    ) {
        val newId = metadataRepository.refetchAlbum(
            album = release,
            currentAlbum = oldAlbum
        )
        _refetchState.value = RefetchAlbumState.Saved
    }

    fun onAlbumSelected(release: Release){
        viewModelScope.launch {
            val currentAlbum = (_refetchState.value as RefetchAlbumState.DisambiguationNeeded).album
            _refetchState.value = RefetchAlbumState.Saving
            performFinalSave(
                release,
                currentAlbum,
            )

        }
    }


}

data class ArtistDetailUiState(
    val artist: Artist? = null,
    val albums: List<AlbumInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)


//data class ArtistState(
//    val artist: Artist? = null
//)