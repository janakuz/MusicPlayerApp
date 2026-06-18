package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.model.GridItem
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.ui.viewmodels.GenreDetailViewModel
import com.example.musicapp.util.toTitleCase
import java.nio.file.WatchEvent

@Composable
fun GenreDetailScreen(
    onArtistClick: (Int) -> Unit,
    onAlbumClick: (Int) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onAddToPlaylistArtist: (GridItem) -> Unit,
    onAddToPlaylistAlbum: (GridItem) -> Unit,
    onPlayNextArtist: (GridItem) -> Unit,
    onPlayNextAlbum: (GridItem) -> Unit,
    onAddToQueueArtist: (GridItem) -> Unit,
    onAddToQueueAlbum: (GridItem) -> Unit,
    onEditArtist: (GridItem) -> Unit,
    onEditAlbum: (GridItem) -> Unit,
    ){

    val genreDetailViewModel: GenreDetailViewModel = hiltViewModel()
    val results by genreDetailViewModel.genreItems.collectAsState()
    val name by genreDetailViewModel.genreName.collectAsState()



    SearchContent(
        results = results,
        onArtistClick = onArtistClick,
        onAlbumClick = onAlbumClick,
        onTrackClick = { list, track -> {} },
        onAddToPlaylist = onAddToPlaylist,
        onAddToPlaylistArtist = onAddToPlaylistArtist,
        onAddToPlaylistAlbum = onAddToPlaylistAlbum,
        padding = PaddingValues(0.dp),
        onPlayNextArtist = onPlayNextArtist,
        onPlayNextAlbum = onPlayNextAlbum,
        onAddToQueueArtist = onAddToQueueArtist,
        onAddToQueueAlbum = onAddToQueueAlbum,
        onEditArtist = onEditArtist,
        onEditAlbum = onEditAlbum,
        onPlayNextTrack = {},
        onAddToQueueTrack = {},
        onEditTrack = {},
        header = { GenreHeader(name) }
    )

}

@Composable
fun GenreHeader(name: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = name.toTitleCase(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp),
            modifier = Modifier.padding(start = 16.dp, bottom = 20.dp)
        )
    }
}