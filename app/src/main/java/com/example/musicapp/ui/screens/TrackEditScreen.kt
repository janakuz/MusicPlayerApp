package com.example.musicapp.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import com.example.musicapp.ui.components.EditTopBar
import com.example.musicapp.ui.viewmodels.AlbumArtistEditUiState
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.ui.viewmodels.TrackEditViewModel
import com.example.musicapp.ui.viewmodels.TrackMultiEditViewModel
import com.example.musicapp.ui.viewmodels.VoiceState


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

    var showDiscardDialog by remember { mutableStateOf(false) }


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
                    text = "Voice Type",
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