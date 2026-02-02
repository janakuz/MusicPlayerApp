package com.example.musicapp.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.viewmodels.LibraryScanViewModel
import androidx.compose.runtime.getValue


@Composable
fun ScanLibraryScreen(viewModel: LibraryScanViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Column {
        if (uiState.isScanning) {
            Text("Scanning... ${uiState.scanProgress}%")
        }
        else if (uiState.isEnriching){
            Text("Enriching... ${uiState.enrichmentProgress}%")
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

            Button(onClick = {
                launcher.launch(permission)
            }) {
                Text("Scan Library")
            }
        }
    }
}
