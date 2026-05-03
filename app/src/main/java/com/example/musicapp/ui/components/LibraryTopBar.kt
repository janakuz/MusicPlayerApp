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
import com.example.musicapp.LibraryScreen

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
                IconButton(onClick = onMenuClick ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
            else if (onBack != null){
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

            SortMenu(screen = currentScreen, onSortSelected = onSortClick, onImport = onImport)
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
    ){
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


                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Next") },
                        onClick = {
                            onPlayNext()
                            onClear()
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Queue") },
                        onClick = {
                            onAddToQueue()
                            onClear()
                            expanded = false
                        }
                    )
                    if (onRemoveFromQueue != null && isQueueScreen) {
                        DropdownMenuItem(
                            text = { (Text("Remove from Queue")) },
                            onClick = {
                                onRemoveFromQueue()
                                onClear()
                                expanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            onClear()
                            expanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        onClick = {
                            onAddToPlaylist()
                            onClear()
                            expanded = false
                        }
                    )

                    if (onRemoveFromPlaylist != null && isPlaylistScreen) {
                        DropdownMenuItem(
                            text = { (Text("Remove from Playlist")) },
                            onClick = {
                                onRemoveFromPlaylist()
                                onClear()
                                expanded = false
                            }
                        )
                    }

                    if (moveEnabled) {
                        DropdownMenuItem(
                            text = { (Text("Split to Album")) },
                            onClick = {
                                onMove()
                                onClear()
                                expanded = false
                            }
                        )
                    }

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


