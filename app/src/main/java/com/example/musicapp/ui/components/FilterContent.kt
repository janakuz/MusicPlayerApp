package com.example.musicapp.ui.components

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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import com.example.musicapp.ui.screens.GenrePicker
import com.example.musicapp.ui.viewmodels.FilterDefaults
import kotlin.math.roundToInt

@Composable
fun FilterDrawerContent(
    draft: LibraryFilter,
    potentialCount: Int,
    filterDefaults: FilterDefaults,
    labelSuggestions: List<String>,
    onDraftChange: (LibraryFilter) -> Unit,
    onApply: () -> Unit,
    onLabelQueryChange: (String) -> Unit,
    interaction: MutableInteractionSource,
){
    val dummyFocusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .focusRequester(dummyFocusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ){
                dummyFocusRequester.requestFocus()
            }
    ) {
        item {
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text("Match All")
                Switch(
                    checked = draft.logic == FilterLogic.OR,
                    onCheckedChange = {
                        val newDraft = draft.copy(logic = if (draft.logic == FilterLogic.OR) FilterLogic.AND else FilterLogic.OR)
                        onDraftChange(newDraft)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text("Match Any")
            }
        }

        item{
            GenrePicker(
                genres = draft.selectedLabels.toList(),
                suggestions = labelSuggestions,
                onGenreQueryChange = {query -> onLabelQueryChange(query)},
                onGenresChange = { newLabels ->
                    onDraftChange(draft.copy(selectedLabels = newLabels.toSet()))
                    onLabelQueryChange("")
                },
                label = "Label",
                titleCase = false
            )
        }

        item{
            DateRangeSection(
                draft,
                filterDefaults,
                interaction,
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
    interaction: MutableInteractionSource,
    onDraftChange: (LibraryFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
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

        DateRangePicker(draft, filterDefaults, interaction, onDraftChange)

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    draft: LibraryFilter,
    filterDefaults: FilterDefaults,
    interaction: MutableInteractionSource,
    onDraftChange: (LibraryFilter) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "From: ${draft.activeRange.first}", style = MaterialTheme.typography.labelLarge)
            Text(text = "To: ${draft.activeRange.last}", style = MaterialTheme.typography.labelLarge)
        }

        var lastStart by remember(draft.activeRange) { mutableIntStateOf(draft.activeRange.first) }
        var lastEnd by remember(draft.activeRange) { mutableIntStateOf(draft.activeRange.last) }

        RangeSlider(
            value = lastStart.toFloat()..lastEnd.toFloat(),
            onValueChange = { range ->
                val newStart = range.start.roundToInt()
                val newEnd = range.endInclusive.roundToInt()

                if (newStart != lastStart || newEnd != lastEnd) {
                    lastStart = newStart
                    lastEnd = newEnd

                    onDraftChange(draft.copy(activeRange = newStart..newEnd))
                }
            },
            startInteractionSource = interaction,
            endInteractionSource = interaction,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                    }
                },
            valueRange = filterDefaults.minYear.toFloat()..filterDefaults.maxYear.toFloat(),
            steps = (filterDefaults.maxYear - filterDefaults.minYear) - 1
        )
    }
}
