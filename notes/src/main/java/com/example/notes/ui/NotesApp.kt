package com.example.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.NotesViewModel
import com.example.notes.model.Note
import com.example.notes.model.NoteFilter
import com.example.notes.model.relativeTime
import com.example.notes.ui.theme.tint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesApp(viewModel: NotesViewModel = viewModel()) {
    val state = viewModel.state
    var editingId by remember { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val editing = editingId?.let(viewModel::noteById)
    if (editing != null) {
        NoteEditor(
            note = editing,
            onBack = { editingId = null },
            onChange = { title, body -> viewModel.updateNote(editing.id, title = title, body = body) },
            onColor = { viewModel.updateNote(editing.id, color = it) },
            onTags = { viewModel.setTags(editing.id, it) },
            onTogglePin = { viewModel.togglePinned(editing.id) },
            onToggleTask = { viewModel.toggleTask(editing.id, it) },
            onArchive = {
                viewModel.setArchived(editing.id, !editing.isArchived)
                editingId = null
            },
            onDelete = {
                viewModel.deleteNote(editing.id)
                editingId = null
            },
        )
        return
    }

    // The undo bar is driven by state rather than fired at the call site, so an archive from
    // inside the editor still surfaces here after the editor closes.
    LaunchedEffect(state.undo) {
        val pending = state.undo ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(pending.label, actionLabel = "Undo")
        if (result == SnackbarResult.ActionPerformed) viewModel.undo() else viewModel.dismissUndo()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingId = viewModel.createNote().id }) {
                Text("+", fontSize = 26.sp)
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Header(
                noteCount = state.visibleNotes.size,
                pinnedCount = state.pinnedCount,
                openTasks = state.openTaskCount,
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                singleLine = true,
                placeholder = { Text("Search notes, tags, anything") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                items(NoteFilter.entries) { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }

            if (state.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    items(state.tags) { (tag, count) ->
                        FilterChip(
                            selected = state.activeTag == tag,
                            onClick = { viewModel.toggleTag(tag) },
                            label = { Text("#$tag  $count") },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.visibleNotes.isEmpty()) {
                EmptyState(query = state.query, filter = state.filter)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 168.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.visibleNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onOpen = { editingId = note.id },
                            onTogglePin = { viewModel.togglePinned(note.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(noteCount: Int, pinnedCount: Int, openTasks: Int) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)) {
        Text("Notebook", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            text = buildString {
                append(if (noteCount == 1) "1 note" else "$noteCount notes")
                if (pinnedCount > 0) append(" · $pinnedCount pinned")
                if (openTasks > 0) append(" · $openTasks open tasks")
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCard(note: Note, onOpen: () -> Unit, onTogglePin: () -> Unit) {
    val now = remember(note.updatedAt) { System.currentTimeMillis() }
    Card(
        onClick = onOpen,
        colors = CardDefaults.cardColors(containerColor = note.color.tint()),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = note.displayTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onTogglePin, contentPadding = PaddingValues(4.dp)) {
                    Text(if (note.isPinned) "★" else "☆", fontSize = 16.sp)
                }
            }

            if (note.tasks.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                TaskProgress(done = note.doneTasks, total = note.tasks.size)
            } else if (note.preview.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = note.body.trim(),
                    fontSize = 13.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (note.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = note.tags.sorted().joinToString(" ") { "#$it" },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = relativeTime(now, note.updatedAt),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun TaskProgress(done: Int, total: Int) {
    Column {
        Text(
            text = "$done of $total done",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else done.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
        )
    }
}

@Composable
private fun EmptyState(query: String, filter: NoteFilter) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 48.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("✎", fontSize = 26.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = when {
                    query.isNotBlank() -> "Nothing matches \"$query\""
                    filter == NoteFilter.Archived -> "No archived notes"
                    filter == NoteFilter.Pinned -> "No pinned notes yet"
                    filter == NoteFilter.Tasks -> "No checklists yet"
                    else -> "Your notebook is empty"
                },
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap + to start a note. Lines like \"- [ ] buy milk\" become a checklist.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
