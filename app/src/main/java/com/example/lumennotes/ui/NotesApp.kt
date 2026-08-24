package com.example.lumennotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lumennotes.NotesViewModel
import com.example.lumennotes.model.Note
import com.example.lumennotes.ui.theme.accentColor
import com.example.lumennotes.ui.theme.accentName
import com.example.lumennotes.ui.theme.accentPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed interface Screen {
    data object List : Screen
    data class Editor(val noteId: String?) : Screen
}

@Composable
fun NotesApp(vm: NotesViewModel = viewModel()) {
    var screen by rememberSaveable(
        saver = androidx.compose.runtime.saveable.Saver(
            save = { state -> (state.value as? Screen.Editor)?.noteId ?: if (state.value is Screen.Editor) "" else "#list" },
            restore = { saved -> mutableStateOf(if (saved == "#list") Screen.List else Screen.Editor(saved.takeIf { it.isNotEmpty() })) },
        )
    ) { mutableStateOf<Screen>(Screen.List) }

    when (val current = screen) {
        is Screen.List -> NoteListScreen(
            vm = vm,
            onNew = { screen = Screen.Editor(null) },
            onOpen = { screen = Screen.Editor(it) },
        )

        is Screen.Editor -> NoteEditorScreen(
            vm = vm,
            noteId = current.noteId,
            onDone = { screen = Screen.List },
        )
    }
}

@Composable
private fun NoteListScreen(vm: NotesViewModel, onNew: () -> Unit, onOpen: (String) -> Unit) {
    val snackbars = remember { SnackbarHostState() }
    val deleted = vm.lastDeleted

    LaunchedEffect(deleted) {
        val note = deleted ?: return@LaunchedEffect
        val result = snackbars.showSnackbar(
            message = "Deleted \"${note.displayTitle}\"",
            actionLabel = "Undo",
        )
        if (result == SnackbarResult.ActionPerformed) vm.undoDelete() else vm.clearUndo()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbars) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                modifier = Modifier.semantics { contentDescription = "New note" },
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New note")
            }
        },
    ) { padding ->
        val notes = vm.visibleNotes
        Column(Modifier.fillMaxSize().padding(padding)) {
            Header(total = vm.notes.size, pinned = vm.pinnedCount)
            SearchField(query = vm.query, onQueryChange = { vm.query = it })

            if (notes.isEmpty()) {
                EmptyState(searching = vm.query.isNotBlank())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onOpen(note.id) },
                            onTogglePin = { vm.togglePin(note.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(total: Int, pinned: Int) {
    Column(Modifier.padding(20.dp, 24.dp, 20.dp, 8.dp)) {
        Text(
            "Lumen",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            when {
                total == 0 -> "A quiet place for your notes"
                pinned > 0 -> "${plural(total, "note")} · $pinned pinned"
                else -> plural(total, "note")
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(16.dp, 4.dp, 16.dp, 8.dp),
        placeholder = { Text("Search notes") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.semantics { contentDescription = "Clear search" },
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onTogglePin: () -> Unit) {
    val accent = accentColor(note.accent)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxSize()
                    .background(accent)
            )
            Column(Modifier.weight(1f).padding(16.dp)) {
                Text(
                    note.displayTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (note.preview.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        note.preview,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Chip(text = accentName(note.accent), color = accent)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${note.wordCount} words · ${formatTime(note.updatedAt)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier
                    .padding(4.dp)
                    .semantics {
                        contentDescription =
                            if (note.pinned) "Unpin ${note.displayTitle}" else "Pin ${note.displayTitle}"
                    },
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = if (note.pinned) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun EmptyState(searching: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(if (searching) "🔍" else "🌙", fontSize = 44.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                if (searching) "No notes match that" else "Nothing written yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (searching) "Try a different word." else "Tap New note to start your first one.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoteEditorScreen(vm: NotesViewModel, noteId: String?, onDone: () -> Unit) {
    val existing = vm.find(noteId)
    var title by rememberSaveable(noteId) { mutableStateOf(existing?.title ?: "") }
    var body by rememberSaveable(noteId) { mutableStateOf(existing?.body ?: "") }
    var accent by rememberSaveable(noteId) { mutableStateOf(existing?.accent ?: (vm.notes.size % accentPalette.size)) }
    var pinned by rememberSaveable(noteId) { mutableStateOf(existing?.pinned ?: false) }

    val canSave = title.isNotBlank() || body.isNotBlank()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onDone,
                    modifier = Modifier.semantics { contentDescription = "Back to notes" },
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                Text(
                    if (existing == null) "New note" else "Edit note",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(
                    onClick = { pinned = !pinned },
                    modifier = Modifier.semantics {
                        contentDescription = if (pinned) "Unpin this note" else "Pin this note"
                    },
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (pinned) accentColor(accent) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    )
                }
                if (existing != null) {
                    IconButton(
                        onClick = {
                            vm.delete(existing.id)
                            onDone()
                        },
                        modifier = Modifier.semantics { contentDescription = "Delete this note" },
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Write something…") },
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "Accent",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(accentPalette.size) { index ->
                    val color = accentPalette[index]
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (accent == index) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = CircleShape,
                            )
                            .clickable { accent = index }
                            .semantics { contentDescription = "${accentName(index)} accent" },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (accent == index) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDone) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                ExtendedFloatingActionButton(
                    onClick = {
                        if (canSave) {
                            vm.upsert(noteId, title, body, accent, pinned)
                            onDone()
                        }
                    },
                    containerColor = if (canSave) accentColor(accent) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (canSave) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { contentDescription = "Save note" },
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save note")
                }
            }
        }
    }
}

private fun plural(count: Int, noun: String): String =
    if (count == 1) "1 $noun" else "$count ${noun}s"

private fun formatTime(millis: Long): String {
    if (millis <= 0L) return "just now"
    val elapsed = System.currentTimeMillis() - millis
    return when {
        elapsed < 60_000 -> "just now"
        elapsed < 3_600_000 -> "${elapsed / 60_000}m ago"
        elapsed < 86_400_000 -> "${elapsed / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}
