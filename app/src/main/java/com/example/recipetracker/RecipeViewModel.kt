package com.example.recipetracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.recipetracker.model.Ingredient
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter
import com.example.recipetracker.model.RemixStyle
import com.example.recipetracker.model.SampleRecipes
import com.example.recipetracker.model.matching
import com.example.recipetracker.model.remix

data class RecipeUiState(
    val recipes: List<Recipe> = SampleRecipes.all,
    val query: String = "",
    val filter: RecipeFilter = RecipeFilter.All,
) {
    val visibleRecipes: List<Recipe> get() = recipes.matching(query, filter)
    val favoriteCount: Int get() = recipes.count { it.isFavorite }
    val averagePrep: Int get() = recipes.map { it.prepMinutes }.average().toInt()
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

    fun remixRecipe(id: Long, style: RemixStyle): Recipe? {
        val source = state.recipes.firstOrNull { it.id == id } ?: return null
        val remixed = source.remix(style)
        val recipe = Recipe(
            id = (state.recipes.maxOfOrNull { it.id } ?: 0) + 1,
            name = remixed.name,
            description = remixed.description,
            prepMinutes = remixed.prepMinutes,
            servings = source.servings,
            difficulty = remixed.difficulty,
            tags = remixed.tags,
            ingredients = remixed.ingredients,
            steps = remixed.steps,
            remixedFromId = source.id,
        )
        state = state.copy(recipes = listOf(recipe) + state.recipes)
        return recipe
    }
}
