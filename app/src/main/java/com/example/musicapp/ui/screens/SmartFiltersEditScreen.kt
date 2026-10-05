package com.example.musicapp.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.FilterDrawerContent
import com.example.musicapp.ui.viewmodels.SmartFiltersEditViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.ui.components.FilterType
import com.example.musicapp.ui.viewmodels.FilterDefaults
import com.example.musicapp.ui.viewmodels.FilterViewModel

@Composable
fun SmartFiltersEditScreen(
    interaction: MutableInteractionSource,
    onBack: () -> Unit,
){
    val smartFiltersEditViewModel: SmartFiltersEditViewModel = hiltViewModel()
    val filterViewModel: FilterViewModel = hiltViewModel()

    val filterDefaults by filterViewModel.filterDefaults.collectAsState()
    val labelSuggestions by filterViewModel.labelSuggestions.collectAsState()
    val genreSuggestions by filterViewModel.genreSuggestions.collectAsState()
    val filterType by filterViewModel.libraryType.collectAsState()
    val areaSuggestions by filterViewModel.areaSuggestions.collectAsState()
    val moodSuggestions by filterViewModel.moodSuggestions.collectAsState()


    val filter by smartFiltersEditViewModel._draftFilter.collectAsState()
    val matches by smartFiltersEditViewModel.filteredGlobalTracks.collectAsState()

    FilterDrawerContent(
        draft = filter,
        isGlobal = true,
        filterType = filterType,
        potentialAlbumCount = 0,
        potentialArtistCount = 0,
        potentialTrackCount = 0,
        potentialGlobalCount = matches.size,
        filterDefaults = filterDefaults,
        labelSuggestions = labelSuggestions,
        onDraftChange = { newFilter -> smartFiltersEditViewModel.updateDraft(newFilter) },
        onApply = {
            smartFiltersEditViewModel.onSave()
            filterViewModel.resetDraft()
            onBack()
        },
        onLabelQueryChange = { query -> filterViewModel.onLabelQueryChange(query) },
        interaction = interaction,
        genreSuggestions = genreSuggestions,
        moodSuggestions = moodSuggestions,
        onGenreQueryChange = { query -> filterViewModel.onGenreQueryChange(query) },
        onMoodQueryChange = { query -> filterViewModel.onMoodQueryChange(query) },
        onTabChange = { tab, isSmart ->
            filterViewModel.updateType(tab)
        },
        areaSuggestions = areaSuggestions,
        onAreaQueryChange = { query -> filterViewModel.onAreaQueryChange(query) },
        onGlobalChange = {},
        isEditingSmart = true
    )
}