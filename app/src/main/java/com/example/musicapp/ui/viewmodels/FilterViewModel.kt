package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.data.repository.AreaRepository
import com.example.musicapp.data.repository.DefunctFilterStatus
import com.example.musicapp.data.repository.FilterRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.LibraryFilter
import com.example.musicapp.data.repository.MoodRepository
import com.example.musicapp.ui.components.FilterType
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val filterRepository: FilterRepository,
    private val genreRepository: GenreRepository,
    private val areaRepository: AreaRepository,
    private val moodRepository: MoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    val filterDefaults = combine(
        listOf(
        filterRepository.getMinYear(),
        filterRepository.getMaxYear(),
        filterRepository.getMinYearArtists(),
        filterRepository.getMaxYearArtists(),
        filterRepository.getAllLabels(),
        genreRepository.getAll(SortOption(SortField.NAME,true)
        )
        )
    ) { list ->
        val min = list[0] as Int
        val max = list[1] as Int
        val minArtists = list[2] as Int
        val maxArtists = list[3] as Int
        val labels = list[4] as List<String>
        val genres = list[5] as List<GenreInfo>
        FilterDefaults(
            minYear = min,
            maxYear = max,
            minYearArtists = minArtists,
            maxYearArtists = maxArtists,
            recordLabels = labels,
            genres = genres.map { it.genre.name }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilterDefaults())


    private val _libraryType = MutableStateFlow<FilterType>(FilterType.ARTISTS)
    val libraryType = _libraryType.asStateFlow()

    private val _activeFilter = MutableStateFlow<LibraryFilter>(LibraryFilter())

    private val _draftFilter = MutableStateFlow(LibraryFilter())
    val draftFilter = _draftFilter.asStateFlow()

    private val _labelQuery = MutableStateFlow("")

    private val _genreQuery = MutableStateFlow("")

    private val _areaQuery = MutableStateFlow("")

    private val _moodQuery = MutableStateFlow("")


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val labelSuggestions: StateFlow<List<String>> = _labelQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                filterRepository.getAllLabels()
            } else {
                filterRepository.findLabel(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val genreSuggestions: StateFlow<List<String>> = _genreQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                genreRepository.getAll(SortOption(SortField.NAME,true)).map { genreInfoList ->
                    genreInfoList.map { it.genre.name }
                }
            }
            else {
                genreRepository.findGenre(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val moodSuggestions: StateFlow<List<String>> = _moodQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                moodRepository.getAll(SortOption(SortField.NAME,true)).map { moodInfoList ->
                    moodInfoList.map { it.mood.name }
                }
            }
            else {
                moodRepository.findMood(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )



    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val areaSuggestions: StateFlow<List<AreaHierarchy>> = _areaQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2)
                flowOf(emptyList())
            else
                areaRepository.findCity(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredArtists = combine(_activeFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (type != FilterType.ARTISTS) flowOf(emptyList())
        else {
            val fullStartRanges = filter.artistFormedRanges + listOf<IntRange>(filter.activeArtistStartRange)
            var newFilter = filter.copy(artistFormedRanges = fullStartRanges)
            if (_draftFilter.value.defunctStatus == DefunctFilterStatus.DEFUNCT) {
                val fullEndRanges =
                    filter.artistEndedRanges + listOf<IntRange>(filter.activeArtistEndRange)
                newFilter = newFilter.copy(artistEndedRanges = fullEndRanges)
            }
            filterRepository.getFilteredArtists(newFilter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val potentialArtistMatches = combine(_draftFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (type != FilterType.ARTISTS) flowOf(emptyList())
        else {
            val fullStartRanges = filter.artistFormedRanges + listOf<IntRange>(filter.activeArtistStartRange)
            var newFilter = filter.copy(artistFormedRanges = fullStartRanges)
            if (_draftFilter.value.defunctStatus == DefunctFilterStatus.DEFUNCT) {
                val fullEndRanges =
                    filter.artistEndedRanges + listOf<IntRange>(filter.activeArtistEndRange)
                newFilter = newFilter.copy(artistEndedRanges = fullEndRanges)
            }
            filterRepository.getFilteredArtists(newFilter)
        }
    }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredAlbums = combine(_activeFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (type != FilterType.ALBUMS) flowOf(emptyList())
        else {
            val fullRanges = filter.dateRanges + listOf<IntRange>(filter.activeRange)
            val newFilter = filter.copy(dateRanges = fullRanges)
            filterRepository.getFilteredAlbums(newFilter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    val potentialAlbumMatches = combine(_draftFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (type != FilterType.ALBUMS) flowOf(emptyList())
        else {
            val fullRanges = filter.dateRanges + listOf<IntRange>(filter.activeRange)
            val newFilter = filter.copy(dateRanges = fullRanges)
            filterRepository.getFilteredAlbums(newFilter)
        }
    }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)



    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredTracks = combine(_activeFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (type != FilterType.TRACKS) flowOf(emptyList())
        else {
            val fullBPMRanges = filter.bpmRanges + listOf<IntRange>(filter.activeBPMRange)
            val fullApproachabilityRanges = filter.approachabilityRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeApproachabilityRange)
            val fullEngagementRanges = filter.engagementRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeEngagementRange)
            val fullDanceabilityRanges = filter.danceabilityRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeDanceabilityRange)
            val fullHappyRanges = filter.moodHappyRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeHappyRange)
            val fullAggressiveRanges = filter.moodAggressiveRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeAggressiveRange)
            val fullPartyRanges = filter.moodPartyRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activePartyRange)
            val fullSadRanges = filter.moodSadRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeSadRange)
            val fullRelaxedRanges = filter.moodRelaxedRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeRelaxedRange)
            val fullKeys = filter.selectedKeys + listOf(filter.activeKeySelection)

            val newFilter = filter.copy(
                bpmRanges = fullBPMRanges,
                approachabilityRanges = fullApproachabilityRanges,
                engagementRanges = fullEngagementRanges,
                danceabilityRanges = fullDanceabilityRanges,
                moodHappyRanges = fullHappyRanges,
                moodSadRanges = fullSadRanges,
                moodAggressiveRanges = fullAggressiveRanges,
                moodRelaxedRanges = fullRelaxedRanges,
                moodPartyRanges = fullPartyRanges,
                selectedKeys = fullKeys
                )
            filterRepository.getFilteredTracks(newFilter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    val potentialTrackMatches = combine(_draftFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (type != FilterType.TRACKS) flowOf(emptyList())
        else {
            val fullBPMRanges = filter.bpmRanges + listOf<IntRange>(filter.activeBPMRange)
            val fullApproachabilityRanges = filter.approachabilityRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeApproachabilityRange)
            val fullEngagementRanges = filter.engagementRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeEngagementRange)
            val fullDanceabilityRanges = filter.danceabilityRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeDanceabilityRange)
            val fullHappyRanges = filter.moodHappyRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeHappyRange)
            val fullAggressiveRanges = filter.moodAggressiveRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeAggressiveRange)
            val fullPartyRanges = filter.moodPartyRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activePartyRange)
            val fullSadRanges = filter.moodSadRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeSadRange)
            val fullRelaxedRanges = filter.moodRelaxedRanges + listOf<ClosedFloatingPointRange<Float>>(filter.activeRelaxedRange)
            val fullKeys = filter.selectedKeys + listOf(filter.activeKeySelection)

            val newFilter = filter.copy(
                bpmRanges = fullBPMRanges,
                approachabilityRanges = fullApproachabilityRanges,
                engagementRanges = fullEngagementRanges,
                danceabilityRanges = fullDanceabilityRanges,
                moodHappyRanges = fullHappyRanges,
                moodSadRanges = fullSadRanges,
                moodAggressiveRanges = fullAggressiveRanges,
                moodRelaxedRanges = fullRelaxedRanges,
                moodPartyRanges = fullPartyRanges,
                selectedKeys = fullKeys
            )
            filterRepository.getFilteredTracks(newFilter)
        }
    }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)



    init {

        resetAll()
        viewModelScope.launch {
            val realDefaults = filterDefaults.first { it.minYear != 1950 || it.maxYear != 2026 }
            resetDefaultYears(realDefaults)
        }

    }


    fun resetDefaultYears(defaults: FilterDefaults){
        _draftFilter.update { currentDraft ->
            currentDraft.copy(
                activeRange = defaults.minYear..defaults.maxYear,
                activeArtistStartRange = defaults.minYearArtists..defaults.maxYearArtists,
                activeArtistEndRange = defaults.minYearArtists..defaults.maxYearArtists
            )
        }

    }


    fun updateType(type: FilterType){
        _libraryType.value = type
    }

    fun applyFilters() {
        _activeFilter.value = _draftFilter.value
    }

    fun updateDraft(newFilter: LibraryFilter) {
        _draftFilter.value = newFilter
    }

    fun onLabelQueryChange(newQuery: String) {
        _labelQuery.value = newQuery
    }

    fun onGenreQueryChange(newQuery: String) {
        _genreQuery.value = newQuery
    }


    fun onMoodQueryChange(newQuery: String) {
        _moodQuery.value = newQuery
    }


    fun onAreaQueryChange(newQuery: String) {
        _areaQuery.value = newQuery
    }

    fun resetAll(){
        _draftFilter.value = LibraryFilter()
        _activeFilter.value = LibraryFilter()
        resetDefaultYears(filterDefaults.value)
    }

    fun resetDraft(){
        _draftFilter.value = LibraryFilter()
        resetDefaultYears(filterDefaults.value)
    }

    fun reset() {
        _draftFilter.value = _activeFilter.value
    }
}

data class FilterDefaults(
    val minYear: Int = 1950,
    val maxYear: Int = 2026,
    val minYearArtists: Int = 1950,
    val maxYearArtists: Int = 2026,
    val recordLabels: List<String> = emptyList(),
    val genres: List<String> = emptyList()
)