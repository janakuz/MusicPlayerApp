package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.local.model.GridItem
import com.example.musicapp.ui.viewmodels.CountryDetailViewModel
import com.example.musicapp.ui.viewmodels.GenreDetailViewModel

@Composable
fun CountryDetailScreen(
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

    val countryDetailViewModel: CountryDetailViewModel = hiltViewModel()
    val results by countryDetailViewModel.countryItems.collectAsState()
    val name = countryDetailViewModel.countryName



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