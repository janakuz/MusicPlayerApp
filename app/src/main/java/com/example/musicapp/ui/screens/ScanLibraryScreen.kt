package com.example.musicapp.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.viewmodels.LibraryScanViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicapp.ui.theme.MusicAppTheme


@Composable
fun ScanLibraryScreen(
    viewModel: LibraryScanViewModel = hiltViewModel(),
    isInitial: Boolean = false) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Rounded.LibraryMusic,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .alpha(0.8f),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isInitial) {
            Text("Welcome to MusicApp", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(8.dp))

        Text("Press the button below to scan your music", style = MaterialTheme.typography.bodyMedium)


        Spacer(Modifier.height(32.dp))

        if (uiState.isScanning) {
            LinearProgressIndicator(
                progress = { uiState.scanProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            )
            Text("Importing ${ (uiState.scanProgress).toInt() }%")

        }
        else if (uiState.isEnriching){
            Text("Tracks imported! Retrieving metadata...")

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { uiState.enrichmentProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            )

            Spacer(Modifier.height(8.dp))

            Text("${uiState.statusMessage}")
        }

        else {

            val context = LocalContext.current
            val permission =
                Manifest.permission.READ_MEDIA_AUDIO

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    if (granted) {
                        viewModel.startScan(context)
                    } else {
                        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Button(
                onClick = { launcher.launch(permission) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp)) {
                Text("Start Import")
            }
        }
    }

//    Column {
//        if (uiState.isScanning) {
//            Text("Scanning... ${uiState.scanProgress}%")
//        }
//        else if (uiState.isEnriching){
//            Text("Enriching... ${uiState.enrichmentProgress}%")
//            Text("${uiState.statusMessage}")
//        }
//        else {
//
//            val context = LocalContext.current
//            val permission =
//                Manifest.permission.READ_MEDIA_AUDIO
//
//            val launcher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.RequestPermission(),
//                onResult = { granted ->
//                    if (granted) {
//                        viewModel.startScan(context)
//                    } else {
//                        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            )
//
//            Button(onClick = {
//                launcher.launch(permission)
//            }) {
//                Text("Scan Library")
//            }
//        }
//    }
}
