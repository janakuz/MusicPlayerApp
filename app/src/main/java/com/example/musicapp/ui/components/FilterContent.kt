package com.example.musicapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
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
import com.example.musicapp.data.repository.FilterLogic
import com.example.musicapp.data.repository.LibraryFilter
import com.example.musicapp.ui.HomeScreen
import com.example.musicapp.ui.screens.CountryPicker
import com.example.musicapp.ui.screens.GenrePicker
import com.example.musicapp.ui.viewmodels.CountryProvider
import com.example.musicapp.ui.viewmodels.FilterDefaults
import kotlin.math.roundToInt


@Composable
fun FilterDrawerContent(
    draft: LibraryFilter,
    potentialAlbumCount: Int,
    potentialArtistCount: Int,
    filterDefaults: FilterDefaults,
    labelSuggestions: List<String>,
    onDraftChange: (LibraryFilter) -> Unit,
    onApply: () -> Unit,
    onLabelQueryChange: (String) -> Unit,
    interaction: MutableInteractionSource,
    genreSuggestions: List<String>,
    onGenreQueryChange: (String) -> Unit,
    onTabChange: (String) -> Unit,
) {
    val dummyFocusRequester = remember { FocusRequester() }

    val tabs = listOf(HomeScreen.Artists, HomeScreen.Albums)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column() {

        TabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            tabs.forEachIndexed { index, screen ->
                Tab(
                    text = { Text(stringResource(screen.title)) },
                    selected = index == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                        onTabChange(tabs[index].name)
                    }
                )
            }
        }

        when {

            (selectedTabIndex == tabs.indexOf(HomeScreen.Artists)) -> LazyColumn(
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        isFiltering = true
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
                    Button(
                        onClick = onApply,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show $potentialArtistCount Results")
                    }
                }

            }

            (selectedTabIndex == tabs.indexOf(HomeScreen.Albums)) -> LazyColumn(
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        draft,
                        filterDefaults,
                        interaction,
                        onDraftChange
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show $potentialAlbumCount Results")
                    }
                }

            }
        }
    }

}


@Composable
fun DateRangeSection(
    draft: LibraryFilter,
    filterDefaults: FilterDefaults,
    interaction: MutableInteractionSource,
    onDraftChange: (LibraryFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text("Release Years")

        FlowRow {
            draft.dateRanges.forEach { range ->
                InputChip(
                    selected = true,
                    onClick = { /* Maybe edit? */ },
                    label = { Text("${range.first} - ${range.last}") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close, "Remove",
                            Modifier.clickable {
                                val newList = draft.dateRanges.filter { it != range }
                                onDraftChange(draft.copy(dateRanges = newList))
                            }
                        )
                    }
                )
            }
        }

        DateRangePicker(draft, filterDefaults, interaction, onDraftChange)

        IconButton(onClick = {
            val updatedSaved = draft.dateRanges + listOf<IntRange>(draft.activeRange)
            onDraftChange(
                draft.copy(
                    dateRanges = updatedSaved,
                    activeRange = filterDefaults.minYear..filterDefaults.maxYear
                )
            )
        }) {
            Icon(Icons.Default.Add, "Add another range")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    draft: LibraryFilter,
    filterDefaults: FilterDefaults,
    interaction: MutableInteractionSource,
    onDraftChange: (LibraryFilter) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "From: ${draft.activeRange.first}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "To: ${draft.activeRange.last}",
                style = MaterialTheme.typography.labelLarge
            )
        }

        var lastStart by remember(draft.activeRange) { mutableIntStateOf(draft.activeRange.first) }
        var lastEnd by remember(draft.activeRange) { mutableIntStateOf(draft.activeRange.last) }

        RangeSlider(
            value = lastStart.toFloat()..lastEnd.toFloat(),
            onValueChange = { range ->
                val newStart = range.start.roundToInt()
                val newEnd = range.endInclusive.roundToInt()

                if (newStart != lastStart || newEnd != lastEnd) {
                    lastStart = newStart
                    lastEnd = newEnd

                    onDraftChange(draft.copy(activeRange = newStart..newEnd))
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
            valueRange = filterDefaults.minYear.toFloat()..filterDefaults.maxYear.toFloat(),
            steps = (filterDefaults.maxYear - filterDefaults.minYear) - 1
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
        Text("Countries", style = MaterialTheme.typography.labelMedium)

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