package com.example.musicapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicapp.R
import com.example.musicapp.ui.components.EditTopBar
import com.example.musicapp.ui.viewmodels.PlaylistEditViewModel

@Composable
fun PlaylistEditScreen(
    onNavigateBack: () -> Unit,
) {
    val playlistEditViewModel: PlaylistEditViewModel = hiltViewModel()

    val playlistEditUiState by playlistEditViewModel.uiState.collectAsState()
    val canSave by playlistEditViewModel.canSave.collectAsState()
    val isEditing = playlistEditViewModel.isEditMode

    var showDiscardDialog by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            playlistEditViewModel.onImageChange(uri)
        }
    }

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
                title = if (isEditing) "Edit Playlist" else "New Playlist",
                onBackClick = handleBack
            )
        },
        floatingActionButton = {
            if (canSave) {
                ExtendedFloatingActionButton(
                    onClick = {
                        playlistEditViewModel.onSave(context)
                        onNavigateBack()
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
                     Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                                    .clickable {
                                        pickMedia.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = playlistEditUiState.draftImageUrl
                                        ?: R.drawable.baseline_album_24,
                                    contentDescription = "Edit Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White)
                                }

                                if (playlistEditUiState.draftImageUrl != null) {
                                    TextButton(
                                        onClick = { playlistEditViewModel.onRemoveImage() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Remove Custom Cover")
                                    }
                                }
                            }
                        }
                }

                item {
                    OutlinedTextField(
                        value = playlistEditUiState.name,
                        onValueChange = { playlistEditViewModel.onNameChange(it) },
                        label = { Text("Playlist Name") },
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
                        value = playlistEditUiState.draftDescription,
                        onValueChange = { playlistEditViewModel.onDescChange(it) },
                        label = { Text("Description") },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }


            }

        }

    }
}
