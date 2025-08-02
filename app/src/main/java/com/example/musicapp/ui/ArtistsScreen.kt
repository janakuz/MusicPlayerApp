package com.example.musicapp.ui

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

    Button(onClick = { artistViewModel.loadFromStorage(context) }) {
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