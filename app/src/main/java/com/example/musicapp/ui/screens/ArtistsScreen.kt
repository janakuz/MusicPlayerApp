package com.example.musicapp.ui.screens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.viewmodels.AllArtistsViewModel
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.Grid
import com.example.musicapp.ui.theme.MusicAppTheme



@Composable
fun AllArtistsScreen(
    onClick: ((GridItem) -> Unit)? = null
){
    val artistViewModel: AllArtistsViewModel = hiltViewModel()
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

}



@Preview(showBackground = true)
@Composable
fun ArtistsPreview() {
    MusicAppTheme {
//        ArtistsGrid(DataSource.artists)
    }

}