package com.example.musicapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private lateinit var mediaController: MediaController
    private val playerViewModel: PlayerViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_MEDIA_AUDIO] == true &&
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true) {
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DevLibraryBootstrap.ensureSampleTracksAvailable(this)

        val permissionsToRequest = arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.POST_NOTIFICATIONS
        )
        requestPermissionLauncher.launch(permissionsToRequest)

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

