package com.example.musicapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.local.model.GridItem
import com.example.musicapp.ui.viewmodels.AreaDetailViewModel
import com.example.musicapp.util.toTitleCase

@Composable
fun AreaDetailScreen(
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

    val areaDetailViewModel: AreaDetailViewModel = hiltViewModel()
    val results by areaDetailViewModel.areaItems.collectAsState()
    val name = areaDetailViewModel.areaName
    val flag = areaDetailViewModel.flag
    val subtitle = areaDetailViewModel.subtitle

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
        header = { GeographicSceneHeader(name, flag, subtitle) }
    )

}

@Composable
fun GeographicSceneHeader(
    areaName: String,
    flagEmoji: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = flagEmoji,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = areaName.toTitleCase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}