package com.example.recipetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.recipetracker.model.CustomRecipeDraft
import com.example.recipetracker.model.Ingredient
import com.example.recipetracker.model.RecipePlan

private val Difficulties = listOf("Easy", "Medium", "Hard")

private data class IngredientDraft(
    var amount: String = "1",
    var unit: String = "",
    var name: String = "",
)

private data class PlanDraft(
    var name: String,
    var notes: String = "",
    val ingredients: MutableList<IngredientDraft> = mutableStateListOf(IngredientDraft()),
    val steps: MutableList<String> = mutableStateListOf(""),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    onDismiss: () -> Unit,
    onSave: (CustomRecipeDraft) -> Boolean,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("45") }
    var servings by remember { mutableStateOf("4") }
    var difficulty by remember { mutableStateOf("Medium") }
    var tags by remember { mutableStateOf("Homemade") }
    val plans = remember {
        mutableStateListOf(
            PlanDraft(name = "Prep"),
            PlanDraft(name = "Cook"),
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            topBar = {
                TopAppBar(
                    title = { Text("New complex recipe") },
                    navigationIcon = { TextButton(onClick = onDismiss) { Text("Close") } },
                    actions = {
                        Button(
                            onClick = {
                                val draft = CustomRecipeDraft(
                                    name = name,
                                    description = description,
                                    prepMinutes = minutes.toIntOrNull() ?: 45,
                                    servings = servings.toIntOrNull() ?: 4,
                                    difficulty = difficulty,
                                    tags = tags.split(",", " ").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
                                    plans = plans.mapIndexed { index, plan -> plan.toRecipePlan(index.toLong() + 1) },
                                )
                                if (!onSave(draft)) {
                                    error = "Add a name and at least one named plan."
                                }
                            },
                        ) { Text("Save locally") }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Text(
                        "Build a multi-plan recipe on this device. Each plan can have its own ingredients and steps.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    OutlinedTextField(
                        name,
                        { name = it; error = null },
                        label = { Text("Recipe name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        description,
                        { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            minutes,
                            { minutes = it.filter(Char::isDigit) },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            servings,
                            { servings = it.filter(Char::isDigit) },
                            label = { Text("Servings") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Difficulties.size) { index ->
                            val value = Difficulties[index]
                            FilterChip(
                                selected = difficulty == value,
                                onClick = { difficulty = value },
                                label = { Text(value) },
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        tags,
                        { tags = it },
                        label = { Text("Tags (comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Plans", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { plans.add(PlanDraft(name = "Plan ${plans.size + 1}")) }) {
                            Text("Add plan")
                        }
                    }
                }
                itemsIndexed(plans, key = { index, _ -> index }) { index, plan ->
                    PlanEditor(
                        plan = plan,
                        onRemove = { if (plans.size > 1) plans.removeAt(index) },
                        canRemove = plans.size > 1,
                    )
                }
                if (error != null) {
                    item {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanEditor(plan: PlanDraft, onRemove: () -> Unit, canRemove: Boolean) {
    var name by remember { mutableStateOf(plan.name) }
    var notes by remember { mutableStateOf(plan.notes) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (canRemove) TextButton(onClick = onRemove) { Text("Remove") }
            }
            OutlinedTextField(
                name,
                {
                    name = it
                    plan.name = it
                },
                label = { Text("Plan name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                notes,
                {
                    notes = it
                    plan.notes = it
                },
                label = { Text("Plan notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Ingredients", fontWeight = FontWeight.SemiBold)
            plan.ingredients.forEachIndexed { index, ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onChange = { plan.ingredients[index] = it },
                )
            }
            OutlinedButton(onClick = { plan.ingredients.add(IngredientDraft()) }) { Text("Add ingredient") }
            Text("Steps", fontWeight = FontWeight.SemiBold)
            plan.steps.forEachIndexed { index, step ->
                OutlinedTextField(
                    step,
                    { plan.steps[index] = it },
                    label = { Text("Step ${index + 1}") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
            OutlinedButton(onClick = { plan.steps.add("") }) { Text("Add step") }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: IngredientDraft, onChange: (IngredientDraft) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            ingredient.amount,
            { onChange(ingredient.copy(amount = it)) },
            label = { Text("Amt") },
            singleLine = true,
            modifier = Modifier.weight(0.8f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        OutlinedTextField(
            ingredient.unit,
            { onChange(ingredient.copy(unit = it)) },
            label = { Text("Unit") },
            singleLine = true,
            modifier = Modifier.weight(0.8f),
        )
        OutlinedTextField(
            ingredient.name,
            { onChange(ingredient.copy(name = it)) },
            label = { Text("Ingredient") },
            singleLine = true,
            modifier = Modifier.weight(1.4f),
        )
    }
}

private fun PlanDraft.toRecipePlan(id: Long): RecipePlan = RecipePlan(
    id = id,
    name = name,
    notes = notes,
    ingredients = ingredients.mapNotNull { draft ->
        val ingredientName = draft.name.trim()
        if (ingredientName.isEmpty()) null
        else Ingredient(draft.amount.toDoubleOrNull() ?: 1.0, draft.unit.trim(), ingredientName)
    },
    steps = steps,
)
