package com.example.musicapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.musicapp.data.repository.DefunctFilterStatus
import com.example.musicapp.data.repository.FilterLogic
import com.example.musicapp.data.repository.LibraryFilter
import com.example.musicapp.ui.HomeScreen
import com.example.musicapp.ui.screens.GenrePicker
import com.example.musicapp.ui.viewmodels.CountryProvider
import com.example.musicapp.ui.viewmodels.FilterDefaults
import kotlin.math.roundToInt
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.draw.rotate
import com.example.musicapp.R
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.remote.dto.Key
import com.example.musicapp.data.repository.Instrumental
import com.example.musicapp.data.repository.VoiceGender
import kotlin.math.max
import kotlin.math.min


enum class FilterType {
    ARTISTS, ALBUMS, TRACKS
}


enum class FilterTabs(@StringRes val title: Int, val type: FilterType) {
    Artists(title = R.string.artists, FilterType.ARTISTS),
    Albums(title = R.string.albums, FilterType.ALBUMS),
    Tracks(title = R.string.tracks, FilterType.TRACKS),
}


@Composable
fun FilterDrawerContent(
    draft: LibraryFilter,
    filterType: FilterType,
    potentialAlbumCount: Int,
    potentialArtistCount: Int,
    potentialTrackCount: Int,
    filterDefaults: FilterDefaults,
    labelSuggestions: List<String>,
    onDraftChange: (LibraryFilter) -> Unit,
    onApply: () -> Unit,
    onLabelQueryChange: (String) -> Unit,
    interaction: MutableInteractionSource,
    genreSuggestions: List<String>,
    moodSuggestions: List<String>,
    onGenreQueryChange: (String) -> Unit,
    onMoodQueryChange: (String) -> Unit,
    onTabChange: (FilterType) -> Unit,
    areaSuggestions: List<AreaHierarchy>,
    onAreaQueryChange: (String) -> Unit,
) {
    val dummyFocusRequester = remember { FocusRequester() }

    val tabs = listOf(FilterTabs.Artists, FilterTabs.Albums, FilterTabs.Tracks)
    val index = when (filterType) {
        FilterType.ARTISTS -> 0
        FilterType.ALBUMS -> 1
        FilterType.TRACKS -> 2
    }
    var selectedTabIndex by remember(filterType) { mutableIntStateOf(index) }
    var acousticSectionExpanded by remember { mutableStateOf(false) }

    Column() {

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, screen ->
                Tab(
                    text = { Text(stringResource(screen.title)) },
                    selected = index == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                        onTabChange(tabs[index].type)
                    }
                )
            }
        }

        when {

            (selectedTabIndex == tabs.indexOf(FilterTabs.Artists)) -> LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .focusRequester(dummyFocusRequester)
                    .focusable()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        dummyFocusRequester.requestFocus()
                    }
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Match All")
                        Switch(
                            checked = draft.logic == FilterLogic.OR,
                            onCheckedChange = {
                                val newDraft =
                                    draft.copy(logic = if (draft.logic == FilterLogic.OR) FilterLogic.AND else FilterLogic.OR)
                                onDraftChange(newDraft)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text("Match Any")
                    }
                }


                item {
                    GenrePicker(
                        genres = draft.selectedGenres.toList(),
                        suggestions = genreSuggestions,
                        onGenreQueryChange = { query -> onGenreQueryChange(query) },
                        onGenresChange = { newGenres ->
                            onDraftChange(draft.copy(selectedGenres = newGenres.toSet()))
                            onGenreQueryChange("")
                        },
                        label = "Genre",
                        titleCase = true,
                        isFiltering = true,
                    )
                }

                item {
                    MultiCountryPicker(
                        onCountriesChange = { newCodes ->
                            onDraftChange(draft.copy(selectedCountries = newCodes.toSet()))
                        },
                        selectedCountryCodes = draft.selectedCountries.toList()
                    )
                }

                item {
                    MultiAreaPicker(
                        selectedAreas = draft.selectedAreas,
                        suggestions = areaSuggestions,
                        onQueryChange = onAreaQueryChange,
                        onAreasChange = { newAreas ->
                            onDraftChange(draft.copy(selectedAreas = newAreas))
                        }
                    )
                }


                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Band Status", style = MaterialTheme.typography.titleMedium)

                        MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = DefunctFilterStatus.entries.toTypedArray()
                            options.forEachIndexed { index, status ->
                                val label = when (status) {
                                    DefunctFilterStatus.ALL -> "All"
                                    DefunctFilterStatus.ACTIVE -> "Active Only"
                                    DefunctFilterStatus.DEFUNCT -> "Defunct Only"
                                }
                                SegmentedButton(
                                    checked = status == draft.defunctStatus,
                                    onCheckedChange = { onDraftChange(draft.copy(defunctStatus = status)) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = options.size
                                    )
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }

                item {
                    DateRangeSection(
                        title = "Years Formed",
                        savedRanges = draft.artistFormedRanges,
                        activeRange = draft.activeArtistStartRange,
                        minYear = filterDefaults.minYearArtists,
                        maxYear = filterDefaults.maxYearArtists,
                        interaction = interaction,
                        onRangeCommitted = { newChipsList ->
                            onDraftChange(
                                draft.copy(
                                    artistFormedRanges = newChipsList,
                                    activeArtistStartRange = filterDefaults.minYearArtists..filterDefaults.maxYearArtists
                                )
                            )
                        },
                        onActiveRangeSliderChange = { draggedRange ->
                            onDraftChange(draft.copy(activeArtistStartRange = draggedRange))
                        }
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = draft.defunctStatus == DefunctFilterStatus.DEFUNCT,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        DateRangeSection(
                            title = "Years Disbanded",
                            savedRanges = draft.artistEndedRanges,
                            activeRange = draft.activeArtistEndRange,
                            minYear = filterDefaults.minYearArtists,
                            maxYear = filterDefaults.maxYearArtists,
                            interaction = interaction,
                            onRangeCommitted = { newChipsList ->
                                onDraftChange(
                                    draft.copy(
                                        artistEndedRanges = newChipsList,
                                        activeArtistEndRange = filterDefaults.minYearArtists..filterDefaults.maxYearArtists
                                    )
                                )
                            },
                            onActiveRangeSliderChange = { draggedRange ->
                                onDraftChange(draft.copy(activeArtistEndRange = draggedRange))
                            }
                        )

                    }
                }


                item {
                    Button(
                        onClick = onApply,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("Show $potentialArtistCount Results")
                    }
                }

            }

            (selectedTabIndex == tabs.indexOf(FilterTabs.Albums)) -> LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .focusRequester(dummyFocusRequester)
                    .focusable()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        dummyFocusRequester.requestFocus()
                    }
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Match All")
                        Switch(
                            checked = draft.logic == FilterLogic.OR,
                            onCheckedChange = {
                                val newDraft =
                                    draft.copy(logic = if (draft.logic == FilterLogic.OR) FilterLogic.AND else FilterLogic.OR)
                                onDraftChange(newDraft)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text("Match Any")
                    }
                }

                item {
                    GenrePicker(
                        genres = draft.selectedLabels.toList(),
                        suggestions = labelSuggestions,
                        onGenreQueryChange = { query -> onLabelQueryChange(query) },
                        onGenresChange = { newLabels ->
                            onDraftChange(draft.copy(selectedLabels = newLabels.toSet()))
                            onLabelQueryChange("")
                        },
                        label = "Label",
                        titleCase = false, isFiltering = true
                    )
                }

                item {
                    DateRangeSection(
                        title = "Release Years",
                        savedRanges = draft.dateRanges,
                        activeRange = draft.activeRange,
                        minYear = filterDefaults.minYear,
                        maxYear = filterDefaults.maxYear,
                        interaction = interaction,
                        onRangeCommitted = { newChipsList ->
                            onDraftChange(
                                draft.copy(
                                    dateRanges = newChipsList,
                                    activeRange = filterDefaults.minYear..filterDefaults.maxYear
                                )
                            )
                        },
                        onActiveRangeSliderChange = { draggedRange ->
                            onDraftChange(draft.copy(activeRange = draggedRange))
                        }
                    )
                }

                item {
                    GenrePicker(
                        genres = draft.selectedGenres.toList(),
                        suggestions = genreSuggestions,
                        onGenreQueryChange = { query -> onGenreQueryChange(query) },
                        onGenresChange = { newGenres ->
                            onDraftChange(draft.copy(selectedGenres = newGenres.toSet()))
                            onGenreQueryChange("")
                        },
                        label = "Genre",
                        titleCase = true,
                        isFiltering = true
                    )
                }




                item {
                    Button(
                        onClick = onApply,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("Show $potentialAlbumCount Results")
                    }
                }

            }

            (selectedTabIndex == tabs.indexOf(FilterTabs.Tracks)) -> LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .focusRequester(dummyFocusRequester)
                    .focusable()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        dummyFocusRequester.requestFocus()
                    }
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Match All")
                        Switch(
                            checked = draft.logic == FilterLogic.OR,
                            onCheckedChange = {
                                val newDraft =
                                    draft.copy(logic = if (draft.logic == FilterLogic.OR) FilterLogic.AND else FilterLogic.OR)
                                onDraftChange(newDraft)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text("Match Any")
                    }
                }

                item {
                    GenrePicker(
                        genres = draft.selectedMoods.toList(),
                        suggestions = moodSuggestions,
                        onGenreQueryChange = { query -> onMoodQueryChange(query) },
                        onGenresChange = { newMoods ->
                            onDraftChange(draft.copy(selectedMoods = newMoods.toSet()))
                            onMoodQueryChange("")
                        },
                        label = "Mood",
                        titleCase = true, isFiltering = true
                    )
                }


                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Instrumental", style = MaterialTheme.typography.titleMedium)

                        MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = Instrumental.entries.toTypedArray()
                            options.forEachIndexed { index, status ->
                                val label = when (status) {
                                    Instrumental.ANY -> "All"
                                    Instrumental.VOCAL -> "With Vocals"
                                    Instrumental.INSTRUMENTAL -> "Instrumental"
                                }
                                SegmentedButton(
                                    checked = status == draft.instrumental,
                                    onCheckedChange = { onDraftChange(draft.copy(instrumental = status)) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = options.size
                                    )
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Vocals", style = MaterialTheme.typography.titleMedium)

                        MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = VoiceGender.entries.toTypedArray()
                            options.forEachIndexed { index, voice ->
                                val label = when (voice) {
                                    VoiceGender.ALL -> "All"
                                    VoiceGender.MALE -> "Male"
                                    VoiceGender.FEMALE -> "Female"
                                    VoiceGender.MIXED -> "Mixed"
                                }
                                SegmentedButton(
                                    checked = voice == draft.voice,
                                    onCheckedChange = { onDraftChange(draft.copy(voice = voice)) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = options.size
                                    )
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }


                item {
                    DateRangeSection(
                        title = "BPM",
                        savedRanges = draft.bpmRanges,
                        activeRange = draft.activeBPMRange,
                        minYear = 40,
                        maxYear = 250,
                        interaction = interaction,
                        onRangeCommitted = { newChipsList ->
                            onDraftChange(
                                draft.copy(
                                    bpmRanges = newChipsList,
                                    activeBPMRange = 40..250
                                )
                            )
                        },
                        onActiveRangeSliderChange = { draggedRange ->
                            onDraftChange(draft.copy(activeBPMRange = draggedRange))
                        }
                    )
                }

                item{
                    KeyPicker(
                        selectedKeys = draft.selectedKeys,
                        activeKeySelection = draft.activeKeySelection,
                        onKeysChange = { newList ->
                            onDraftChange(
                                draft.copy(
                                    selectedKeys = newList,
                                    activeKeySelection = Key(null, null)
                                )
                            )
                        },
                        onActiveKeyChange = { selection ->
                            onDraftChange(draft.copy(activeKeySelection = selection))
                        }
                    )
                }


                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { acousticSectionExpanded = !acousticSectionExpanded }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = "Audio Features & Moods",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Danceability, engagement, and acoustic textures",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val rotationAngle by animateFloatAsState(
                                targetValue = if (acousticSectionExpanded) 180f else 0f,
                                label = "ArrowRotation"
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (acousticSectionExpanded) "Collapse" else "Expand",
                                modifier = Modifier.rotate(rotationAngle)
                            )
                        }

                        if (acousticSectionExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Acoustic Profiles",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                FeatureRangeSection(
                                    title = "Approachability",
                                    savedRanges = draft.approachabilityRanges,
                                    activeRange = draft.activeApproachabilityRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                approachabilityRanges = newChipsList,
                                                activeApproachabilityRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activeApproachabilityRange = draggedRange))
                                    }
                                )

                                FeatureRangeSection(
                                    title = "Engagement",
                                    savedRanges = draft.engagementRanges,
                                    activeRange = draft.activeEngagementRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                engagementRanges = newChipsList,
                                                activeEngagementRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activeEngagementRange = draggedRange))
                                    }
                                )

                                FeatureRangeSection(
                                    title = "Danceability",
                                    savedRanges = draft.danceabilityRanges,
                                    activeRange = draft.activeDanceabilityRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                danceabilityRanges = newChipsList,
                                                activeDanceabilityRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activeDanceabilityRange = draggedRange))
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                Text(
                                    text = "Estimated Mood Textures",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                FeatureRangeSection(
                                    title = "Mood Aggressive",
                                    savedRanges = draft.moodAggressiveRanges,
                                    activeRange = draft.activeAggressiveRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                moodAggressiveRanges = newChipsList,
                                                activeAggressiveRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activeAggressiveRange = draggedRange))
                                    }
                                )

                                FeatureRangeSection(
                                    title = "Mood Happy",
                                    savedRanges = draft.moodHappyRanges,
                                    activeRange = draft.activeHappyRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                moodHappyRanges = newChipsList,
                                                activeHappyRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activeHappyRange = draggedRange))
                                    }
                                )

                                FeatureRangeSection(
                                    title = "Mood Party",
                                    savedRanges = draft.moodPartyRanges,
                                    activeRange = draft.activePartyRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                moodPartyRanges = newChipsList,
                                                activePartyRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activePartyRange = draggedRange))
                                    }
                                )

                                FeatureRangeSection(
                                    title = "Mood Relaxed",
                                    savedRanges = draft.moodRelaxedRanges,
                                    activeRange = draft.activeRelaxedRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                moodRelaxedRanges = newChipsList,
                                                activeRelaxedRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activeRelaxedRange = draggedRange))
                                    }
                                )

                                FeatureRangeSection(
                                    title = "Mood Sad",
                                    savedRanges = draft.moodSadRanges,
                                    activeRange = draft.activeSadRange,
                                    interaction = interaction,
                                    onRangeCommitted = { newChipsList ->
                                        onDraftChange(
                                            draft.copy(
                                                moodSadRanges = newChipsList,
                                                activeSadRange = 0f..1f
                                            )
                                        )
                                    },
                                    onActiveRangeSliderChange = { draggedRange ->
                                        onDraftChange(draft.copy(activeSadRange = draggedRange))
                                    }
                                )
                            }
                        }
                    }
                }



                item {
                    Button(
                        onClick = onApply,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("Show $potentialTrackCount Results")
                    }
                }

            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyPicker(
    selectedKeys: List<Key>,
    activeKeySelection: Key,
    onKeysChange: (List<Key>) -> Unit,
    onActiveKeyChange: (Key) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Key Signature", style = MaterialTheme.typography.titleMedium)



        if (selectedKeys.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                selectedKeys.forEach { keyFilter ->
                    val displayLabel = when {
                        keyFilter.key != null && keyFilter.scale != null ->
                            "${keyFilter.key} ${keyFilter.scale.replaceFirstChar { it.uppercase() }}"
                        keyFilter.key != null -> "${keyFilter.key} (Any Scale)"
                        keyFilter.scale != null -> "Any ${keyFilter.scale.replaceFirstChar { it.uppercase() }}"
                        else -> "Any Key"
                    }

                    InputChip(
                        selected = true,
                        onClick = { onKeysChange(selectedKeys.filter { it != keyFilter }) },
                        label = { Text(displayLabel) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            var noteDropdownExpanded by remember { mutableStateOf(false) }
            var scaleDropdownExpanded by remember { mutableStateOf(false) }

            val rootNotes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
            val scales = listOf("major", "minor")


            ExposedDropdownMenuBox(
                expanded = noteDropdownExpanded,
                onExpandedChange = { noteDropdownExpanded = !noteDropdownExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = activeKeySelection.key ?: "Any",
                    onValueChange = {},
                    label = { Text("Note") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = noteDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)

                )
                ExposedDropdownMenu(
                    expanded = noteDropdownExpanded,
                    onDismissRequest = { noteDropdownExpanded = false }
                ) {
                    rootNotes.forEach { note ->
                        DropdownMenuItem(
                            text = { Text(note) },
                            onClick = {
                                onActiveKeyChange(Key(note, activeKeySelection.scale))
                                noteDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = scaleDropdownExpanded,
                onExpandedChange = { scaleDropdownExpanded = !scaleDropdownExpanded },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = (activeKeySelection.scale ?: "Any").replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    label = { Text("Scale") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scaleDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = scaleDropdownExpanded,
                    onDismissRequest = { scaleDropdownExpanded = false }
                ) {
                    scales.forEach { scale ->
                        DropdownMenuItem(
                            text = { Text(scale.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onActiveKeyChange(Key(activeKeySelection.key, scale))
                                scaleDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = {
                onKeysChange(selectedKeys + activeKeySelection)
            },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Key Filter")
        }
    }
}

@Composable
fun DateRangeSection(
    title: String,
    savedRanges: List<IntRange>,
    activeRange: IntRange,
    minYear: Int,
    maxYear: Int,
    interaction: MutableInteractionSource,
    onRangeCommitted: (updatedRanges: List<IntRange>) -> Unit,
    onActiveRangeSliderChange: (IntRange) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {

            savedRanges.forEach { range ->
                InputChip(
                    selected = true,
                    onClick = { /* Maybe edit? */ },
                    label = { Text("${range.first} - ${range.last}") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close, "Remove",
                            Modifier.clickable {
                                val newList = savedRanges.filter { it != range }
                                onRangeCommitted(newList)
                            }
                        )
                    }
                )
            }
        }

        DateRangePicker(activeRange, minYear, maxYear, interaction, onActiveRangeSliderChange)

        IconButton(onClick = {
            val updatedSaved = savedRanges + listOf<IntRange>(activeRange)
            onRangeCommitted(updatedSaved)
        }) {
            Icon(Icons.Default.Add, "Add another range")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    activeRange: IntRange,
    minYear: Int,
    maxYear: Int,
    interaction: MutableInteractionSource,
    onRangeChange: (IntRange) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "From: ${activeRange.first}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "To: ${activeRange.last}",
                style = MaterialTheme.typography.labelLarge
            )
        }

        var lastStart by remember(activeRange) { mutableIntStateOf(activeRange.first) }
        var lastEnd by remember(activeRange) { mutableIntStateOf(activeRange.last) }

        RangeSlider(
            value = lastStart.toFloat()..lastEnd.toFloat(),
            onValueChange = { range ->
                val newStart = range.start.roundToInt()
                val newEnd = range.endInclusive.roundToInt()

                if (newStart != lastStart || newEnd != lastEnd) {
                    lastStart = newStart
                    lastEnd = newEnd

                    onRangeChange(newStart..newEnd)
                }
            },
            startInteractionSource = interaction,
            endInteractionSource = interaction,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                    }
                },
            valueRange = minYear.toFloat()..maxYear.toFloat(),
            steps = (maxYear - minYear) - 1
        )
    }
}


@Composable
fun FeatureRangeSection(
    title: String,
    savedRanges: List<ClosedFloatingPointRange<Float>>,
    activeRange: ClosedFloatingPointRange<Float>,
    interaction: MutableInteractionSource,
    onRangeCommitted: (updatedRanges: List<ClosedFloatingPointRange<Float>>) -> Unit,
    onActiveRangeSliderChange: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {

            savedRanges.forEach { range ->
                InputChip(
                    selected = true,
                    onClick = { /* Maybe edit? */ },
                    label = { Text("${(range.start * 100).toInt()} - ${(range.endInclusive * 100).toInt()}") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close, "Remove",
                            Modifier.clickable {
                                val newList = savedRanges.filter { it != range }
                                onRangeCommitted(newList)
                            }
                        )
                    }
                )
            }
        }

        FloatRangePicker(activeRange, interaction, onActiveRangeSliderChange)

        IconButton(onClick = {
            val updatedSaved = savedRanges + listOf<ClosedFloatingPointRange<Float>>(activeRange)
            onRangeCommitted(updatedSaved)
        }) {
            Icon(Icons.Default.Add, "Add another range")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatRangePicker(
    activeRange: ClosedFloatingPointRange<Float>,
    interaction: MutableInteractionSource,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "From: ${(activeRange.start * 100).toInt()}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "To: ${(activeRange.endInclusive * 100).toInt()}",
                style = MaterialTheme.typography.labelLarge
            )
        }

        var lastStart by remember(activeRange) { mutableFloatStateOf(activeRange.start) }
        var lastEnd by remember(activeRange) { mutableFloatStateOf(activeRange.endInclusive) }

        RangeSlider(
            value = lastStart.toFloat()..lastEnd.toFloat(),
            onValueChange = { range ->
                val newStart = range.start
                val newEnd = range.endInclusive

                if (newStart != lastStart || newEnd != lastEnd) {
                    lastStart = newStart
                    lastEnd = newEnd

                    onRangeChange(newStart..newEnd)
                }
            },
            startInteractionSource = interaction,
            endInteractionSource = interaction,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                    }
                },
            valueRange = 0f..1f,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiCountryPicker(
    selectedCountryCodes: List<String>,
    onCountriesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val dummyFocusRequester = remember { FocusRequester() }

    val suggestions = remember(textFieldValue) {
        if (textFieldValue.isBlank()) {
            CountryProvider.allCountries
        } else {
            CountryProvider.allCountries.filter { country ->
                country.name.contains(textFieldValue, ignoreCase = true) ||
                        country.code.contains(textFieldValue, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(dummyFocusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                dummyFocusRequester.requestFocus()
            }
    ) {
        Text("Countries", style = MaterialTheme.typography.titleMedium)

        FlowRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            selectedCountryCodes.forEach { countryCode ->
                val countryData = remember(countryCode) {
                    CountryProvider.allCountries.find { it.code == countryCode }
                }

                InputChip(
                    selected = true,
                    onClick = {
                        onCountriesChange(selectedCountryCodes.filter { it != countryCode })
                    },
                    label = {
                        Text(countryData?.let { "${it.flag}  ${it.name}" } ?: countryCode)
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove country",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded && suggestions.isNotEmpty(),
            onExpandedChange = {
                expanded = it
                if (!expanded) {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                }
            }
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    expanded = true
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth(),
                label = { Text("Filter by Countries") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val topMatch = suggestions.firstOrNull()
                    if (topMatch != null && !selectedCountryCodes.contains(topMatch.code)) {
                        onCountriesChange(selectedCountryCodes + topMatch.code)
                        textFieldValue = ""
                    }
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            )

            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ) {
                    suggestions.forEach { country ->
                        if (!selectedCountryCodes.contains(country.code)) {
                            DropdownMenuItem(
                                text = { Text("${country.flag}   ${country.name}") },
                                onClick = {
                                    onCountriesChange(selectedCountryCodes + country.code)
                                    textFieldValue = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MultiAreaPicker(
    selectedAreas: List<AreaHierarchy>,
    suggestions: List<AreaHierarchy>,
    onQueryChange: (String) -> Unit,
    onAreasChange: (List<AreaHierarchy>) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val dummyFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(dummyFocusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                dummyFocusRequester.requestFocus()
            }
    ) {
        Text("Areas / Scenes", style = MaterialTheme.typography.titleMedium)

        FlowRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            selectedAreas.forEach { area ->
                val chipText = remember(area) {
                    listOfNotNull(
                        area.cityName.takeIf { !it.isNullOrEmpty() },
                        area.stateName.takeIf { !it.isNullOrEmpty() }
                            ?: area.countryName.takeIf { !it.isNullOrEmpty() }
                    ).joinToString(", ")
                }

                InputChip(
                    selected = true,
                    onClick = { onAreasChange(selectedAreas.filter { it != area }) },
                    label = { Text(chipText) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove area",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded && suggestions.isNotEmpty(),
            onExpandedChange = {
                expanded = it
                if (!expanded) {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                }
            }
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onQueryChange(it)
                    expanded = true
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth(),
                label = { Text("Search Areas (City, State, Region...)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val topMatch = suggestions.firstOrNull()
                    if (topMatch != null && !selectedAreas.any { it.gid == topMatch.gid }) {
                        onAreasChange(selectedAreas + topMatch)
                        textFieldValue = ""
                    }
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            )

            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ) {
                    suggestions.forEach { suggestion ->
                        val isAlreadySelected = selectedAreas.any { it.gid == suggestion.gid }

                        if (!isAlreadySelected) {
                            val suggestionText = remember(suggestion) {
                                listOfNotNull(
                                    suggestion.cityName.takeIf { !it.isNullOrEmpty() },
                                    suggestion.countyName.takeIf { !it.isNullOrEmpty() },
                                    suggestion.stateName.takeIf { !it.isNullOrEmpty() },
                                    suggestion.countryName.takeIf { !it.isNullOrEmpty() }
                                ).joinToString(", ")
                            }

                            DropdownMenuItem(
                                text = { Text(suggestionText) },
                                onClick = {
                                    onAreasChange(selectedAreas + suggestion)
                                    textFieldValue = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}