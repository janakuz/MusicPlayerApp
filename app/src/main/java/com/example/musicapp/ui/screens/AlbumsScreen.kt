package com.example.musicapp.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.GridItem
import com.example.musicapp.data.remote.dto.Release
import com.example.musicapp.ui.components.DeleteConfirmationDialog
import com.example.musicapp.ui.components.Grid
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.AllAlbumsViewModel
import com.example.musicapp.ui.viewmodels.RefetchAlbumState
import com.example.musicapp.ui.viewmodels.RefetchState


@Composable
fun AlbumsGrid(
    albums: List<AlbumInfo>,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
    showReleaseDate: Boolean = false,
    onClick: ((GridItem) -> Unit)? = null,
    onEdit: (GridItem) -> Unit,
    header: (@Composable () -> Unit)? = null,
    onDelete: (Int, String) -> Unit,
    onRefetch: (Int) -> Unit,
) {


    val items = albums.map { album ->
        GridItem.AlbumItem(
            id = album.albumId,
            displayName = album.title,
            imageRes = album.image.toString(),
            releaseYear = album.releaseDate?.take(4).orEmpty(),
            numTracks = 0,
            duration = album.duration.toInt(),
        )
    }


    Grid(
        listItems = items,
        isAlbum = true,
        showReleaseDate = showReleaseDate,
        onPlayNext = onPlayNext,
        onAddToQueue = onAddToQueue,
        onAddToPlaylist = onAddToPlaylist,
        shape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        onClick = onClick,
        header = header,
        onEdit = onEdit,
        onDelete = onDelete,
        onRefetch = onRefetch
    )
}


@Composable
fun AllAlbumsScreen(
    onClick: (GridItem) -> Unit,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    sortRequest: SortOption?,
) {
    val albumViewModel: AllAlbumsViewModel = hiltViewModel()

    data class DeleteEvent(val id: Int, val name: String)

    var pendingDeletion by remember { mutableStateOf<DeleteEvent?>(null) }


    val context = LocalContext.current
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingDeletion != null) {
            albumViewModel.finalizeDeletion(pendingDeletion!!.id)
        } else {
            albumViewModel.clearPendingDeletion()
        }
    }

    val pendingUris by albumViewModel.pendingDeleteUris.collectAsState()

    LaunchedEffect(pendingUris) {
        if (pendingUris.isNotEmpty()) {
            val pendingIntent = albumViewModel.getDeleteIntent(context, pendingUris)
            deleteLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        }
    }


    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            albumViewModel.setSort(it)
        }
    }


    val albumsState by albumViewModel.albumListUiState.collectAsState()
    val albums = albumsState.albums

    val refetchUiState by albumViewModel.refetchState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        AlbumsGrid(
            albums,
            onPlayNext = onPlayNext,
            onAddToQueue = onAddToQueue,
            onAddToPlaylist = onAddToPlaylist,
            onClick = onClick,
            onEdit = onEdit,
            onDelete = { id, title -> pendingDeletion = DeleteEvent(id, title) },
            onRefetch = { id -> albumViewModel.refetchMetadata(id) }
        )


        when (refetchUiState) {
            is RefetchAlbumState.Saving -> {
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

            is RefetchAlbumState.DisambiguationNeeded -> {
                AlbumDisambiguationDialog(
                    matches = (refetchUiState as RefetchAlbumState.DisambiguationNeeded).matches,
                    onAlbumSelected = { selectedAlbum ->
                        albumViewModel.onAlbumSelected(selectedAlbum)
                    },
                    onDismiss = {
                        albumViewModel.reset()
                    }
                )
            }

            is RefetchAlbumState.Error -> {
                Text(text = (refetchUiState as RefetchState.Error).message)
            }

            else -> {}
        }


    }

    pendingDeletion?.let { item ->
        DeleteConfirmationDialog(
            text = item.name,
            onConfirm = {
                albumViewModel.prepareDeletion(item.id)
                pendingDeletion = null
            },
            onDismiss = { pendingDeletion = null },
        )
    }


}


@Composable
fun AlbumDisambiguationDialog(
    matches: List<Release>,
    onAlbumSelected: (Release) -> Unit,
    onDismiss: () -> Unit,
    onNotMatchedSelected: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Multiple Albums Found") },
        text = {
            LazyColumn {
                items(matches) { album ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumSelected(album) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (album.artistCredit.isNotEmpty()) {
                                Text(
                                    text = album.artistCredit[0].artist.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!album.country.isNullOrEmpty()) {
                                Text(
                                    text = "${album.country} ${album.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
                if (onNotMatchedSelected != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNotMatchedSelected() }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) { Text("Add as unmatched") }
                    }
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


@Preview(showBackground = true)
@Composable
fun AlbumsPreview() {
    MusicAppTheme {
    }
}