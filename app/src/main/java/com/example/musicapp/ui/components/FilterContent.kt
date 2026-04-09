package com.example.musicapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.musicapp.data.repository.FilterLogic
import com.example.musicapp.data.repository.LibraryFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.graphics.Color
import com.example.musicapp.ui.viewmodels.FilterDefaults

@Composable
fun FilterDrawerContent(
    draft: LibraryFilter,
    potentialCount: Int,
    filterDefaults: FilterDefaults,
    onDraftChange: (LibraryFilter) -> Unit,
    onApply: () -> Unit
){
    LazyColumn {
        item {
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text("Match All")
                Switch(
                    checked = draft.logic == FilterLogic.OR,
                    onCheckedChange = {
                        val newDraft = draft.copy(logic = if (draft.logic == FilterLogic.OR) FilterLogic.AND else FilterLogic.OR)
                        onDraftChange(newDraft)
                    }
                )
                Text("Match Any")
            }
        }

        item{
            LabelPicker(
                allLabels = filterDefaults.recordLabels,
                selectedLabels = draft.selectedLabels,
                onLabelToggle = { label->
                    val newLabels = if (draft.selectedLabels.contains(label)) {
                        draft.selectedLabels - label
                    } else {
                        draft.selectedLabels + label
                    }
                    onDraftChange(draft.copy(selectedLabels = newLabels))
                }
            )
        }

        item{
            DateRangeSection(
                draft,
                filterDefaults,
                onDraftChange
            )
        }


        item {
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show $potentialCount Results")
            }
        }

    }

}


@Composable
fun DateRangeSection(
    draft: LibraryFilter,
    filterDefaults: FilterDefaults,
    onDraftChange: (LibraryFilter) -> Unit
) {
    Column {
        Text("Release Years")

        FlowRow {
            draft.dateRanges.forEach { range ->
                InputChip(
                    selected = true,
                    onClick = { /* Maybe edit? */ },
                    label = { Text("${range.first} - ${range.last}") },
                    trailingIcon = {
                        Icon(Icons.Default.Close, "Remove",
                            Modifier.clickable {
                                val newList = draft.dateRanges.filter { it != range }
                                onDraftChange(draft.copy(dateRanges = newList))
                            }
                        )
                    }
                )
            }
        }

        DateRangePicker(draft, filterDefaults, onDraftChange)

        IconButton(onClick = {
            val updatedSaved = draft.dateRanges + listOf<IntRange>(draft.activeRange)
            onDraftChange(draft.copy(
                dateRanges = updatedSaved,
                activeRange = filterDefaults.minYear..filterDefaults.maxYear
            ))
        }) {
            Icon(Icons.Default.Add, "Add another range")
        }
    }
}


@Composable
fun DateRangePicker(
    draft: LibraryFilter,
    filterDefaults: FilterDefaults,
    onDraftChange: (LibraryFilter) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "From: ${draft.activeRange.first}", style = MaterialTheme.typography.labelLarge)
            Text(text = "To: ${draft.activeRange.last}", style = MaterialTheme.typography.labelLarge)
        }

        RangeSlider(
            value = draft.activeRange.first.toFloat()..draft.activeRange.last.toFloat(),
            onValueChange = { range ->
                onDraftChange(draft.copy(activeRange = range.start.toInt()..range.endInclusive.toInt()))
            },
            valueRange = filterDefaults.minYear.toFloat()..filterDefaults.maxYear.toFloat(),
            steps = filterDefaults.maxYear - filterDefaults.minYear,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                inactiveTrackColor = Color.Gray,
                thumbColor = Color.White,
                activeTickColor = MaterialTheme.colorScheme.onSurfaceVariant,
                inactiveTickColor = Color.Gray
            )
        )
    }
}

@Composable
fun LabelPicker(
    allLabels: List<String>,
    selectedLabels: Set<String>,
    onLabelToggle: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLabels = allLabels.filter { it.contains(searchQuery, ignoreCase = true) }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search labels...") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
            items(filteredLabels) { label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedLabels.contains(label),
                        onCheckedChange = { onLabelToggle(label) }
                    )
                    Text(label)
                }
            }
        }
    }
}