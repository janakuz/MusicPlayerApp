package com.example.musicapp.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
import com.example.musicapp.ui.components.EditTopBar
import com.example.musicapp.ui.viewmodels.ArtistEditViewModel
import com.example.musicapp.ui.viewmodels.CountryProvider
import com.example.musicapp.ui.viewmodels.NameEditUiState
import kotlin.math.absoluteValue


@Composable
fun ArtistImagePicker(
    images: List<String>,
    currentSelection: String,
    onImageSelected: (String) -> Unit
) {
    val initialPage = images.indexOf(currentSelection).coerceAtLeast(0)
    Log.d("first image", currentSelection)
    Log.d("first image", images.joinToString())
    Log.d("first image", initialPage.toString())
    val pagerState = rememberPagerState(initialPage = initialPage) { images.size }

    LaunchedEffect(images, initialPage) {
        if (images.isNotEmpty()) {
            pagerState.scrollToPage(initialPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (images.isNotEmpty()) {
            onImageSelected(images[pagerState.currentPage])
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(horizontal = 80.dp),
            pageSpacing = 16.dp
        ) { page ->
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            Card(
                modifier = Modifier
                    .graphicsLayer {
                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${images.size}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPicker(
    selectedCountryCode: String? = null,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val currentSelectionDisplay = remember(selectedCountryCode) {
        CountryProvider.allCountries.find { it.code == selectedCountryCode }?.let {
            "${it.flag}  ${it.name}"
        } ?: ""
    }

    var searchQuery by remember(currentSelectionDisplay) {
        mutableStateOf(currentSelectionDisplay)
    }

    val filteredCountries = remember(searchQuery) {
        CountryProvider.allCountries.filter { country ->
            country.name.contains(searchQuery, ignoreCase = true) ||
                    country.code.contains(searchQuery, ignoreCase = true)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = true
            },
            label = { Text("Country of Origin") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            singleLine = true
        )

        if (filteredCountries.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = currentSelectionDisplay
                }
            ) {
                filteredCountries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text("${country.flag}   ${country.name}") },
                        onClick = {
                            searchQuery = "${country.flag}  ${country.name}"
                            onCountrySelected(country.code)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistBioEditor(
    draftBio: String,
    lastFmBio: String,
    discogsBio: String,
    onBioChange: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Draft", "Last.fm", "Discogs")

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> OutlinedTextField(
                value = draftBio,
                onValueChange = onBioChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                label = { Text("Edit Biography") }
            )

            1 -> BioReferenceText(lastFmBio, onCopy = onBioChange)
            2 -> BioReferenceText(discogsBio, onCopy = onBioChange)
        }
    }
}

@Composable
fun BioReferenceText(text: String, onCopy: (String) -> Unit) {
    Column {
        Text(
            text = if (text.isBlank()) "No data found." else text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .verticalScroll(rememberScrollState())
        )
        if (text.isNotBlank()) {
            TextButton(onClick = { onCopy(text) }) {
                Text("Use this bio")
            }
        }
    }
}

@Composable
fun ArtistDisambiguationDialog(
    matches: List<ArtistSearchInfo>,
    onArtistSelected: (ArtistSearchInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Multiple Artists Found") },
        text = {
            LazyColumn {
                items(matches) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onArtistSelected(artist) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (!artist.disambiguation.isNullOrEmpty()) {
                                Text(
                                    text = artist.disambiguation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!artist.country.isNullOrEmpty()) {
                                Text(
                                    text = artist.country,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ArtistEditScreen(
    onNavigateBack: () -> Unit,
) {
    val artistEditViewModel: ArtistEditViewModel = hiltViewModel()

    val artistEditUiState by artistEditViewModel.uiState.collectAsState()
    val nameEditWorkflowState by artistEditViewModel.workflowState.collectAsState()
    val images = artistEditUiState.discogsImages.map { it.resourceUrl }
    val canSave by artistEditViewModel.canSave.collectAsState()
    val suggestions by artistEditViewModel.genreSuggestions.collectAsState()

    var showDiscardDialog by remember { mutableStateOf(false) }


    val handleBack = {
        if (canSave)
            showDiscardDialog = true
        else {
            onNavigateBack()
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to leave?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            EditTopBar(
                title = "Edit Artist",
                onBackClick = handleBack
            )
        },
        floatingActionButton = {
            if (canSave) {
                ExtendedFloatingActionButton(
                    onClick = {
                        artistEditViewModel.onSave(onNavigateBack)
                    },
                    text = { Text("Save") },
                    icon = { Icon(Icons.Default.Check, null) }
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                    ArtistImagePicker(
                        images = images,
                        currentSelection = artistEditUiState.draftImageUrl,
                        onImageSelected = { selected -> artistEditViewModel.onImageChange(selected) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = artistEditUiState.name,
                        onValueChange = { artistEditViewModel.onNameChange(it) },
                        label = { Text("Artist Name") },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                item {
                    GenrePicker(
                        genres = artistEditUiState.draftGenres,
                        onGenresChange = { list -> artistEditViewModel.onGenresChange(list) },
                        suggestions = suggestions,
                        onGenreQueryChange = { query -> artistEditViewModel.onGenreQueryChange(query) },
                        label = "Genre"
                    )
                }

                item {
                    CountryPicker(
                        selectedCountryCode = artistEditUiState.draftCountryCode,
                        onCountrySelected = { code -> artistEditViewModel.onCountryChange(code) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = artistEditUiState.draftHomeCity.orEmpty(),
                            onValueChange = { artistEditViewModel.onHomeCityChange(it) },
                            label = { Text("Home City") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = artistEditUiState.draftCurrentCity.orEmpty(),
                            onValueChange = { artistEditViewModel.onCurrentCityChange(it) },
                            label = { Text("Current City") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = artistEditUiState.draftActiveStartYear.orEmpty(),
                                onValueChange = { artistEditViewModel.onActiveStartYearChange(it) },
                                label = { Text("Year Formed") },
                                placeholder = { Text("YYYY") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = artistEditUiState.draftActiveEndYear.orEmpty(),
                                onValueChange = { artistEditViewModel.onActiveEndYearChange(it) },
                                label = { Text("Year Disbanded") },
                                placeholder = { Text("YYYY") },
                                enabled = artistEditUiState.draftIsDefunct,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (artistEditUiState.draftIsDefunct) "❌ Band is Defunct" else "⚡ Band is Active",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked = artistEditUiState.draftIsDefunct,
                                onCheckedChange = { isChecked -> artistEditViewModel.onDefunctStatusChange(isChecked) }
                            )
                        }
                    }
                }

                item {
                    ArtistBioEditor(
                        draftBio = artistEditUiState.draftBio,
                        lastFmBio = artistEditUiState.lastFmBio,
                        discogsBio = artistEditUiState.discogsBio,
                        onBioChange = { newBio -> artistEditViewModel.onBioChange(newBio) }
                    )
                }

            }

            when (nameEditWorkflowState) {
                is NameEditUiState.Saving -> {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Saving...", color = Color.White)
                        }
                    }
                    BackHandler(enabled = true) { }
                }

                is NameEditUiState.DisambiguationNeeded -> {
                    ArtistDisambiguationDialog(
                        matches = (nameEditWorkflowState as NameEditUiState.DisambiguationNeeded).matches,
                        onArtistSelected = { selectedArtist ->
                            artistEditViewModel.onArtistSelected(selectedArtist, onNavigateBack)
                        },
                        onDismiss = {
                            artistEditViewModel.resetName()
                        }
                    )
                }

                is NameEditUiState.Error -> {
                }

                else -> {}
            }
        }

    }

    BackHandler(enabled = canSave) {
        showDiscardDialog = true
    }

}

