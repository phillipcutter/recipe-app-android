package com.example.recipetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipetracker.AppTab
import com.example.recipetracker.RecipeViewModel
import com.example.recipetracker.model.GroceryItem
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter

@Composable
fun RecipeApp(viewModel: RecipeViewModel = viewModel()) {
    val state = viewModel.state
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.semantics {
                    contentDescription = if (state.tab == AppTab.Grocery) {
                        "Add grocery item"
                    } else {
                        "Add recipe"
                    }
                },
            ) {
                Text("+", fontSize = 26.sp)
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.tab == AppTab.Recipes,
                    onClick = { viewModel.setTab(AppTab.Recipes) },
                    icon = { Text("🍽") },
                    label = { Text("Recipes") },
                    modifier = Modifier.semantics { contentDescription = "Recipes tab" },
                )
                NavigationBarItem(
                    selected = state.tab == AppTab.Grocery,
                    onClick = { viewModel.setTab(AppTab.Grocery) },
                    icon = { Text("🛒") },
                    label = { Text("Grocery") },
                    modifier = Modifier.semantics { contentDescription = "Grocery tab" },
                )
            }
        },
    ) { padding ->
        when (state.tab) {
            AppTab.Recipes -> RecipeListScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
                onOpenRecipe = { selectedRecipe = it },
            )
            AppTab.Grocery -> GroceryListScreen(
                items = state.groceryItems,
                remaining = state.groceryUncheckedCount,
                modifier = Modifier.padding(padding),
                onToggle = viewModel::toggleGroceryItem,
                onRemove = viewModel::removeGroceryItem,
                onClearChecked = viewModel::clearCheckedGroceryItems,
            )
        }
    }

    selectedRecipe?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            onDismiss = { selectedRecipe = null },
            onAddToGrocery = {
                viewModel.addIngredientsToGrocery(recipe.ingredients)
                selectedRecipe = null
            },
        )
    }
    if (showAddDialog) {
        if (state.tab == AppTab.Grocery) {
            AddGroceryDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, note ->
                    viewModel.addGroceryItem(name, note)
                    showAddDialog = false
                },
            )
        } else {
            AddRecipeDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, minutes, tag ->
                    viewModel.addRecipe(name, minutes, tag)
                    showAddDialog = false
                },
            )
        }
    }
}

@Composable
private fun RecipeListScreen(
    viewModel: RecipeViewModel,
    modifier: Modifier = Modifier,
    onOpenRecipe: (Recipe) -> Unit,
) {
    val state = viewModel.state
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("My recipes", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "A little collection of things worth cooking.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { StatsRow(state.recipes.size, state.favoriteCount, state.averagePrep) }
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search recipes or ingredients") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RecipeFilter.entries) { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }
        }
        if (state.visibleRecipes.isEmpty()) {
            item { EmptyState() }
        } else {
            items(state.visibleRecipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onOpenRecipe(recipe) },
                    onFavorite = { viewModel.toggleFavorite(recipe.id) },
                )
            }
        }
    }
}

@Composable
private fun GroceryListScreen(
    items: List<GroceryItem>,
    remaining: Int,
    modifier: Modifier = Modifier,
    onToggle: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onClearChecked: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Grocery list", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                if (remaining == 0) "Nothing left to buy."
                else "$remaining item${if (remaining == 1) "" else "s"} still to pick up.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("$remaining", "To buy", Modifier.weight(1f))
                Stat("${items.size}", "On list", Modifier.weight(1f))
                Stat("${items.count { it.checked }}", "In cart", Modifier.weight(1f))
            }
        }
        if (items.any { it.checked }) {
            item {
                TextButton(onClick = onClearChecked) { Text("Clear checked items") }
            }
        }
        if (items.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Your list is empty", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Add items or pull ingredients from a recipe.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                GroceryRow(item = item, onToggle = { onToggle(item.id) }, onRemove = { onRemove(item.id) })
            }
        }
    }
}

@Composable
private fun GroceryRow(item: GroceryItem, onToggle: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.semantics { contentDescription = "Check ${item.name}" },
            )
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                )
                if (item.note.isNotBlank()) {
                    Text(item.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = onRemove) { Text("Remove") }
        }
    }
}

@Composable
private fun StatsRow(total: Int, favorites: Int, average: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Stat("$total", "Recipes", Modifier.weight(1f))
        Stat("$favorites", "Favorites", Modifier.weight(1f))
        Stat("$average min", "Avg. prep", Modifier.weight(1f))
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeCard(recipe: Recipe, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(recipe.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        recipe.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = onFavorite, modifier = Modifier.size(48.dp)) {
                    Text(if (recipe.isFavorite) "♥" else "♡", fontSize = 24.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${recipe.prepMinutes} min", style = MaterialTheme.typography.labelLarge)
                Text("•")
                Text("${recipe.servings} servings", style = MaterialTheme.typography.labelLarge)
                Text("•")
                Text(recipe.difficulty, style = MaterialTheme.typography.labelLarge)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recipe.tags.toList()) { tag -> AssistChip(onClick = {}, label = { Text(tag) }) }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No recipes found", style = MaterialTheme.typography.titleLarge)
        Text("Try another search or filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecipeDetailDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onAddToGrocery: () -> Unit,
) {
    var servings by remember(recipe.id) { mutableIntStateOf(recipe.servings) }
    val multiplier = servings.toDouble() / recipe.servings
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recipe.name) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text(recipe.description, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Servings", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { if (servings > 1) servings-- }) { Text("−") }
                        Text("$servings", modifier = Modifier.padding(horizontal = 14.dp))
                        OutlinedButton(onClick = { servings++ }) { Text("+") }
                    }
                }
                item { Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(recipe.ingredients) { Text("• ${it.displayAmount(multiplier)}") }
                item { HorizontalDivider(); Text("Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(recipe.steps.indices.toList()) { index -> Text("${index + 1}. ${recipe.steps[index]}") }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } },
        dismissButton = { TextButton(onClick = onAddToGrocery) { Text("Add to grocery") } },
    )
}

@Composable
private fun AddGroceryDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a grocery item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Item") }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, note) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AddRecipeDialog(onDismiss: () -> Unit, onAdd: (String, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("30") }
    var tag by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a recipe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Recipe name") }, singleLine = true)
                OutlinedTextField(
                    minutes,
                    { minutes = it.filter(Char::isDigit) },
                    label = { Text("Prep time (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(tag, { tag = it }, label = { Text("Tag (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, minutes.toIntOrNull() ?: 30, tag) }, enabled = name.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
