package com.example.musicapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.R
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.local.model.VisualTrack
import com.example.musicapp.data.repository.PlayerColors
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.AlbumDetailViewModel
import com.example.musicapp.ui.viewmodels.NewAlbum
import com.example.musicapp.ui.viewmodels.RefetchAlbumTracksState
import com.example.musicapp.util.formatDuration


@Composable
fun ImageHeader(
    image: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) {
            val defaultImage = painterResource(R.drawable.baseline_album_24)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .size(400)
                    .crossfade(false)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .placeholderMemoryCacheKey(image)
                    .build(),
                placeholder = defaultImage,
                error = defaultImage,
                fallback = defaultImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.background
//                                        gradientColors.mainColor.copy(alpha = 0.6f),
//                                        gradientColors.mainColor.copy(alpha = 0.5f)
                                )
                            )
                    )
            )
        }
    }
}

@Composable
fun AlbumDetailHeader(
    image: String,
    title: String,
    gradientColors: PlayerColors? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        ImageHeader(image)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
fun AlbumInfoRow(
    duration: Long,
    numTracks: Int,
    releaseDate: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (releaseDate != null) {
            InfoChip(text = releaseDate)

            DotSeparator()
        }

        InfoChip(text = "$numTracks Tracks")

        DotSeparator()

        InfoChip(text = duration.formatDuration())
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = CircleShape,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DotSeparator() {
    Text(
        text = "•",
        modifier = Modifier.padding(horizontal = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun FullHeader(album: Album, gradientColors: PlayerColors) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlbumDetailHeader(
            image = album.image.toString(),
            title = album.title,
            gradientColors = gradientColors
        )
        Spacer(modifier = Modifier.height(8.dp))
        AlbumInfoRow(
            duration = album.duration,
            numTracks = album.numTracks,
            releaseDate = album.releaseDate?.take(4)
        )
        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
fun Footer(label: String) {
    Row(
        modifier = Modifier
            .padding(top = 16.dp, start = 8.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = "Released on $label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun AlbumView(
    onTrackClick: (TrackInfo, List<TrackInfo>) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onEdit: (TrackInfo) -> Unit,
    onGoToArtist: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val albumDetailViewModel: AlbumDetailViewModel = hiltViewModel()

    val albumUiState by albumDetailViewModel.albumDetailUiState.collectAsState()
    val album = albumUiState.album
    val tracks = albumUiState.tracks

    val moveToAlbum by albumDetailViewModel.currentNewAlbum.collectAsState()
    val pendingMoveIds by albumDetailViewModel.pendingMoveIds.collectAsState()
    val moveState by albumDetailViewModel.moveState.collectAsState()

    val visualTracks = tracks.map { track -> VisualTrack(key = track.trackId, data = track) }

    if (album != null) {

        albumDetailViewModel.getAlbumColors(album.image.toString())
        val gradientColors = albumDetailViewModel.albumColors

        Box(modifier = Modifier.fillMaxSize()) {

            TrackList(
                visualTracks,
                onClick = { track -> onTrackClick(track.data, tracks) },
                onPlayNext = onPlayNext,
                onAddToQueue = onAddToQueue,
                showTrackNum = true,
                header = {
                    FullHeader(
                        album,
                        gradientColors
                    )
                },
                footer = {
                    if (album.label != null && album.label != "") Footer(album.label) else null
                },
                onEdit = onEdit,
                onMove = { ids -> albumDetailViewModel.prepareMove(ids) },
                onAddToPlaylist = onAddToPlaylist,
                onGoToArtist = onGoToArtist
            )

            when (moveState) {
                is RefetchAlbumTracksState.Saving -> {
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

                is RefetchAlbumTracksState.DisambiguationNeeded -> {
                    AlbumDisambiguationDialog(
                        matches = (moveState as RefetchAlbumTracksState.DisambiguationNeeded).matches,
                        onAlbumSelected = { selectedAlbum ->
                            albumDetailViewModel.onAlbumSelected(selectedAlbum)
                        },
                        onDismiss = {
                            albumDetailViewModel.reset()
                        },
                        onNotMatchedSelected = {
                            albumDetailViewModel.splitToUnenriched(
                                pendingMoveIds,
                                moveToAlbum.artist ?: "",
                                moveToAlbum.title ?: ""
                            )
                        }
                    )
                }

                is RefetchAlbumTracksState.InputExpected -> {
                    SplitAlbumDialog(
                        onSave = {
                            albumDetailViewModel.splitToAlbum(
                                pendingMoveIds,
                                moveToAlbum.artist ?: "",
                                moveToAlbum.title ?: ""
                            )
                        },
                        onDismiss = { albumDetailViewModel.reset() },
                        onAlbumChange = { title -> albumDetailViewModel.onTitleChange(title) },
                        onArtistChange = { name -> albumDetailViewModel.onArtistChange(name) },
                        uiState = moveToAlbum
                    )
                }

                is RefetchAlbumTracksState.Error -> {
                    Text(text = (moveState as RefetchAlbumTracksState.Error).message)
                }

                else -> {}
            }


        }
    }

}


@Composable
fun SplitAlbumDialog(
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onArtistChange: (String) -> Unit,
    onAlbumChange: (String) -> Unit,
    uiState: NewAlbum
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Input New Artist and Title") },
        confirmButton = {
            TextButton(onClick = { onSave() }) {
                Text("Find Album")
            }
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = uiState.title ?: "",
                        onValueChange = { onAlbumChange(it) },
                        label = { Text("Album Title") },
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
                        value = uiState.artist ?: "",
                        onValueChange = { onArtistChange(it) },
                        label = { Text("Artist") },
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
    )

}

@Preview(showBackground = true)
@Composable
fun AlbumPreview() {
    MusicAppTheme {
    }

}