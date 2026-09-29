package com.example.musicapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.musicapp.ui.LibraryScreen
import com.example.musicapp.util.toTitleCase

enum class SortField {
    NAME,
    RELEASE_DATE,
    DURATION,
    NUMBER_OF_TRACKS,
    DATE_CREATED,
    DATE_UPDATED,
    TOTAL_COUNT,
    ARTIST_COUNT,
    ALBUM_COUNT
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

        LibraryScreen.ARTIST_DETAIL -> listOf(
            SortField.NAME,
            SortField.RELEASE_DATE,
            SortField.DURATION
        )

        LibraryScreen.PLAYLISTS -> listOf(
            SortField.NAME, SortField.DURATION, SortField.NUMBER_OF_TRACKS,
            SortField.DATE_CREATED, SortField.DATE_UPDATED
        )

        LibraryScreen.GENRES -> listOf(
            SortField.NAME, SortField.TOTAL_COUNT, SortField.ARTIST_COUNT, SortField.ALBUM_COUNT
        )

        LibraryScreen.COUNTRIES -> listOf(
            SortField.NAME, SortField.TOTAL_COUNT, SortField.ARTIST_COUNT, SortField.ALBUM_COUNT
        )


        LibraryScreen.AREAS -> listOf(
            SortField.TOTAL_COUNT, SortField.ARTIST_COUNT, SortField.ALBUM_COUNT
        )

        LibraryScreen.LABELS -> listOf(
            SortField.TOTAL_COUNT, SortField.ARTIST_COUNT, SortField.ALBUM_COUNT
        )

        LibraryScreen.MOODS -> listOf(
            SortField.NAME, SortField.TOTAL_COUNT
        )

        else -> emptyList()
    }


fun isSortable(screen: LibraryScreen): Boolean{
    return availableSortFields(screen).isNotEmpty()
}



@Composable
fun SortMenuContent(
    screen: LibraryScreen,
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                onSortOptionSelected(SortOption(currentSortOption.field, !currentSortOption.ascending))
            }) {
                Icon(
                    imageVector = if (currentSortOption.ascending == true)
                        Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = "Toggle Sort Order"
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        availableSortFields(screen).forEach { field ->
            val isSelected = field == currentSortOption.field
            ListItem(
                headlineContent = { Text(field.name.lowercase().replace("_", " ").toTitleCase()) },
                leadingContent = {
                    RadioButton(
                        selected = isSelected,
                        onClick = null
                    )
                },
                modifier = Modifier.clickable {
                    onSortOptionSelected(SortOption(field, currentSortOption.ascending))
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}
