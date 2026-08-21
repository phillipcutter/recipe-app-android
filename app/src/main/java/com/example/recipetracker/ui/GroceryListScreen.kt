package com.example.recipetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.recipetracker.model.GroceryFilter
import com.example.recipetracker.model.GroceryItem

@Composable
fun GroceryListScreen(
    groceryItems: List<GroceryItem>,
    filter: GroceryFilter,
    toBuyCount: Int,
    inCartCount: Int,
    onFilterChange: (GroceryFilter) -> Unit,
    onToggle: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onClearChecked: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Grocery list", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Everything you still need from the store.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("$toBuyCount", "To buy", Modifier.weight(1f))
                Stat("$inCartCount", "In cart", Modifier.weight(1f))
                Stat("${groceryItems.size}", "Showing", Modifier.weight(1f))
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GroceryFilter.entries) { entry ->
                    FilterChip(
                        selected = filter == entry,
                        onClick = { onFilterChange(entry) },
                        label = { Text(entry.label) },
                    )
                }
            }
        }
        if (inCartCount > 0) {
            item {
                TextButton(onClick = onClearChecked) {
                    Text(if (inCartCount == 1) "Clear 1 item in cart" else "Clear $inCartCount items in cart")
                }
            }
        }
        if (groceryItems.isEmpty()) {
            item { GroceryEmptyState(filter) }
        } else {
            items(groceryItems, key = { it.id }) { groceryItem ->
                GroceryRow(
                    groceryItem = groceryItem,
                    onToggle = { onToggle(groceryItem.id) },
                    onRemove = { onRemove(groceryItem.id) },
                )
            }
        }
    }
}

@Composable
private fun GroceryRow(groceryItem: GroceryItem, onToggle: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (groceryItem.isChecked) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = groceryItem.isChecked, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(
                    groceryItem.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (groceryItem.isChecked) TextDecoration.LineThrough else null,
                    color = if (groceryItem.isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (groceryItem.recipeName != null) {
                    Text(
                        "for ${groceryItem.recipeName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onRemove) { Text("✕") }
        }
    }
}

@Composable
private fun GroceryEmptyState(filter: GroceryFilter) {
    val title: String
    val subtitle: String
    when (filter) {
        GroceryFilter.All -> {
            title = "Nothing on the list yet"
            subtitle = "Tap + to add an item, or open a recipe and add its ingredients."
        }
        GroceryFilter.ToBuy -> {
            title = "All done here"
            subtitle = "Everything on your list is already in the cart."
        }
        GroceryFilter.InCart -> {
            title = "Nothing in the cart yet"
            subtitle = "Check items off as you pick them up."
        }
    }
    Column(
        Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun AddGroceryItemDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to grocery list") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Item") }, singleLine = true)
                OutlinedTextField(
                    quantity,
                    { quantity = it },
                    label = { Text("Quantity (optional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, quantity) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
