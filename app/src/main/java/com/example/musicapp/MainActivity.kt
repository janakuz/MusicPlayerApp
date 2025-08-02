package com.example.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.musicapp.ui.theme.MusicAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private lateinit var mediaController: MediaController
    val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
//        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        enableEdgeToEdge()

//        controllerFuture.addListener({
//            mediaController = controllerFuture.get()
//            setContent {
//                MusicAppTheme {
//                    MusicApp(mediaController)
//                }
//            }
//        }, ContextCompat.getMainExecutor(this))

        setContent {
            MusicAppTheme {
                    MusicApp(playerViewModel)
                }
            }
    }


}

