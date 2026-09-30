package com.example.musicapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PartyMode
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import com.example.musicapp.ui.LibraryScreen
import com.example.musicapp.util.toTitleCase
import java.util.Locale

enum class SortField {
    NAME,
    RELEASE_DATE,
    DURATION,
    NUMBER_OF_TRACKS,
    DATE_CREATED,
    DATE_UPDATED,
    TOTAL_COUNT,
    ARTIST_COUNT,
    ALBUM_COUNT,
    APPROACHABILITY,
    DANCEABILITY,
    ENGAGEMENT,
    MOOD_AGGRESSIVE,
    MOOD_HAPPY,
    MOOD_SAD,
    MOOD_PARTY,
    MOOD_RELAXED,
    BPM,
    DYNAMIC_RANGE,
    LOUDNESS
}

val characteristics = listOf(
    SortField.DANCEABILITY,
    SortField.APPROACHABILITY,
    SortField.ENGAGEMENT,
    SortField.MOOD_RELAXED,
    SortField.MOOD_AGGRESSIVE,
    SortField.MOOD_PARTY,
    SortField.MOOD_HAPPY,
    SortField.MOOD_SAD,
    SortField.BPM,
    SortField.DYNAMIC_RANGE,
    SortField.LOUDNESS
)

data class SortOption(
    val field: SortField = SortField.NAME,
    val ascending: Boolean = true
)

fun availableSortFields(screen: LibraryScreen): List<SortField> =
    when (screen) {
        LibraryScreen.ARTISTS -> listOf(SortField.NAME)
        LibraryScreen.ALBUMS ->
            listOf(SortField.NAME, SortField.RELEASE_DATE, SortField.DURATION, SortField.NUMBER_OF_TRACKS) +
                    characteristics

        LibraryScreen.TRACKS ->
            listOf(SortField.NAME, SortField.DURATION) + characteristics

        LibraryScreen.ARTIST_DETAIL -> listOf(
            SortField.NAME,
            SortField.RELEASE_DATE,
            SortField.DURATION,
            SortField.NUMBER_OF_TRACKS
        ) + characteristics

        LibraryScreen.PLAYLISTS -> listOf(
            SortField.NAME, SortField.DURATION, SortField.NUMBER_OF_TRACKS,
            SortField.DATE_CREATED, SortField.DATE_UPDATED
        ) + characteristics

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
            .navigationBarsPadding()
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            SortFieldsList(
                screen = screen,
                currentSortOption = currentSortOption,
                onSortOptionSelected = onSortOptionSelected
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

@Composable
fun SortFieldsList(
    screen: LibraryScreen,
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit
) {
    val allFields = remember(screen) { availableSortFields(screen) }
    val standardFields = remember(allFields) { allFields.filter { it !in characteristics } }
    val audioFields = remember(allFields) { allFields.filter { it in characteristics } }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (audioFields.isNotEmpty()) {
            SortSectionHeader(title = "General")
        }

        standardFields.forEach { field ->
            SortFieldRow(
                field = field,
                isSelected = field == currentSortOption.field,
                icon = null,
                onSelect = {
                    onSortOptionSelected(SortOption(field, currentSortOption.ascending))
                }
            )
        }

        if (audioFields.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            SortSectionHeader(title = "Audio Characteristics")

            audioFields.forEach { field ->
                SortFieldRow(
                    field = field,
                    isSelected = field == currentSortOption.field,
                    icon = getCharacteristicIcon(field), // Optional helper for custom icons
                    onSelect = {
                        onSortOptionSelected(SortOption(field, currentSortOption.ascending))
                    }
                )
            }
        }
    }
}

@Composable
private fun SortSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun SortFieldRow(
    field: SortField,
    isSelected: Boolean,
    icon: ImageVector?,
    onSelect: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    ListItem(
        headlineContent = {
            Text(
                text = if (field.name.lowercase() != "bpm") field.name.lowercase().replace("_", " ").toTitleCase()
                       else field.name.lowercase().replace("_", " ").uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        },
        leadingContent = {
            RadioButton(
                selected = isSelected,
                onClick = null
            )
        },
        trailingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
    )
}

@Composable
private fun getCharacteristicIcon(field: SortField): ImageVector? {
    return when (field) {
        SortField.APPROACHABILITY -> Icons.Default.MusicNote
        SortField.ENGAGEMENT -> Icons.Default.RocketLaunch
        SortField.DANCEABILITY -> Icons.Default.GraphicEq
        SortField.MOOD_AGGRESSIVE -> Icons.Default.ElectricBolt
        SortField.MOOD_HAPPY -> Icons.Default.SentimentVerySatisfied
        SortField.MOOD_PARTY -> Icons.Default.Celebration
        SortField.MOOD_SAD -> Icons.Default.HeartBroken
        SortField.MOOD_RELAXED -> Icons.Default.Deck
        SortField.BPM -> Icons.Default.Speed
        SortField.DYNAMIC_RANGE -> Icons.Default.Equalizer
        SortField.LOUDNESS -> Icons.AutoMirrored.Filled.VolumeUp
        else -> Icons.Default.AutoAwesome
    }
}