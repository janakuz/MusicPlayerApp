package com.example.musicapp.ui.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.ArtistWithArea
import com.example.musicapp.data.remote.dto.Release
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.AreaRepository
import com.example.musicapp.data.repository.AreaType
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
    private val areaRepository: AreaRepository,
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
            artistRepository.getArtistWithArea(id),
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val countryArtistCount: StateFlow<Int> = artistDetailUiState.flatMapLatest { state ->
        if (state.artist?.artist?.countryCode == null) flowOf(0)
        else {
            areaRepository.getCountryArtistsAndAlbums(state.artist.artist.countryCode)
                .map { it.artists.size }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    val sameCityArtists: StateFlow<List<Artist>> = artistDetailUiState.flatMapLatest { state ->
        if (state.artist?.area?.city != null)
            areaRepository.getArtistsFromArea(
                state.artist.area.city, state.artist.artist.countryCode ?: "", AreaType.CITY)
                .map { it.filter { it.id != state.artist.artist.id } }
        else flowOf(emptyList<Artist>())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val sameCountyArtists: StateFlow<List<Artist>> = artistDetailUiState.flatMapLatest { state ->
        if (state.artist?.area?.county != null)
            areaRepository.getArtistsFromArea(
                state.artist.area.county, state.artist.artist.countryCode ?: "", AreaType.COUNTY)
                .map { it.filter { it.id != state.artist.artist.id } }
        else flowOf(emptyList<Artist>())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val sameStateArtists: StateFlow<List<Artist>> = artistDetailUiState.flatMapLatest { state ->
        if (state.artist?.area?.state != null)
            areaRepository.getArtistsFromArea(
                state.artist.area.state, state.artist.artist.countryCode ?: "", AreaType.STATE)
                .map { it.filter { it.id != state.artist.artist.id } }
        else flowOf(emptyList<Artist>())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val sameCountryArtists: StateFlow<List<Artist>> = artistDetailUiState.flatMapLatest { state ->
        Log.d("area", state.artist?.artist?.countryCode.toString())
        if (state.artist?.artist?.countryCode != null)
            areaRepository.getArtistsFromArea(
                state.artist.area.country ?: "", state.artist.artist.countryCode, AreaType.COUNTRY)
                .map { it.filter { it.id != state.artist.artist.id } }
        else flowOf(emptyList<Artist>())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
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


    fun refetchMetadata(id: Int) {
        viewModelScope.launch {
            val album = albumRepository.getAlbum(id).first()
            var albumArtist = artistDetailUiState.value.artist?.artist
            if (albumArtist == null) {
                val artists = albumArtistRepository.getAllAlbumArtists(id)
                albumArtist = artists.first { it.mbId != null }
            }
            _refetchState.value = RefetchAlbumState.Saving
            val query =
                if (albumArtist.mbId != null) "arid:${albumArtist.mbId} AND release:${album.searchKey}" else """artist:${albumArtist.searchKey} AND release:${album.searchKey}"""
            val searchResults = albumRepository.findAlbumMB(query)
            if (searchResults == null || searchResults.releases.isEmpty()) {
                _refetchState.value = RefetchAlbumState.Error("No album found")
            } else if (searchResults.releases.size > 1) {
                _refetchState.value =
                    RefetchAlbumState.DisambiguationNeeded(searchResults.releases, album)
                return@launch
            } else {
                performFinalSave(searchResults.releases[0], album)
            }
            _refetchState.value = RefetchAlbumState.Saved

        }
    }

    fun reset() {
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

    fun onAlbumSelected(release: Release) {
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
    val artist: ArtistWithArea? = null,
    val albums: List<AlbumInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)


//data class ArtistState(
//    val artist: Artist? = null
//)