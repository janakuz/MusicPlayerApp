package com.example.musicapp.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.musicapp.ui.viewmodels.AllArtistsViewModel
import com.example.musicapp.data.local.model.GridItem
import com.example.musicapp.ui.components.DeleteConfirmationDialog
import com.example.musicapp.ui.components.Grid
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.RefetchState


@Composable
fun AllArtistsScreen(
    onClick: ((GridItem) -> Unit)? = null,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    sortRequest: SortOption?,
){
    val artistViewModel: AllArtistsViewModel = hiltViewModel()

    data class DeleteEvent(val id: Int, val name: String)

    var pendingDeletion by remember { mutableStateOf<DeleteEvent?>(null) }

    val context = LocalContext.current
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingDeletion != null) {
            artistViewModel.finalizeDeletion(pendingDeletion!!.id)
        } else {
            artistViewModel.clearPendingDeletion()
        }
    }

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            artistViewModel.setSort(it)
        }
    }

    val pendingUris by artistViewModel.pendingDeleteUris.collectAsState()

    LaunchedEffect(pendingUris) {
        if (pendingUris.isNotEmpty()) {
            val pendingIntent = artistViewModel.getDeleteIntent(context, pendingUris)
            deleteLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        }
    }

    val uiState by artistViewModel.artistListUiState.collectAsState()
    val refetchUiState by artistViewModel.refetchState.collectAsState()

    val artists = uiState.artists
    val items = artists.map { artist ->
        GridItem.ArtistItem(
            id = artist.id,
            displayName = artist.name,
            imageRes = artist.image.toString(),
            description = artist.bio.toString()
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Grid(
            listItems = items,
            shape = CircleShape,
            isAlbum = false,
            textStyle = MaterialTheme.typography.bodyMedium,
            onPlayNext = onPlayNext,
            onAddToQueue = onAddToQueue,
            onClick = onClick,
            onEdit = onEdit,
            onDelete = { id, name ->
                pendingDeletion = DeleteEvent(id, name)
            },
            onRefetch = { id -> artistViewModel.refetchMetadata(id) },
            onAddToPlaylist = onAddToPlaylist
        )

        pendingDeletion?.let { item ->
            DeleteConfirmationDialog(
                text = item.name,
                onConfirm = {
                    artistViewModel.prepareDeletion(item.id)
                    pendingDeletion = null
                },
                onDismiss = { pendingDeletion = null },
            )
        }

        when (refetchUiState) {
            is RefetchState.Saving -> {
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

            is RefetchState.DisambiguationNeeded -> {
                ArtistDisambiguationDialog(
                    matches = (refetchUiState as RefetchState.DisambiguationNeeded).matches,
                    onArtistSelected = { selectedArtist ->
                        artistViewModel.onArtistSelected(selectedArtist)
                    },
                    onDismiss = {
                        artistViewModel.reset()
                    }
                )
            }

            is RefetchState.Error -> {
                Text(text = (refetchUiState as RefetchState.Error).message)
            }

            else -> {}
        }
    }


}



@Preview(showBackground = true)
@Composable
fun ArtistsPreview() {
    MusicAppTheme {
//        ArtistsGrid(DataSource.artists)
    }

}