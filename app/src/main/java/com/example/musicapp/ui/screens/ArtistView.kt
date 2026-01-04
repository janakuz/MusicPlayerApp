package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.ImageWithTextColumn
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.ArtistDetailViewModel

@Composable
fun AlbumDetailHeader(
    image: String,
    title: String
) {
    ImageWithTextColumn(
        image = image,
        text = title,
        isAlbum = false,
        imageModifier = Modifier.size(400.dp),
        textStyle = MaterialTheme.typography.headlineMedium,
        imageShape = RectangleShape
    )
}

@Composable
fun ArtistView(
    modifier: Modifier = Modifier,
    onAlbumClick: ((GridItem) -> Unit)? = null,
    sortRequest: SortOption?
){

    val artistDetailViewModel: ArtistDetailViewModel = hiltViewModel()

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            artistDetailViewModel.setSort(it)
        }
    }


    val artistUiState by artistDetailViewModel.currentArtistUiState.collectAsState()
    val artist = artistUiState.artist

    val albumsState by artistDetailViewModel.albumListUiState.collectAsState()
    val albums = albumsState.albums

    if (artist != null) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlbumDetailHeader(
                image = artist.image.toString(),
                title = artist.name
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = artist.bio.toString(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            AlbumsGrid(albums, showReleaseDate = true, onClick = onAlbumClick)


        }
    }

}

@Preview(showBackground = true)
@Composable
fun ArtistPreview() {
    MusicAppTheme {
        val albumArtistSort = null
        ArtistView(sortRequest = albumArtistSort)
    }

}