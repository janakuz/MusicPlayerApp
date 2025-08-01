package com.example.musicapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicapp.data.DataSource
import com.example.musicapp.model.Track
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicapp.PlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun NowPlayingView(
    name: String = "",
    artist: String = "",
    image: Painter = painterResource(R.drawable.cover),
    track1: Track = DataSource.tracks.get(0),
    playerViewModel: PlayerViewModel,
    tracks: List<Track> = DataSource.tracks,
    modifier: Modifier = Modifier,
    onQueueClick: Any
){
    val context = LocalContext.current
//    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    val controller by playerViewModel.controller.collectAsState()
    val track by playerViewModel.currentTrack.collectAsState()
    val tracks by playerViewModel.queue.collectAsState()
    var isPlaying by remember { mutableStateOf(false) }
    val position by playerViewModel.position.collectAsState()
    val duration by playerViewModel.duration.collectAsState()

    var sliderPosition by remember { mutableStateOf(position.toFloat()) }

    LaunchedEffect(controller) {
        if (controller != null) {
//            playerViewModel.playTracks(tracks,track)
            isPlaying = true
        }
    }

    // Safe polling loop
 //   LaunchedEffect(controller) {
 //       while (true) {
//            if (controller != null && controller!!.isPlaying) {
//                position = controller!!.currentPosition
//                duration = controller!!.duration
//            }
 //           controller?.let {
 //               position = it.currentPosition
 //               duration = it.duration
 //           }
 //           delay(500)
  //      }
  //  }

 //   LaunchedEffect(Unit) {
    //    val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
   //     val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
   //     controllerFuture.addListener(
      //      {
    //            val controller = controllerFuture.get()
    //            mediaController = controller

                // Load and play test file
    //            playerViewModel.playTrack("asset:///03_guest_list.mp3".toUri())
    //            isPlaying = true

    //            while (true) {
     //               position = controller?.currentPosition ?: 0
    //               duration = controller?.duration ?: 0
                 //   delay(500)
       //         }
        //    },
       //     ContextCompat.getMainExecutor(context)
       // )
  //  }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        AlbumDetailHeader(image= painterResource(track!!.art),
//            title = stringResource(track!!.title)
//        )
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(text = stringResource(track!!.artist), style = MaterialTheme.typography.bodyLarge)
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(text = stringResource(track!!.album), style = MaterialTheme.typography.bodyMedium)

        track?.let {
            AlbumDetailHeader(
                image = painterResource(it.art),
                title = stringResource(it.title)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(it.artist), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(it.album), style = MaterialTheme.typography.bodyMedium)
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

//            value = position.toFloat(),
  //          onValueChange = { newValue -> playerViewModel.controller.value?.seekTo(newValue.toLong()) },
  //          valueRange = 0f..duration.toFloat()
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
fun NowPlayingWithQueue(playerViewModel: PlayerViewModel, onTrackClick: (Track) -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetScaffoldState()

    val tracks by playerViewModel.queue.collectAsState()


    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContent = {
            PlayQueueScreen(tracks, onTrackClick, playerViewModel)
        },
        sheetPeekHeight = 0.dp,
        content = {
            NowPlayingView(
                playerViewModel = playerViewModel,
                onQueueClick = {
                    scope.launch {
                        sheetState.bottomSheetState.expand()
                    }
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NowPlayingPreview() {
    MusicAppTheme {
        NowPlayingView(
            name= stringResource(R.string.sw),
            artist = stringResource(R.string.sw),
            playerViewModel = viewModel(),
            onQueueClick = {

            }
        )
    }

}