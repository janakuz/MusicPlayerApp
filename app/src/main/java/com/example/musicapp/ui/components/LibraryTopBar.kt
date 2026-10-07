package com.example.musicapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.LibraryScreen
import com.example.musicapp.ui.viewmodels.SortViewModel
import com.example.musicapp.util.toTitleCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopBar(
    currentScreen: LibraryScreen,
    currentRoute: String,
    onFilterClick: () -> Unit,
    onSearchClick: () -> Unit,
//    onSortClick: (SortOption) -> Unit,
    onMenuClick: () -> Unit,
    onImport: (() -> Unit)? = null,
    title: String? = "",
    showBack: Boolean,
    onBack: (() -> Unit)? = null,
    onShowSimilar: (() -> Unit)? = null,
    onOpenSequencer: (() -> Unit)? = null,
    onDeduplicate: (() -> Unit)? = null,
    ) {
    var expanded by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = if (onBack == null && title != null) title else "",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (!showBack) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            } else if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterAlt, contentDescription = "Filter")
            }

            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }

            val topBarSortViewModel: SortViewModel = hiltViewModel()
            val currentSortOption by topBarSortViewModel.getSortOptionForRoute(currentRoute).collectAsState()

            val actions = MenuActions(
                onDeduplicate = if (currentScreen == LibraryScreen.PLAYLIST_DETAIL) onDeduplicate else null,
                onImportM3u = if (currentScreen == LibraryScreen.PLAYLISTS) onImport else null,
                onAddSimilar = if (currentScreen == LibraryScreen.ARTIST_DETAIL) onShowSimilar else null,
                onOpenSequencer = if (currentScreen == LibraryScreen.PLAYLIST_DETAIL) onOpenSequencer else null,
                onSort =  if (isSortable(currentScreen)) { { showSort = true } } else null
            )
            if (expanded){
                TopActionsMenu(
                    title = "${currentScreen.name.split("_").first().toTitleCase()} Options",
                    onDismiss = {
                        expanded = false
                        showSort = false
                    },
                    actions = actions,
                    onSortClick = { sortOption -> topBarSortViewModel.updateSortOption(currentRoute, sortOption) },
                    showSortMenu = showSort,
                    currentScreen = currentScreen,
                    currentSortOption = currentSortOption
                )
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    count: Int,
    onClear: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveFromQueue: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    isQueueScreen: Boolean = false,
    isPlaylistScreen: Boolean = false,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    moveEnabled: Boolean = false,
    onAddToPlaylist: () -> Unit,
    onEdit: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = if (count == 1) "1 track selected" else "$count tracks selected",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                    )
                }



                val actions = MenuActions(
                    onPlayNext = {
                        onPlayNext()
                        onClear()
                        expanded = false
                    },
                    onAddToQueue = {
                        onAddToQueue()
                        onClear()
                        expanded = false
                    },
                    onRemoveFromQueue = if (onRemoveFromQueue != null && isQueueScreen) {
                        {
                            onRemoveFromQueue()
                            onClear()
                            expanded = false
                        }
                    } else null,
                    onDelete = {
                        onDelete()
                        onClear()
                        expanded = false
                    },
                    onEdit = {
                        onEdit()
                        expanded = false
                    },
                    onAddToPlaylist = {
                        onAddToPlaylist()
                        onClear()
                        expanded = false
                    },
                    onRemoveFromPlaylist = if (onRemoveFromPlaylist != null && isPlaylistScreen) {
                        {
                            onRemoveFromPlaylist()
                            onClear()
                            expanded = false
                        }
                    } else null,
                    onMoveToAlbum = if (moveEnabled) {
                        {
                            onMove()
                            onClear()
                            expanded = false
                        }
                    } else null
                )

                if (expanded) {
                    ActionMenu(
                        title = "$count tracks",
                        actions = actions,
                        onDismiss = { expanded = false }
                    )
                }

            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Exit Search")
            }
        },
        actions = {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTopBar(
    onEditFilters: () -> Unit,
    onBack: () -> Unit,
){
    TopAppBar(
        title = { Text("") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = null)
            }
        },
        actions = {
            TextButton(onClick = onEditFilters) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.FilterAlt, contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Edit Filters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )

}

