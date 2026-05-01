package com.example.musicapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.ui.viewmodels.CreatePlaylistState

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Int) -> Unit,
    onCreateNewPlaylist: () -> Unit
    ) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add to Playlist") },
        text = {
            Column {
                TextButton(
                    onClick = {
                        onCreateNewPlaylist()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create New Playlist")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            leadingContent = {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                onPlaylistSelected(playlist.id)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreatePlaylistDialog(
    createInfo: CreatePlaylistState,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isNameValid = createInfo.name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Playlist") },
        text = {
            Column {
                OutlinedTextField(
                    value = createInfo.name,
                    onValueChange = { onNameChange(it) },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., Late Night Punk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isNameValid,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isNameValid) {
                                onConfirm()
                                onDismiss()
                            }
                        }
                    )
                )
                if (!isNameValid) {
                    Text(
                        text = "Name cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                enabled = isNameValid
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
