package com.example.recipetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipetracker.RecipeUiState
import com.example.recipetracker.model.GroceryItem

@Composable
fun GroceryScreen(
    state: RecipeUiState,
    contentPadding: PaddingValues,
    onToggle: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onClearPurchased: () -> Unit,
) {
    val toBuy = state.itemsToBuy
    val purchased = state.purchasedItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Grocery list", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Everything you still need from the store.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (state.groceryItems.isNotEmpty()) {
            item { ShoppingProgress(toBuy = toBuy.size, purchased = purchased.size) }
        }

        if (state.groceryItems.isEmpty()) {
            item { GroceryEmptyState() }
        }

        if (toBuy.isNotEmpty()) {
            item { SectionHeader("To buy (${toBuy.size})") }
            items(toBuy, key = { it.id }) { item ->
                GroceryRow(item = item, onToggle = { onToggle(item.id) }, onRemove = { onRemove(item.id) })
            }
        }

        if (purchased.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("In the cart (${purchased.size})", Modifier.weight(1f))
                    TextButton(onClick = onClearPurchased) { Text("Clear") }
                }
            }
            items(purchased, key = { it.id }) { item ->
                GroceryRow(item = item, onToggle = { onToggle(item.id) }, onRemove = { onRemove(item.id) })
            }
        }
    }
}

@Composable
private fun ShoppingProgress(toBuy: Int, purchased: Int) {
    val total = toBuy + purchased
    val fraction = if (total == 0) 0f else purchased.toFloat() / total
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (toBuy == 0) "All done — everything is in the cart." else "$toBuy of $total still to buy",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun GroceryRow(item: GroceryItem, onToggle: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.isPurchased, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                Text(
                    item.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.isPurchased) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                    color = if (item.isPurchased) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                item.recipeName?.let {
                    Text(
                        "for $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onRemove, modifier = Modifier.size(44.dp)) {
                Text("×", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GroceryEmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("🛒", fontSize = 44.sp)
        Text("Nothing on the list yet", style = MaterialTheme.typography.titleLarge)
        Text(
            "Add an item below, or open a recipe and send its ingredients here.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AddGroceryItemDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to the list") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    text,
                    { text = it },
                    label = { Text("What do you need?") },
                    singleLine = true,
                )
                Text(
                    "Amounts are picked up automatically — try \"2 cups flour\".",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(text) }, enabled = text.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
