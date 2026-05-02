package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.media3.session.CommandButton
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.R
import com.example.musicapp.ui.components.CreatePlaylistDialog
import com.example.musicapp.ui.components.formatDuration
import com.example.musicapp.ui.viewmodels.CreatePlaylistState
import com.example.musicapp.ui.viewmodels.PlaylistUiModel


@Composable
fun PlaylistsScreen(
//    playlists: List<Playlist>,
    playlistStates: List<PlaylistUiModel>,
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
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNewPlaylist) {
                Icon(Icons.Default.Add, contentDescription = "Create Playlist")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 80.dp
            )
        ) {

            items(playlistStates) { playlistModel ->
                PlaylistRow(
                    playlist = playlistModel.playlist,
                    onClick = { onClick(playlistModel.playlist.id) },
                    onDelete = onDelete,
                    trackCount = playlistModel.trackCount,
                    duration = playlistModel.totalDuration,
                    images = playlistModel.top4Images
                )
            }
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: Playlist,
    trackCount: Int,
    duration: Long,
    images: List<String> = emptyList<String>(),
    onClick: () -> Unit,
    onDelete: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
        Row(Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = true }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (images.isNotEmpty()) PlaylistCollage(images, modifier = Modifier)
                else Icon(Icons.Default.MusicNote, contentDescription = "")
            }

            Spacer(Modifier.width(16.dp))

            Column (modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
//                    text = "2 tracks • 3:50",
                    text = "$trackCount tracks • ${formatDuration(duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

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

@Composable
fun PlaylistCollage(images: List<String>, modifier: Modifier = Modifier) {
    if (images.size == 4) {
        Column(modifier) {
            Row(Modifier.weight(1f)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(images[0])
                        .build(),
                    placeholder = painterResource(R.drawable.baseline_album_24),
                    error = painterResource(R.drawable.baseline_album_24),
                    fallback = painterResource(R.drawable.baseline_album_24),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f),
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(images[1])
                        .build(),
                    placeholder = painterResource(R.drawable.baseline_album_24),
                    error = painterResource(R.drawable.baseline_album_24),
                    fallback = painterResource(R.drawable.baseline_album_24),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f),
                )
            }
            Row(Modifier.weight(1f)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(images[2])
                        .build(),
                    placeholder = painterResource(R.drawable.baseline_album_24),
                    error = painterResource(R.drawable.baseline_album_24),
                    fallback = painterResource(R.drawable.baseline_album_24),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f),
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(images[3])
                        .build(),
                    placeholder = painterResource(R.drawable.baseline_album_24),
                    error = painterResource(R.drawable.baseline_album_24),
                    fallback = painterResource(R.drawable.baseline_album_24),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f),
                )
            }
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(images[0])
                .build(),
            placeholder = painterResource(R.drawable.baseline_album_24),
            error = painterResource(R.drawable.baseline_album_24),
            fallback = painterResource(R.drawable.baseline_album_24),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}