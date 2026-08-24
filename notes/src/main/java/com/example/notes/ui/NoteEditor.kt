package com.example.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes.model.Note
import com.example.notes.model.NoteColor
import com.example.notes.model.parseTags
import com.example.notes.model.relativeTime
import com.example.notes.ui.theme.tint

/**
 * Full-screen editor. Every keystroke goes straight to the ViewModel, so there is no save
 * button to forget and backing out never loses text.
 */
@Composable
fun NoteEditor(
    note: Note,
    onBack: () -> Unit,
    onChange: (title: String, body: String) -> Unit,
    onColor: (NoteColor) -> Unit,
    onTags: (Set<String>) -> Unit,
    onTogglePin: () -> Unit,
    onToggleTask: (Int) -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showChecklist by remember { mutableStateOf(note.tasks.isNotEmpty()) }
    var tagText by remember(note.id) { mutableStateOf(note.tags.sorted().joinToString(", ")) }
    val now = remember(note.updatedAt) { System.currentTimeMillis() }

    Surface(color = note.color.tint(), modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    TextButton(onClick = onBack) { Text("← Back") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onTogglePin) {
                        Text(if (note.isPinned) "★ Pinned" else "☆ Pin")
                    }
                    TextButton(onClick = onArchive) {
                        Text(if (note.isArchived) "Unarchive" else "Archive")
                    }
                    TextButton(onClick = { confirmDelete = true }) { Text("Delete") }
                }

                TransparentField(
                    value = note.title,
                    onValueChange = { onChange(it, note.body) },
                    placeholder = "Title",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    singleLine = true,
                )

                Text(
                    text = "Edited ${relativeTime(now, note.updatedAt)} · ${note.wordCount} words",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Spacer(Modifier.height(8.dp))
                Palette(selected = note.color, onSelect = onColor)
                Spacer(Modifier.height(8.dp))

                if (note.tasks.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    ) {
                        Text(
                            text = "${note.doneTasks}/${note.tasks.size} tasks",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { showChecklist = !showChecklist }) {
                            Text(if (showChecklist) "Edit as text" else "Show checklist")
                        }
                    }
                }

                if (showChecklist && note.tasks.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        note.tasks.forEachIndexed { index, task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleTask(index) }
                                    .padding(vertical = 2.dp),
                            ) {
                                Checkbox(checked = task.isDone, onCheckedChange = { onToggleTask(index) })
                                Text(text = task.text, fontSize = 15.sp)
                            }
                        }
                    }
                } else {
                    TransparentField(
                        value = note.body,
                        onValueChange = { onChange(note.title, it) },
                        placeholder = "Start writing. Use \"- [ ] thing\" for a checklist.",
                        fontSize = 16.sp,
                        minHeight = 220.dp,
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = tagText,
                    onValueChange = {
                        tagText = it
                        onTags(parseTags(it))
                    },
                    label = { Text("Tags") },
                    placeholder = { Text("work, ideas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                )

                Spacer(Modifier.height(24.dp))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this note?") },
            text = { Text("You can undo this from the note list.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun Palette(selected: NoteColor, onSelect: (NoteColor) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        NoteColor.entries.forEach { color ->
            val isSelected = color == selected
            ColorSwatch(
                color = color,
                selected = isSelected,
                onClick = { onSelect(color) },
            )
        }
    }
}

@Composable
private fun ColorSwatch(color: NoteColor, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(if (selected) 34.dp else 28.dp)
            .clip(CircleShape)
            .background(color.tint())
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

/** A text field that reads as plain paper: no box, no fill, just the note. */
@Composable
private fun TransparentField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fontSize: TextUnit,
    fontWeight: FontWeight? = null,
    singleLine: Boolean = false,
    minHeight: Dp = 0.dp,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = fontSize, fontWeight = fontWeight) },
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize,
            fontWeight = fontWeight,
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = minHeight),
    )
}
