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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipetracker.RecipeUiState
import com.example.recipetracker.RecipeViewModel
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter

enum class AppTab(val label: String, val glyph: String) {
    Recipes("Recipes", "🍲"),
    Groceries("Groceries", "🛒"),
}

@Composable
fun RecipeApp(viewModel: RecipeViewModel = viewModel()) {
    val state = viewModel.state
    var tab by remember { mutableStateOf(AppTab.Recipes) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showAddRecipeDialog by remember { mutableStateOf(false) }
    var showAddGroceryDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { entry ->
                    val badged = entry == AppTab.Groceries && state.toBuyCount > 0
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { tab = entry },
                        icon = { Text(entry.glyph, fontSize = 20.sp) },
                        label = { Text(if (badged) "${entry.label} (${state.toBuyCount})" else entry.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (tab) {
                        AppTab.Recipes -> showAddRecipeDialog = true
                        AppTab.Groceries -> showAddGroceryDialog = true
                    }
                },
            ) { Text("+", fontSize = 26.sp) }
        },
    ) { padding ->
        val contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = padding.calculateTopPadding() + 20.dp,
            bottom = padding.calculateBottomPadding() + 96.dp,
        )
        when (tab) {
            AppTab.Recipes -> RecipeListScreen(
                state = state,
                onQueryChange = viewModel::setQuery,
                onFilterChange = viewModel::setFilter,
                onFavorite = viewModel::toggleFavorite,
                onSelect = { selectedRecipe = it },
                contentPadding = contentPadding,
            )
            AppTab.Groceries -> GroceryListScreen(
                groceryItems = state.visibleGroceryItems,
                filter = state.groceryFilter,
                toBuyCount = state.toBuyCount,
                inCartCount = state.inCartCount,
                onFilterChange = viewModel::setGroceryFilter,
                onToggle = viewModel::toggleGroceryItem,
                onRemove = viewModel::removeGroceryItem,
                onClearChecked = viewModel::clearCheckedGroceryItems,
                contentPadding = contentPadding,
            )
        }
    }

    selectedRecipe?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            onAddToGroceryList = { multiplier -> viewModel.addRecipeToGroceryList(recipe, multiplier) },
            onDismiss = { selectedRecipe = null },
        )
    }
    if (showAddRecipeDialog) {
        AddRecipeDialog(
            onDismiss = { showAddRecipeDialog = false },
            onAdd = { name, minutes, tag ->
                viewModel.addRecipe(name, minutes, tag)
                showAddRecipeDialog = false
            },
        )
    }
    if (showAddGroceryDialog) {
        AddGroceryItemDialog(
            onDismiss = { showAddGroceryDialog = false },
            onAdd = { name, quantity ->
                viewModel.addGroceryItem(name, quantity)
                showAddGroceryDialog = false
            },
        )
    }
}

@Composable
private fun RecipeListScreen(
    state: RecipeUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (RecipeFilter) -> Unit,
    onFavorite: (Long) -> Unit,
    onSelect: (Recipe) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
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
                onValueChange = onQueryChange,
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
                        onClick = { onFilterChange(filter) },
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
                    onClick = { onSelect(recipe) },
                    onFavorite = { onFavorite(recipe.id) },
                )
            }
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
internal fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
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
    onAddToGroceryList: (Double) -> Int,
    onDismiss: () -> Unit,
) {
    var servings by remember(recipe.id) { mutableIntStateOf(recipe.servings) }
    var groceryMessage by remember(recipe.id) { mutableStateOf<String?>(null) }
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
                item {
                    OutlinedButton(
                        onClick = {
                            val added = onAddToGroceryList(multiplier)
                            groceryMessage = when (added) {
                                0 -> "Already on your grocery list."
                                1 -> "Added 1 item to your grocery list."
                                else -> "Added $added items to your grocery list."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Add ingredients to grocery list") }
                }
                groceryMessage?.let { message ->
                    item {
                        Text(
                            message,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                item { HorizontalDivider(); Text("Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(recipe.steps.indices.toList()) { index -> Text("${index + 1}. ${recipe.steps[index]}") }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } },
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
