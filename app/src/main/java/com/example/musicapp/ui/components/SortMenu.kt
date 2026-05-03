package com.example.musicapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.musicapp.LibraryScreen

enum class SortField {
    NAME,
    RELEASE_DATE,
    DURATION
}

data class SortOption(
    val field: SortField = SortField.NAME,
    val ascending: Boolean = true
)

fun availableSortFields(screen: LibraryScreen): List<SortField> =
    when (screen) {
        LibraryScreen.ARTISTS -> listOf(SortField.NAME)
        LibraryScreen.ALBUMS ->
            listOf(SortField.NAME, SortField.RELEASE_DATE, SortField.DURATION)
        LibraryScreen.TRACKS ->
            listOf(SortField.NAME, SortField.DURATION)
        LibraryScreen.ALBUM_DETAIL -> listOf(SortField.NAME, SortField.RELEASE_DATE, SortField.DURATION)
        else -> emptyList()
    }


@Composable
fun SortMenu(screen: LibraryScreen,
             onSortSelected: (SortOption) -> Unit,
             onImport: (() -> Unit)? = null) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Sort")
    }


    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        if (onImport != null && availableSortFields(screen).isEmpty()) {
            DropdownMenuItem(
                text = { Text("Import...") },
                onClick = onImport
            )
        }

        Column {
            availableSortFields(screen).forEach { field ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "Sort by ${
                                field.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                            } ASC"
                        )
                    },
                    onClick = {
                        expanded = false
                        onSortSelected(SortOption(field, ascending = true))
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Sort by ${
                                field.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                            } DESC"
                        )
                    },
                    onClick = {
                        expanded = false
                        onSortSelected(SortOption(field, ascending = false))
                    }
                )
            }


        }
    }
}