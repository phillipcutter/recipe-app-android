package com.example.recipetracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.recipetracker.model.GroceryItem
import com.example.recipetracker.model.Ingredient
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter
import com.example.recipetracker.model.SampleRecipes
import com.example.recipetracker.model.matching

data class RecipeUiState(
    val recipes: List<Recipe> = SampleRecipes.all,
    val query: String = "",
    val filter: RecipeFilter = RecipeFilter.All,
    val groceryItems: List<GroceryItem> = emptyList(),
) {
    val visibleRecipes: List<Recipe> get() = recipes.matching(query, filter)
    val favoriteCount: Int get() = recipes.count { it.isFavorite }
    val averagePrep: Int get() = recipes.map { it.prepMinutes }.average().toInt()
    val checkedGroceryCount: Int get() = groceryItems.count { it.isChecked }
}

class RecipeViewModel : ViewModel() {
    var state by mutableStateOf(RecipeUiState())
        private set

    fun setQuery(query: String) { state = state.copy(query = query) }
    fun setFilter(filter: RecipeFilter) { state = state.copy(filter = filter) }

    fun toggleFavorite(id: Long) {
        state = state.copy(recipes = state.recipes.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        })
    }

    fun addRecipe(name: String, minutes: Int, tag: String) {
        val cleanedName = name.trim()
        if (cleanedName.isEmpty()) return
        val recipe = Recipe(
            id = (state.recipes.maxOfOrNull { it.id } ?: 0) + 1,
            name = cleanedName,
            description = "A recipe you added to your collection.",
            prepMinutes = minutes.coerceIn(1, 999),
            servings = 2,
            difficulty = "Easy",
            tags = setOf(tag.trim().ifEmpty { "Homemade" }),
            ingredients = listOf(Ingredient(1.0, "", "Add your ingredients")),
            steps = listOf("Add preparation steps for this recipe."),
        )
        state = state.copy(recipes = listOf(recipe) + state.recipes)
    }

    fun addGroceryItem(name: String, amountAndUnit: String = "") {
        val cleanedName = name.trim()
        if (cleanedName.isEmpty()) return
        val nextId = (state.groceryItems.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = GroceryItem(
            id = nextId,
            name = cleanedName,
            amountAndUnit = amountAndUnit.trim(),
            isChecked = false,
        )
        state = state.copy(groceryItems = state.groceryItems + newItem)
    }

    fun toggleGroceryItem(id: Long) {
        state = state.copy(
            groceryItems = state.groceryItems.map {
                if (it.id == id) it.copy(isChecked = !it.isChecked) else it
            }
        )
    }

    fun deleteGroceryItem(id: Long) {
        state = state.copy(
            groceryItems = state.groceryItems.filterNot { it.id == id }
        )
    }

    fun clearCompletedGroceryItems() {
        state = state.copy(
            groceryItems = state.groceryItems.filterNot { it.isChecked }
        )
    }

    fun addRecipeIngredientsToGrocery(recipe: Recipe, multiplier: Double = 1.0) {
        var nextId = (state.groceryItems.maxOfOrNull { it.id } ?: 0) + 1
        val newItems = recipe.ingredients.map { ingredient ->
            val amountStr = ingredient.displayAmount(multiplier).substringBefore(" ${ingredient.name}").trim()
            GroceryItem(
                id = nextId++,
                name = ingredient.name,
                amountAndUnit = if (amountStr != ingredient.name) amountStr else "",
                isChecked = false,
            )
        }
        state = state.copy(groceryItems = state.groceryItems + newItems)
    }
}
