package com.example.musicapp.ui.components

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.example.musicapp.LibraryScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopBar(
    currentScreen: LibraryScreen,
    onSearchClick: () -> Unit,
    onSortClick: (SortOption) -> Unit,
    onMenuClick: () -> Unit
) {
    Log.d("top bar", currentScreen.name)
    TopAppBar(
        title = {
            Text(
                text = "Library",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* future */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            SortMenu(screen = currentScreen, onSortSelected = onSortClick)
        }
    )
}

