package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.repository.SearchResult
import com.example.musicapp.ui.components.EditTopBar
import com.example.musicapp.ui.components.SearchTopBar
import com.example.musicapp.ui.viewmodels.SearchViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.dto.VisualTrack
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.ImageWithTextColumn
import com.example.musicapp.ui.components.TrackRow
import com.example.musicapp.ui.components.formatDuration
import com.example.musicapp.ui.viewmodels.SearchScope

@Composable
fun SearchResultsScreen(
    onArtistClick: (Int) -> Unit,
    onAlbumClick: (Int) -> Unit,
    onTrackClick: (List<TrackInfo>, TrackInfo) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onAddToPlaylistArtist: (GridItem) -> Unit,
    onAddToPlaylistAlbum: (GridItem) -> Unit,
    onBack: () -> Unit,
){
    val searchViewModel: SearchViewModel = hiltViewModel()

    val query by searchViewModel.searchQuery.collectAsState()
    val results by searchViewModel.searchResults.collectAsState()


    Scaffold(
        topBar = {
            SearchTopBar(
                query = query,
                onQueryChange = { query -> searchViewModel.onQueryChange(query) },
                onClose = onBack,
                placeholder = if (searchViewModel.scope?.scopeType == "ARTIST") "Search in artist..."
                              else if (searchViewModel.scope?.scopeType == "ALBUM") "Search in album..."
                              else "Search..."
            )
        },
    ) { padding ->


        SearchContent(
            results = results,
            onArtistClick = onArtistClick,
            onAlbumClick = onAlbumClick,
            onTrackClick = onTrackClick,
            onAddToPlaylist = onAddToPlaylist,
            onAddToPlaylistArtist = onAddToPlaylistArtist,
            onAddToPlaylistAlbum = onAddToPlaylistAlbum,
            padding = padding,)
    }
}

@Composable
fun SearchContent(
    results: SearchResult,
    onArtistClick: (Int) -> Unit,
    onAlbumClick: (Int) -> Unit,
    onTrackClick: (List<TrackInfo>, TrackInfo) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onAddToPlaylistArtist: (GridItem) -> Unit,
    onAddToPlaylistAlbum: (GridItem) -> Unit,
    padding: PaddingValues
){
    LazyColumn(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        if (results.artists.isNotEmpty()) {
            item { SearchSectionHeader("Artists") }
            val artistRows = results.artists.chunked(3)

            items(artistRows) { row ->
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (artist in row) {
                        Box(modifier = Modifier.weight(1f)) {
                            ImageWithTextColumn(
                                item = GridItem.ArtistItem(
                                    id = artist.id,
                                    displayName = artist.name,
                                    imageRes = artist.image ?: "",
                                    description = artist.bio ?: ""
                                ),
                                image = artist.image ?: "",
                                text = artist.name,
                                isAlbum = false,
                                onPlayNext = {},
                                imageShape = CircleShape,
                                imageModifier = Modifier.size(80.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                onAddToQueue = {},
                                onEdit = {},
                                onDelete = { a, b -> {} },
                                onRefetch = {},
                                onClick = { gridItem -> onArtistClick( gridItem.id) },
                                onAddToPlaylist = onAddToPlaylistArtist
                            )
                        }

                    }
                    val emptySlots = 3 - (row.size)
                    if (emptySlots < 3) {
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                }
            }
        }

        if (results.albums.isNotEmpty()) {
            item { SearchSectionHeader("Albums") }
            val albumRows = results.albums.chunked(3)

            items(albumRows) { row ->
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (album in row) {
                        Box(modifier = Modifier.weight(1f)) {
                            ImageWithTextColumn(
                                item = GridItem.AlbumItem(
                                    id = album.albumId,
                                    displayName = album.title,
                                    imageRes = album.image ?: "",
                                    duration = album.duration.toInt(),
                                    numTracks = album.numTracks,
                                    releaseYear = album.releaseDate ?: ""
                                ),
                                image = album.image ?: "",
                                text = album.title,
                                isAlbum = true,
                                albumArtist = album.releaseDate ?: "",
                                imageModifier = Modifier.size(80.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                onPlayNext = {},
                                onAddToQueue = {},
                                onEdit = {},
                                onDelete = { a, b -> {} },
                                onRefetch = {},
                                onClick = {gridItem -> onAlbumClick(gridItem.id)},
                                onAddToPlaylist = onAddToPlaylistAlbum
                            )
                        }
                    }
                    val emptySlots = 3 - (row.size)
                    if (emptySlots < 3) {
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                }
            }
        }

        if (results.tracks.isNotEmpty()) {
            item { SearchSectionHeader("Tracks") }

            itemsIndexed(results.tracks, key = { index, track -> track.trackId }) { id, track ->
                TrackRow(
                    artwork = track.albumArt.toString(),
                    title = track.title,
                    artist = track.artistName,
                    isPlaying = false,
                    onClick = {track -> onTrackClick(results.tracks, track.data)},
                    onPlayNext = {},
                    onAddToQueue = {},
                    showArtwork = true,
                    showTrackNum = false,
                    showReorderIconStart = false,
                    showReorderIconEnd = false,
                    trackNum = track.trackNum ?: 0,
                    duration = formatDuration(track.duration),
                    track = VisualTrack(key = track.trackId, data = track),
                    useQueueId = false,
                    trackIndex = id,
                    onRemoveFromQueue = {},
                    onEdit = {},
                    onDelete = {ids ->{}},
                    onMove = {},
                    onAddToPlaylist = onAddToPlaylist,
                )
            }
        }
    }
}

@Composable
fun SearchSectionHeader(title: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}