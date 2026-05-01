package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musicapp.data.entity.Playlist
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.musicapp.ui.components.CreatePlaylistDialog
import com.example.musicapp.ui.viewmodels.CreatePlaylistState


@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    createInfo: CreatePlaylistState,
    onNameChange: (String) -> Unit,
    onClick: (Int) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (Int) -> Unit,
) {
    if (createInfo.isShowing) {
        CreatePlaylistDialog(
            createInfo = createInfo,
            onNameChange = onNameChange,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }

    LazyColumn {
        item {
            TextButton(
                onClick = {
                    onCreateNewPlaylist()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create New Playlist")
            }
        }
        items(playlists) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onClick(playlist.id) },
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(onClick = onClick) {
        Row(Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = true }
            )
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(48.dp))
            }
            Text(
                text = playlist.name,
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete(playlist.id)
                        expanded = false
                    }
                )
            }

        }
    }
}