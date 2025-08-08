package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.Slider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.data.dto.TrackInfo

@Composable
fun NowPlayingView(
    playerViewModel: PlayerViewModel,
){
    val track by playerViewModel.currentTrack.collectAsState()
    var isPlaying by playerViewModel.isPlaying
    val position by playerViewModel.position.collectAsState()
    val duration by playerViewModel.duration.collectAsState()

    var sliderPosition by remember { mutableStateOf(position.toFloat()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        track?.let {
            AlbumDetailHeader(
                image = track!!.albumArt.toString(),
                title = track!!.title
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it.artistName, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it.albumTitle, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(32.dp))

        Slider(

            value = position.toFloat(),
            onValueChange = { newValue ->
                sliderPosition  = newValue
            },
            onValueChangeFinished = {
                playerViewModel.seekTo(sliderPosition.toLong())
            },            valueRange = 0f..(duration.takeIf { it > 0 } ?: 1).toFloat()

        )

        Spacer(Modifier.height(32.dp))

        Row {
            IconButton(onClick = {
                if (playerViewModel.controller.value?.hasPreviousMediaItem() == true) {
                    playerViewModel.skipToPrevious()
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
            }


            Button(onClick = {
                playerViewModel.controller.value?.pause()
                isPlaying = false
            }, enabled = isPlaying) {
                Text("Pause")
            }

            Spacer(Modifier.width(16.dp))

            Button(onClick = {
                playerViewModel.controller.value?.play()
                isPlaying = true
            }, enabled = !isPlaying) {
                Text("Play")
            }

            IconButton(onClick = {
                if (playerViewModel.controller.value?.hasNextMediaItem() == true) {
                    playerViewModel.skipToNext()
                }
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingWithQueue(
    playerViewModel: PlayerViewModel,
    track: TrackInfo?,
    tracks1: List<TrackInfo>,
    onTrackClick: (TrackInfo) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetScaffoldState()

    val tracks by playerViewModel.queue.collectAsState()


    BottomSheetScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        scaffoldState = sheetState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                PlayQueueScreen(tracks1, onTrackClick, playerViewModel)
            }
        },
        sheetPeekHeight = 0.dp,
        content = {
            NowPlayingView(
                playerViewModel = playerViewModel,
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NowPlayingPreview() {
    MusicAppTheme {
        NowPlayingView(
            playerViewModel = viewModel(),
        )
    }

}