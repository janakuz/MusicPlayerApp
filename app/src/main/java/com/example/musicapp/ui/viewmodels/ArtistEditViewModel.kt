package com.example.musicapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.FullArea
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
import com.example.musicapp.data.remote.dto.DiscogsImage
import com.example.musicapp.data.repository.AreaRepository
import com.example.musicapp.data.repository.ArtistGenreRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.util.getFlagEmoji
import com.example.musicapp.util.isTrulyBlank
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ArtistEditViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val metadataRepository: MetadataRepository,
    private val genreRepository: GenreRepository,
    private val artistGenreRepository: ArtistGenreRepository,
    private val areaRepository: AreaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: Int = savedStateHandle.get<String>("artistId")?.toInt()
        ?: throw IllegalStateException("artistId not found in SavedStateHandle")

    private val _uiState = MutableStateFlow(ArtistEditUiState(id = artistId))
    val uiState = _uiState.asStateFlow()

    private val _workflowState = MutableStateFlow<NameEditUiState>(NameEditUiState.Idle)
    val workflowState = _workflowState.asStateFlow()

    private val _genreQuery = MutableStateFlow("")

    private val _cityQuery = MutableStateFlow("")


    private var initialName: String? = null
    private var initialBio: String? = ""
    private var initialImageUrl: String? = ""
    private var initialGenres: List<String> = emptyList()
    private var initialIsDefunct: Boolean? = false
    private var initialHomeCity: String? = ""
    private var initialHomeAreaId: String? = null
    private var initialCurrentCity: String? = ""
    private var initialCountry: String? = ""
    private var initialCountryCode: String? = ""
    private var initialStartYear: String? = ""
    private var initialEndYear: String? = ""


    val canSave: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges =
            state.draftBio != initialBio || state.draftImageUrl != initialImageUrl || state.name != initialName || state.draftGenres != initialGenres
                    || state.draftCountry != initialCountry  || state.draftCountryCode != initialCountryCode
                    || state.draftHomeCity != initialHomeCity || state.draftCurrentCity != initialCurrentCity || state.draftHomeCityId != initialHomeAreaId
                    || state.draftActiveStartYear != initialStartYear || state.draftActiveEndYear != initialEndYear || state.draftIsDefunct != initialIsDefunct
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val genreSuggestions: StateFlow<List<String>> = _genreQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                flowOf(emptyList())
            } else {
                genreRepository.findGenre(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val citySuggestions: StateFlow<List<AreaHierarchy>> = _cityQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2){
                flowOf(emptyList())
            } else {
                areaRepository.findCity(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {
        loadArtistData()
    }

    private fun loadArtistData() {
        viewModelScope.launch {
            val artistWithArea = artistRepository.getArtistWithArea(artistId).first()
            val artist = artistWithArea.artist
            val genres = artistGenreRepository.getArtistGenres(artistId)

            initialName = artist.name
            initialBio = artist.bio
            initialImageUrl = artist.image ?: ""
            initialGenres = genres
            initialIsDefunct = artist.isDefunct
            initialHomeCity = getLowestArea(artistWithArea.area) ?: artist.homeCity ?: ""
            initialHomeAreaId = artist.homeAreaGid
            initialCurrentCity = artist.currentCity ?: ""
            initialCountry = artist.country ?: ""
            initialCountryCode = artist.countryCode ?: ""
            initialStartYear = artist.activeStartYear ?: ""
            initialEndYear = artist.activeEndYear ?: ""

            Log.d("AREA_DEBUG", "Init: $initialHomeCity")

            Log.d("EDIT_DEBUG", "Draft: ${getLowestArea(artistWithArea.area) ?: artist.homeCity ?: ""}")


            _uiState.update {
                it.copy(
                    name = artist.name,
                    draftBio = artist.bio ?: "",
                    draftImageUrl = artist.image ?: "",
                    draftGenres = genres,
                    draftIsDefunct = artist.isDefunct,
                    draftCountry = artist.country ?: "",
                    draftHomeCity = getLowestArea(artistWithArea.area) ?: artist.homeCity ?: "",
                    draftHomeCityId = artist.homeAreaGid,
                    draftCurrentCity = artist.currentCity ?: "",
                    draftCountryCode = artist.countryCode ?: "",
                    draftActiveStartYear = artist.activeStartYear ?: "",
                    draftActiveEndYear = artist.activeEndYear ?: "",
                    )
            }


            if (artist.discogsId != null) getDiscogsInfo(artist.discogsId)
            getLastfmInfo(artist.mbId, artist.name)
        }


    }

    fun onCountryChange(countryCode: String) {
        val fullName = CountryProvider.allCountries.find { it.code == countryCode }?.name.orEmpty()

        _uiState.update { currentState ->
            currentState.copy(
                draftCountryCode = countryCode,
                draftCountry = fullName
            )
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onBioChange(newBio: String) {
        _uiState.update { it.copy(draftBio = newBio) }
    }

    fun onHomeCityChange(newCity: String) {
        _uiState.update { it.copy(draftHomeCity = newCity) }
    }

    fun onCurrentCityChange(newCity: String) {
        _uiState.update { it.copy(draftCurrentCity = newCity) }
    }

    fun onActiveStartYearChange(newStart: String) {
        _uiState.update { it.copy(draftActiveStartYear = newStart) }
    }

    fun onActiveEndYearChange(newEnd: String) {
        _uiState.update { it.copy(draftActiveEndYear = newEnd) }
    }

    fun onDefunctStatusChange(newStatus: Boolean) {
        _uiState.update { it.copy(draftIsDefunct = newStatus) }
    }


    fun onImageChange(newImageUrl: String) {
        _uiState.update { it.copy(draftImageUrl = newImageUrl) }
    }


    suspend fun getDiscogsInfo(discogsId: String) {
        val discogs = artistRepository.getArtistDiscogsInfo(discogsId)
        if (discogs != null) {
            _uiState.update {
                it.copy(discogsBio = discogs.profile, discogsImages = discogs.images ?: emptyList())
            }
        }
    }

    suspend fun getLastfmInfo(mbId: String?, name: String) {
        val lastFm = artistRepository.getArtistBio(mbId, name)
        _uiState.update {
            it.copy(lastFmBio = lastFm)
        }
    }

    fun onSave(onBack: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val currentArtist = artistRepository.getArtist(artistId).first()

            val newArtist = currentArtist.copy(
                bio = _uiState.value.draftBio,
                image = _uiState.value.draftImageUrl,
                homeCity = _uiState.value.draftHomeCity,
                homeAreaGid = _uiState.value.draftHomeCityId,
                currentCity = _uiState.value.draftCurrentCity,
                country = _uiState.value.draftCountry,
                countryCode = _uiState.value.draftCountryCode,
                isDefunct = _uiState.value.draftIsDefunct,
                activeStartYear = _uiState.value.draftActiveStartYear,
                activeEndYear = _uiState.value.draftActiveEndYear
            )
            artistRepository.update(newArtist)
            if (initialGenres != _uiState.value.draftGenres){
                artistGenreRepository.updateArtistGenres(artistId, _uiState.value.draftGenres)
            }

            if (initialName != _uiState.value.name) {
                try {
                    _workflowState.value = NameEditUiState.Saving

                    val searchResults = artistRepository.findArtistMB(_uiState.value.name)
                    if (searchResults.isEmpty()) {
                        _workflowState.value = NameEditUiState.Error("No artist found")
                    } else if (searchResults.size > 1) {
                        _workflowState.value = NameEditUiState.DisambiguationNeeded(searchResults)
                        return@launch
                    } else {
                        performFinalSave(searchResults[0], currentArtist, onBack)
                    }
                    _uiState.update { it.copy(isSaving = false) }
                    onBack()

                } catch (e: SocketTimeoutException) {
                    _workflowState.value =
                        NameEditUiState.Error("MusicBrainz is taking too long. Please try again.")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    _workflowState.value = NameEditUiState.Error("Network error: ${e.message}")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    Log.d("SaveError", "Failed to save: ${e.message}", e)
                    _uiState.update { it.copy(isSaving = false) }
                }
            } else {
                onBack()
            }
        }
    }

    suspend fun performFinalSave(
        artistResult: ArtistSearchInfo,
        oldArtist: Artist,
        onBack: () -> Unit
    ) {
        val updatedArtist = metadataRepository.updateArtist(
            newArtistName = _uiState.value.name,
            oldArtist = oldArtist,
            mbArtist = artistResult
        )
        _uiState.update { it.copy(id = updatedArtist.id) }
        _workflowState.value = NameEditUiState.Saved
        onBack()
    }

    fun onArtistSelected(artistResult: ArtistSearchInfo, onBack: () -> Unit) {
        viewModelScope.launch {
            _workflowState.value = NameEditUiState.Saving
            val currentArtist = artistRepository.getArtist(artistId).first()
            performFinalSave(artistResult, currentArtist, onBack)

        }
    }

    fun onGenresChange(newGenres: List<String>) {
        _uiState.update { it.copy(draftGenres = newGenres.distinct()) }
        _genreQuery.value = ""
    }

    fun onGenreQueryChange(newQuery: String) {
        _genreQuery.value = newQuery
    }


    fun onCityQueryChange(newQuery: String) {
        _cityQuery.value = newQuery
    }

    fun getLowestArea(area: AreaHierarchy) : String? {

        return  if (!area.city.isTrulyBlank()) area.cityName
        else if (!area.county.isTrulyBlank()) area.countyName
        else if (!area.state.isTrulyBlank()) area.stateName
        else if (!area.country.isTrulyBlank()) area.countryName
        else null
    }

    fun onSelectedArea(newArea: AreaHierarchy){
        _uiState.update { it.copy(
            draftHomeCityId =
                if (!newArea.city.isTrulyBlank()) newArea.city
                else if (!newArea.county.isTrulyBlank()) newArea.county
                else if (!newArea.state.isTrulyBlank()) newArea.state
                else if (!newArea.country.isTrulyBlank()) newArea.country
                else null,
            draftHomeCity =
                if (!newArea.cityName.isTrulyBlank()) newArea.cityName
                else if (!newArea.countyName.isTrulyBlank()) newArea.countyName
//                else if (!newArea.stateName.isTrulyBlank()) newArea.stateName
//                else if (!newArea.countryName.isTrulyBlank()) newArea.countryName
                else "",
        ) }
    }

    fun onSelectedNotFound(newCity: String){
        _uiState.update { it.copy(
            draftHomeCityId = null,
            draftHomeCity = newCity
        ) }
    }


    fun resetName() {
        _uiState.update {
            it.copy(
                name = initialName ?: ""
            )
        }
    }

    fun resetToOriginal() {
        _uiState.update {
            it.copy(
                draftBio = initialBio ?: "",
                draftImageUrl = initialImageUrl ?: ""
            )
        }
    }
}


data class ArtistEditUiState(
    val isLoading: Boolean = false,
    val id: Int,
    val name: String = "",
    val draftBio: String = "",
    val draftImageUrl: String = "",
    val discogsImages: List<DiscogsImage> = emptyList(),
    val draftGenres: List<String> = emptyList(),
    val draftCountry: String = "",
    val draftCountryCode: String = "",
    val draftHomeCity: String? = "",
    val draftHomeCityId: String? = null,
    val draftCurrentCity: String = "",
    val draftActiveStartYear: String = "",
    val draftActiveEndYear: String = "",
    val draftIsDefunct: Boolean = false,
    val lastFmBio: String = "",
    val discogsBio: String = "",
    val isSaving: Boolean = false
)

sealed class NameEditUiState {
    object Idle : NameEditUiState()
    object Saving : NameEditUiState()
    data class DisambiguationNeeded(val matches: List<ArtistSearchInfo>) : NameEditUiState()
    object Saved : NameEditUiState()
    data class Error(val message: String) : NameEditUiState() // Show toast
}

data class CountryData(
    val code: String,
    val name: String,
    val flag: String
)

object CountryProvider {
    val allCountries: List<CountryData> by lazy {
        Locale.getISOCountries().map { code ->
            val locale = Locale.Builder()
                .setRegion(code)
                .build()
            CountryData(
                code = code,
                name = locale.displayCountry,
                flag = getFlagEmoji(code)
            )
        }.sortedBy { it.name }
    }
}