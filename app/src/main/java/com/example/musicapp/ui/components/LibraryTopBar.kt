package com.example.musicapp.ui.components

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.musicapp.ui.LibraryScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopBar(
    currentScreen: LibraryScreen,
    onFilterClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: (SortOption) -> Unit,
    onMenuClick: () -> Unit,
    onImport: (() -> Unit)? = null,
    title: String? = "",
    showBack: Boolean,
    onBack: (() -> Unit)? = null,
    onShowSimilar: (() -> Unit)? = null,
    onOpenSequencer: (() -> Unit)? = null,
    ) {
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

            RightMenu(screen = currentScreen, onSortSelected = onSortClick, onImport = onImport, onShowSimilar = onShowSimilar, onOpenSequencer = onOpenSequencer)
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
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }
    )
}


