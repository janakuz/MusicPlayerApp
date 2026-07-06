package com.example.musicapp.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.EditTopBar
import com.example.musicapp.ui.viewmodels.AlbumArtistEditUiState
import com.example.musicapp.ui.viewmodels.SearchSheetState
import com.example.musicapp.ui.viewmodels.TrackEditViewModel
import com.example.musicapp.ui.viewmodels.TrackMultiEditViewModel
import com.example.musicapp.ui.viewmodels.VoiceState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextOverflow
import com.example.musicapp.data.remote.dto.LRCLibResponse
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackEditScreen(
    onNavigateBack: () -> Unit,
) {
    val trackEditViewModel: TrackEditViewModel = hiltViewModel()

    val trackEditUiState by trackEditViewModel.uiState.collectAsState()
    val albumArtistEditWorkflowState by trackEditViewModel.workflowState.collectAsState()
    val canSave by trackEditViewModel.canSave.collectAsState()
    val suggestions by trackEditViewModel.moodSuggestions.collectAsState()
    val searchSheetState by trackEditViewModel.lyricsSearchState.collectAsState()

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }


    val handleBack = {
        if (canSave)
            showDiscardDialog = true
        else onNavigateBack()
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
                title = "Edit Track",
                onBackClick = handleBack
            )
        },
        floatingActionButton = {
            if (canSave) {
                ExtendedFloatingActionButton(
                    onClick = {
                        trackEditViewModel.onSave(onNavigateBack)
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
                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                        Text(
                            text = "File path",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = trackEditUiState.filePath,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace, // Use Monospace for paths
                                        lineHeight = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }


                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedTextField(
                            value = trackEditUiState.draftTrackNumber,
                            onValueChange = { trackEditViewModel.onTrackNumChange(it) },
                            enabled = true,
                            label = { Text("No.") },
                            modifier = Modifier.width(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )


                        OutlinedTextField(
                            value = trackEditUiState.title,
                            onValueChange = { trackEditViewModel.onTitleChange(it) },
                            label = { Text("Track Title") },
                            enabled = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }



                item {
                    OutlinedTextField(
                        value = trackEditUiState.artist,
                        onValueChange = { trackEditViewModel.onArtistChange(it) },
                        label = { Text("Artist") },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }


                item {
                    OutlinedTextField(
                        value = trackEditUiState.album,
                        onValueChange = { trackEditViewModel.onAlbumChange(it) },
                        label = { Text("Album") },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                item {
                    GenrePicker(
                        genres = trackEditUiState.draftMoods,
                        onGenresChange = { list -> trackEditViewModel.onMoodsChange(list) },
                        suggestions = suggestions,
                        onGenreQueryChange = { query -> trackEditViewModel.onMoodQueryChange(query) },
                        label = "Mood"
                    )
                }

                item {
                    val voiceState = VoiceState(trackEditUiState.instrumental, trackEditUiState.voice)
                    InstrumentalAndVoiceSection(
                        voiceState,
                        onInstrumentalChange = {value -> trackEditViewModel.onInstrumentalChange(value)},
                        onVoiceChange = {voice -> trackEditViewModel.onVoiceChange(voice)}
                        )
                }


                item {
                    val rootNotes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
                    val scales = listOf("major", "minor")

                    var bpmInput by remember { mutableStateOf(trackEditUiState.bpm?.toInt()?.toString() ?: "") }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = bpmInput,
                            onValueChange = { newValue ->
                                val digitsOnly = newValue.filter { it.isDigit() }

                                if (digitsOnly.isEmpty()) {
                                    bpmInput = ""
                                } else {
                                    val parsedBpm = digitsOnly.toIntOrNull()
                                    if (parsedBpm != null && parsedBpm <= 250) {
                                        bpmInput = digitsOnly
                                        trackEditViewModel.onBPMChange(parsedBpm)
                                    }
                                }
                            },
                            label = { Text("BPM") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            isError = bpmInput.isNotEmpty() && (bpmInput.toIntOrNull() ?: 0) < 40
                        )

                        var noteDropdownExpanded by remember { mutableStateOf(false) }
                        var scaleDropdownExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = noteDropdownExpanded,
                            onExpandedChange = { noteDropdownExpanded = !noteDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = trackEditUiState.note ?: "",
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
                                            trackEditViewModel.onKeyChange(note, trackEditUiState.scale ?: "")
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
                                value = (trackEditUiState.scale?: "").replaceFirstChar { it.uppercase() },
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
                                            trackEditViewModel.onKeyChange(trackEditUiState.note ?: "", scale)
                                            scaleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }


                item{
                    Column {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lyrics",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            TextButton(
                                onClick = {
                                    showSearchSheet = true
                                    trackEditViewModel.onSearch()
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Search LRCLib",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }



                        var selectedTab by remember { mutableIntStateOf(0) }
                        val tabs = listOf("Plain Lyrics", "Synced Lyrics")

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
                                value = trackEditUiState.currentLyrics?.plainLyrics ?: "",
                                onValueChange = { trackEditViewModel.onLyricsChange(it, trackEditUiState.currentLyrics?.syncedLyrics) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                label = { Text("Edit Plain Lyrics") }
                            )

                            1 -> OutlinedTextField(
                                value = trackEditUiState.currentLyrics?.syncedLyrics ?: "",
                                onValueChange = { trackEditViewModel.onLyricsChange(trackEditUiState.currentLyrics?.plainLyrics, it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                label = { Text("Edit Synced Lyrics") }
                            )
                        }
                    }

                }

            }

            if (showSearchSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSearchSheet = false },
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    AnimatedContent(
                        targetState = searchSheetState,
                        label = "SearchSheetTransition"
                    ) { state ->
                        when (state) {
                            is SearchSheetState.Loading -> {
                                Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is SearchSheetState.Results -> {
                                if (state.list.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                        Text("No lyrics found on LRCLib.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            Text("Select a match to preview", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                                        }
                                        items(state.list) { result ->
                                            ResultItemRow(
                                                result = result,
                                                onClick = { trackEditViewModel.onPreview(result) }
                                            )
                                        }
                                    }
                                }
                            }
                            is SearchSheetState.Preview -> {

                                LyricsPreviewContainer(
                                    result = state.selected,
                                    onBackClick = { trackEditViewModel.onBackLyricsPreview() },
                                    onApplyClick = {
                                                   plain, synced -> trackEditViewModel.onLyricsChange(plain, synced)
                                                   showSearchSheet = false },
                                )
                            }
                            is SearchSheetState.Error -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Search failed", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                                    Text(state.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            when (albumArtistEditWorkflowState) {
                is AlbumArtistEditUiState.Saving -> {
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

                is AlbumArtistEditUiState.DisambiguationNeeded -> {
                    ArtistDisambiguationDialog(
                        matches = (albumArtistEditWorkflowState as AlbumArtistEditUiState.DisambiguationNeeded).matches,
                        onArtistSelected = { selectedArtist ->
                            trackEditViewModel.onArtistSelected(selectedArtist, onNavigateBack)
                        },
                        onDismiss = {
                            trackEditViewModel.resetName()
                        }
                    )
                }

                is AlbumArtistEditUiState.Error -> {
                }

                else -> {}
            }
        }

    }


    BackHandler(enabled = canSave) {
        showDiscardDialog = true
    }

}

@Composable
fun InstrumentalAndVoiceSection(
    voiceState: VoiceState,
    onInstrumentalChange: (Boolean) -> Unit,
    onVoiceChange: (String) -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Instrumental",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Track contains no vocals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = voiceState.instrumental == true,
                onCheckedChange = { checked ->
                    onInstrumentalChange(checked)
                }
            )
        }

        AnimatedVisibility(
            visible = voiceState.instrumental != true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.0.dp)
            ) {
                Text(
                    text = "Vocals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.0.dp)
                ) {
                    val genderOptions = listOf("male", "female", "mixed")

                    genderOptions.forEach { option ->
                        FilterChip(
                            selected = (voiceState.voice == option),
                            onClick = { onVoiceChange(option) },
                            label = {
                                Text(text = option.replaceFirstChar { it.uppercase() })
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TrackMultiEditScreen(
    tracksToEdit: Set<Int>,
    onNavigateBack: () -> Unit
) {
    val trackMultiEditViewModel: TrackMultiEditViewModel = hiltViewModel()
    val currentVoiceState by trackMultiEditViewModel.voiceState.collectAsState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                trackMultiEditViewModel.onEditMultiple(currentVoiceState, tracksToEdit)
                onNavigateBack()
        }) {
                Icon(Icons.Default.Save, contentDescription = "Save new order")
            }
        }
    ) { _ ->
        if (tracksToEdit.isEmpty()) onNavigateBack()
        InstrumentalAndVoiceSection(
            voiceState = currentVoiceState,
            onInstrumentalChange = {value -> trackMultiEditViewModel.onInstrumentalChange(value) },
            onVoiceChange = {voice -> trackMultiEditViewModel.onVoiceChange(voice)},
        )
    }
}


@Composable
fun ResultItemRow(
    result: LRCLibResponse,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.trackName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${result.artistName} • ${result.albumName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val minutes = result.duration.toInt() / 60
                val seconds = result.duration.toInt() % 60
                Text(
                    text = String.format(Locale.ROOT, "%d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val hasSynced = !result.syncedLyrics.isNullOrBlank()
                val hasPlain = !result.plainLyrics.isNullOrBlank()

                if (hasSynced) {
                    LyricsFormatBadge(text = "LRC", isSynced = true)
                }
                if (hasPlain && !hasSynced) {
                    LyricsFormatBadge(text = "TXT", isSynced = false)
                }
            }
        }
    }
}

@Composable
private fun LyricsFormatBadge(text: String, isSynced: Boolean) {
    Surface(
        color = if (isSynced) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (isSynced) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}


@Composable
fun LyricsPreviewContainer(
    result: LRCLibResponse,
    onBackClick: () -> Unit,
    onApplyClick: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var previewTab by remember { mutableIntStateOf(0) }

    val hasPlain = !result.plainLyrics.isNullOrBlank()
    val hasSynced = !result.syncedLyrics.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 550.dp)
            .padding(16.dp)
    ) {
        Text(text = "Previewing: ${result.trackName}", style = MaterialTheme.typography.titleMedium)
        Text(text = result.artistName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = previewTab,
            containerColor = Color.Transparent
        ) {
            Tab(
                selected = previewTab == 0,
                onClick = { previewTab = 0 },
                text = { Text("Plain Preview") },
                enabled = hasPlain
            )
            Tab(
                selected = previewTab == 1,
                onClick = { previewTab = 1 },
                text = { Text("Synced (LRC) Preview") },
                enabled = hasSynced
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp)
        ) {
            val textToDisplay = when (previewTab) {
                0 -> if (hasPlain) result.plainLyrics else "No plain lyrics available."
                1 -> if (hasSynced) result.syncedLyrics else "No timestamped LRC lyrics available."
                else -> ""
            }

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = textToDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = if (previewTab == 1) FontFamily.Monospace else FontFamily.Default
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to List")
            }

            Button(
                onClick = {
                    onApplyClick(result.plainLyrics, result.syncedLyrics)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Lyrics")
            }
        }
    }
}