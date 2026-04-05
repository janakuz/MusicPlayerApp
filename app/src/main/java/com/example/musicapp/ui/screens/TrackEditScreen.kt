package com.example.musicapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.EditTopBar
import com.example.musicapp.ui.viewmodels.AlbumArtistEditUiState
import com.example.musicapp.ui.viewmodels.TrackEditViewModel


@Composable
fun TrackEditScreen(
    onNavigateBack: () -> Unit,
){
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
                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
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
                        modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth(),
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
