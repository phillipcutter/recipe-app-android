package com.example.recipetracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.recipetracker.model.GroceryFilter
import com.example.recipetracker.model.GroceryItem
import com.example.recipetracker.model.Ingredient
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter
import com.example.recipetracker.model.SampleRecipes
import com.example.recipetracker.model.matching
import com.example.recipetracker.model.plusIngredients
import com.example.recipetracker.model.plusItem
import com.example.recipetracker.model.toggling
import com.example.recipetracker.model.without
import com.example.recipetracker.model.withoutChecked

data class RecipeUiState(
    val recipes: List<Recipe> = SampleRecipes.all,
    val query: String = "",
    val filter: RecipeFilter = RecipeFilter.All,
    val groceryItems: List<GroceryItem> = emptyList(),
    val groceryFilter: GroceryFilter = GroceryFilter.All,
) {
    val visibleRecipes: List<Recipe> get() = recipes.matching(query, filter)
    val favoriteCount: Int get() = recipes.count { it.isFavorite }
    val averagePrep: Int get() = recipes.map { it.prepMinutes }.average().toInt()

    val visibleGroceryItems: List<GroceryItem> get() = groceryItems.matching(groceryFilter)
    val toBuyCount: Int get() = groceryItems.count { !it.isChecked }
    val inCartCount: Int get() = groceryItems.count { it.isChecked }
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

    fun setGroceryFilter(filter: GroceryFilter) { state = state.copy(groceryFilter = filter) }

    fun addGroceryItem(name: String, quantity: String) {
        state = state.copy(groceryItems = state.groceryItems.plusItem(name, quantity))
    }

    fun toggleGroceryItem(id: Long) {
        state = state.copy(groceryItems = state.groceryItems.toggling(id))
    }

    fun removeGroceryItem(id: Long) {
        state = state.copy(groceryItems = state.groceryItems.without(id))
    }

    fun clearCheckedGroceryItems() {
        state = state.copy(groceryItems = state.groceryItems.withoutChecked())
    }

    /**
     * Adds every ingredient of [recipe] to the grocery list, scaled by [multiplier] and
     * skipping anything already on the list. Returns how many items were actually added.
     */
    fun addRecipeToGroceryList(recipe: Recipe, multiplier: Double = 1.0): Int {
        val updated = state.groceryItems.plusIngredients(recipe, multiplier)
        val added = updated.size - state.groceryItems.size
        state = state.copy(groceryItems = updated)
        return added
    }
}
