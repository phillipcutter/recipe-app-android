package com.example.recipetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.example.recipetracker.RecipeViewModel
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter
import com.example.recipetracker.model.RemixStyle

@Composable
fun RecipeApp(viewModel: RecipeViewModel = viewModel()) {
    val state = viewModel.state
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var remixSource by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", fontSize = 26.sp)
            }
        },
    ) { padding ->
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
                        androidx.compose.material3.FilterChip(
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
                        onClick = { selectedRecipe = recipe },
                        onFavorite = { viewModel.toggleFavorite(recipe.id) },
                    )
                }
            }
        }
    }

    selectedRecipe?.let { recipe ->
        if (remixSource == null) {
            RecipeDetailDialog(
                recipe = recipe,
                originalName = state.recipes.firstOrNull { it.id == recipe.remixedFromId }?.name,
                onDismiss = { selectedRecipe = null },
                onRemix = { remixSource = recipe },
            )
        }
    }
    remixSource?.let { source ->
        RemixRecipeDialog(
            recipe = source,
            onDismiss = { remixSource = null },
            onRemix = { style ->
                val created = viewModel.remixRecipe(source.id, style)
                remixSource = null
                selectedRecipe = created
            },
        )
    }
    if (showAddDialog) {
        AddRecipeDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, minutes, tag ->
                viewModel.addRecipe(name, minutes, tag)
                showAddDialog = false
            },
        )
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
            if (recipe.remixedFromId != null) {
                Text(
                    "Remix of another recipe",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
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
    originalName: String?,
    onDismiss: () -> Unit,
    onRemix: () -> Unit,
) {
    var servings by remember(recipe.id) { mutableIntStateOf(recipe.servings) }
    val multiplier = servings.toDouble() / recipe.servings
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recipe.name) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text(recipe.description, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                if (originalName != null) {
                    item {
                        Text(
                            "Remixed from $originalName",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
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
        dismissButton = { TextButton(onClick = onRemix) { Text("Remix") } },
    )
}

@Composable
private fun RemixRecipeDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onRemix: (RemixStyle) -> Unit,
) {
    var selected by remember { mutableStateOf(RemixStyle.Spicy) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remix ${recipe.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Spin this recipe into a new variation. Ingredients and steps update to match the style.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RemixStyle.entries.forEach { style ->
                    androidx.compose.material3.FilterChip(
                        selected = selected == style,
                        onClick = { selected = style },
                        label = { Text(style.label) },
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onRemix(selected) }) { Text("Save remix") }
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
