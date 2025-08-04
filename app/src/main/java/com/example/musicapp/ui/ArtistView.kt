package com.example.musicapp.ui

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
import com.example.musicapp.AlbumViewModel
import com.example.musicapp.ArtistViewModel
import com.example.musicapp.ui.components.ImageWithTextColumn
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.theme.MusicAppTheme

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
    id: Int,
    artistViewModel: ArtistViewModel,
    albumViewModel: AlbumViewModel,
//    name: String,
//    bio: String,
//    image: String,
//    albums: List<GridItem.AlbumItem>,
    modifier: Modifier = Modifier,
    onAlbumClick: ((GridItem) -> Unit)? = null){

    LaunchedEffect(id) {
        artistViewModel.getArtistById(id)
        albumViewModel.getAlbumsByArtist(id)
    }

    val albumUiState by albumViewModel.albumArtistListUiState.collectAsState()
    val albums = albumUiState.albums

    val artistUiState by artistViewModel.currentArtistUiState.collectAsState()
    val artist = artistUiState.artist

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
//        ArtistView(name= stringResource(R.string.sw),
//            bio = stringResource(R.string.sw_bio),
//            image = "",
//            albums = DataSource.albums
//        )
    }

}