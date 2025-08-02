package com.example.musicapp.ui

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicapp.ArtistViewModel
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.Grid
import com.example.musicapp.ui.theme.MusicAppTheme



@Composable
fun ArtistsGrid(
    artistViewModel: ArtistViewModel,
//    artists: List<Artist>,
    onClick: ((GridItem) -> Unit)? = null
){
    val uiState by artistViewModel.artistListUiState.collectAsState()
    val artists = uiState.artists
    val items = artists.map { artist ->
        GridItem.ArtistItem(
            id = artist.id,
            displayName = artist.name,
            imageRes = artist.image.toString(),
            description = artist.bio.toString()
        )
    }
    Grid(listItems = items,
        shape = CircleShape,
        isAlbum = false,
        textStyle = MaterialTheme.typography.bodyMedium,
        onClick = onClick)

    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_AUDIO
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
//                artistViewModel.triggerScan(context, "/sdcard/Music/01 - Bloodstains (Orignal Version).mp3");
//                artistViewModel.triggerScan(context, "/sdcard/Music/01 Kelly Burkett.mp3");
                artistViewModel.loadFromStorage(context)
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



@Preview(showBackground = true)
@Composable
fun ArtistsPreview() {
    MusicAppTheme {
//        ArtistsGrid(DataSource.artists)
    }

}