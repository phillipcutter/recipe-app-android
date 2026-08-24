package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.data.PreferencesNoteStore
import com.example.notes.ui.NotesApp
import com.example.notes.ui.theme.NotebookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = PreferencesNoteStore(this)
        setContent {
            NotebookTheme {
                NotesApp(viewModel = viewModel(factory = NotesViewModel.factory(store)))
            }
        }
    }
}
