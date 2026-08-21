package com.example.recipetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipetracker.RecipeViewModel
import com.example.recipetracker.model.GroceryItem
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    Recipes("Recipes"),
    Grocery("Grocery List"),
}

@Composable
fun RecipeApp(viewModel: RecipeViewModel = viewModel()) {
    val state = viewModel.state
    var selectedTab by remember { mutableStateOf(AppTab.Recipes) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showAddRecipeDialog by remember { mutableStateOf(false) }
    var showAddGroceryDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == AppTab.Recipes,
                    onClick = { selectedTab = AppTab.Recipes },
                    label = { Text("Recipes") },
                    icon = { Text("📖", fontSize = 18.sp) },
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.Grocery,
                    onClick = { selectedTab = AppTab.Grocery },
                    label = { Text("Grocery List") },
                    icon = { Text("🛒", fontSize = 18.sp) },
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == AppTab.Recipes) {
                FloatingActionButton(onClick = { showAddRecipeDialog = true }) {
                    Text("+", fontSize = 26.sp)
                }
            } else {
                FloatingActionButton(onClick = { showAddGroceryDialog = true }) {
                    Text("+", fontSize = 26.sp)
                }
            }
        },
    ) { padding ->
        when (selectedTab) {
            AppTab.Recipes -> {
                RecipesScreen(
                    state = state,
                    padding = padding,
                    onQueryChange = viewModel::setQuery,
                    onFilterChange = viewModel::setFilter,
                    onRecipeClick = { selectedRecipe = it },
                    onFavoriteClick = viewModel::toggleFavorite,
                )
            }
            AppTab.Grocery -> {
                GroceryScreen(
                    groceryItems = state.groceryItems,
                    padding = padding,
                    onToggleItem = viewModel::toggleGroceryItem,
                    onDeleteItem = viewModel::deleteGroceryItem,
                    onClearCompleted = viewModel::clearCompletedGroceryItems,
                )
            }
        }
    }

    selectedRecipe?.let { recipe ->
        RecipeDetailDialog(
            recipe = recipe,
            onDismiss = { selectedRecipe = null },
            onAddIngredientsToGrocery = { multiplier ->
                viewModel.addRecipeIngredientsToGrocery(recipe, multiplier)
                selectedRecipe = null
                scope.launch {
                    snackbarHostState.showSnackbar("Added ingredients to Grocery List")
                }
            },
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
            onAdd = { name, amount ->
                viewModel.addGroceryItem(name, amount)
                showAddGroceryDialog = false
            },
        )
    }
}

@Composable
private fun RecipesScreen(
    state: com.example.recipetracker.RecipeUiState,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onQueryChange: (String) -> Unit,
    onFilterChange: (RecipeFilter) -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp,
        ),
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
                    androidx.compose.material3.FilterChip(
                        selected = state.filter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }
        }
        if (state.visibleRecipes.isEmpty()) {
            item { EmptyRecipesState() }
        } else {
            items(state.visibleRecipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    onFavorite = { onFavoriteClick(recipe.id) },
                )
            }
        }
    }
}

@Composable
private fun GroceryScreen(
    groceryItems: List<GroceryItem>,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onToggleItem: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearCompleted: () -> Unit,
) {
    val pendingItems = groceryItems.filter { !it.isChecked }
    val completedItems = groceryItems.filter { it.isChecked }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Grocery List", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text(
                        if (groceryItems.isEmpty()) "Your grocery list is empty."
                        else "${pendingItems.size} remaining • ${completedItems.size} completed",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (completedItems.isNotEmpty()) {
                    TextButton(onClick = onClearCompleted) {
                        Text("Clear completed")
                    }
                }
            }
        }

        if (groceryItems.isEmpty()) {
            item { EmptyGroceryState() }
        } else {
            if (pendingItems.isNotEmpty()) {
                item {
                    Text("To Buy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(pendingItems, key = { it.id }) { item ->
                    GroceryItemRow(
                        item = item,
                        onToggle = { onToggleItem(item.id) },
                        onDelete = { onDeleteItem(item.id) },
                    )
                }
            }

            if (completedItems.isNotEmpty()) {
                item {
                    Text(
                        "Completed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                items(completedItems, key = { it.id }) { item ->
                    GroceryItemRow(
                        item = item,
                        onToggle = { onToggleItem(item.id) },
                        onDelete = { onDeleteItem(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GroceryItemRow(
    item: GroceryItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                if (item.amountAndUnit.isNotBlank()) {
                    Text(
                        text = item.amountAndUnit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    )
                }
            }
            TextButton(onClick = onDelete) {
                Text("✕", fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyGroceryState() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No grocery items yet", style = MaterialTheme.typography.titleLarge)
        Text("Add items manually or from recipe details.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun EmptyRecipesState() {
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
    onAddIngredientsToGrocery: (Double) -> Unit,
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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                items(recipe.ingredients) { Text("• ${it.displayAmount(multiplier)}") }
                item {
                    OutlinedButton(
                        onClick = { onAddIngredientsToGrocery(multiplier) },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) {
                        Text("🛒 Add ingredients to Grocery List")
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

@Composable
private fun AddGroceryItemDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Grocery List") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name (e.g., Milk)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount / notes (e.g., 1 gallon, optional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, amount) },
                enabled = name.isNotBlank(),
            ) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
