package com.example.musicapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.musicapp.ui.LibraryScreen

enum class SortField {
    NAME,
    RELEASE_DATE,
    DURATION,
    TRACK_NUM,
    DATE_CREATED,
    DATE_UPDATED,
    TOTAL_COUNT,
    ARTIST_COUNT,
    ALBUM_COUNT
}

enum class MenuPage { MAIN, SORT }


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

        LibraryScreen.ALBUM_DETAIL -> listOf(
            SortField.NAME,
            SortField.RELEASE_DATE,
            SortField.DURATION
        )

        LibraryScreen.PLAYLISTS -> listOf(
            SortField.NAME, SortField.DURATION, SortField.TRACK_NUM,
            SortField.DATE_CREATED, SortField.DATE_UPDATED
        )

        LibraryScreen.GENRES -> listOf(
            SortField.NAME, SortField.TOTAL_COUNT, SortField.ARTIST_COUNT, SortField.ALBUM_COUNT
        )

        else -> emptyList()
    }


@Composable
fun SortMenu(
    screen: LibraryScreen,
    onSortSelected: (SortOption) -> Unit,
) {

    availableSortFields(screen).forEach { field ->
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "Sort by ${
                            field.name.lowercase().replace("_", " ")
                                .replaceFirstChar { it.uppercase() }
                        } "
                    )
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "ASC",
                        modifier = Modifier.size(16.dp),
                    )
                }
            },
            onClick = {
                onSortSelected(SortOption(field, ascending = true))
            }
        )
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.Bottom) {

                    Text(
                        "Sort by ${
                            field.name.lowercase().replace("_", " ")
                                .replaceFirstChar { it.uppercase() }
                        } "
                    )

                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "DESC",
                        modifier = Modifier.size(16.dp),
                    )

                }
            },
            onClick = {
                onSortSelected(SortOption(field, ascending = false))
            }
        )
    }


}


@Composable
fun RightMenu(
    screen: LibraryScreen,
    onSortSelected: (SortOption) -> Unit,
    onImport: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var menuPage by remember { mutableStateOf(MenuPage.MAIN) }


    Box {

        IconButton(
            onClick = {
                expanded = true
                menuPage = MenuPage.MAIN
            }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            when (menuPage) {
                MenuPage.MAIN -> {
                    if (screen == LibraryScreen.PLAYLISTS && onImport != null) {
                        DropdownMenuItem(
                            text = { Text("Import .m3u") },
                            onClick = {
                                expanded = false
                                onImport()
                            }
                        )
                    }

                    if (availableSortFields(screen).isNotEmpty()) {

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Sort")
                                    Icon(Icons.AutoMirrored.Filled.ArrowRight, null)
                                }
                            },
                            onClick = { menuPage = MenuPage.SORT }
                        )
                    }
                }


                MenuPage.SORT -> {

                    if (availableSortFields(screen).isNotEmpty()) {

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowLeft, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Back", fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = { menuPage = MenuPage.MAIN }
                        )
                        SortMenu(
                            screen = screen,
                            onSortSelected = { screen ->
                                expanded = false
                                onSortSelected(screen)
                            },
                        )
                    }
                }

            }
        }
    }
}