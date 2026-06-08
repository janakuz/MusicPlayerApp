package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.FilterRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.LibraryFilter
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    val filterDefaults = combine(
        filterRepository.getMinYear(),
        filterRepository.getMaxYear(),
        filterRepository.getAllLabels(),
        genreRepository.getAll(SortOption(SortField.NAME,true))
    ) { min, max, labels, genres ->
        FilterDefaults(
            minYear = min,
            maxYear = max,
            recordLabels = labels,
            genres = genres.map { it.genre.name }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilterDefaults())


    private val _libraryType = MutableStateFlow<String>("artists")

    private val _activeFilter = MutableStateFlow<LibraryFilter>(LibraryFilter())

    private val _draftFilter = MutableStateFlow(LibraryFilter())
    val draftFilter = _draftFilter.asStateFlow()

    private val _labelQuery = MutableStateFlow("")

    private val _genreQuery = MutableStateFlow("")


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


    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredArtists = combine(_activeFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (_libraryType.value != "artists") flowOf(emptyList())
        else filterRepository.getFilteredArtists(filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredAlbums = combine(_activeFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (_libraryType.value != "albums") flowOf(emptyList())
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
        if (_libraryType.value != "albums") flowOf(emptyList())
        else {
            val fullRanges = filter.dateRanges + listOf<IntRange>(filter.activeRange)
            val newFilter = filter.copy(dateRanges = fullRanges)
            filterRepository.getFilteredAlbums(newFilter)
        }
    }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    @OptIn(ExperimentalCoroutinesApi::class)
    val potentialArtistMatches = combine(_draftFilter, _libraryType) { filter, type ->
        Pair(filter, type)
    }.flatMapLatest { (filter, type) ->
        if (_libraryType.value != "artists") flowOf(emptyList())
        else filterRepository.getFilteredArtists(filter)
    }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    init {

        viewModelScope.launch {
            filterDefaults.collect { defaults ->
                if (_draftFilter.value.activeRange == 1950..2026) {
                    _draftFilter.update {
                        it.copy(
                            activeRange = defaults.minYear..defaults.maxYear
                        )
                    }
                }
            }
        }
    }


    fun updateType(type: String){
        _libraryType.value = type.lowercase()
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


    fun resetAll(){
        _draftFilter.value = LibraryFilter()
        _activeFilter.value = LibraryFilter()
    }

    fun resetDraft(){
        _draftFilter.value = LibraryFilter()
    }

    fun reset() {
        _draftFilter.value = _activeFilter.value
    }
}

data class FilterDefaults(
    val minYear: Int = 1950,
    val maxYear: Int = 2026,
    val recordLabels: List<String> = emptyList(),
    val genres: List<String> = emptyList()
)