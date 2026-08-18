package com.example.recipetracker

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.recipetracker.data.LocalRecipeStore
import com.example.recipetracker.model.CustomRecipeDraft
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipeFilter
import com.example.recipetracker.model.SampleRecipes
import com.example.recipetracker.model.matching

data class RecipeUiState(
    val recipes: List<Recipe> = SampleRecipes.all,
    val query: String = "",
    val filter: RecipeFilter = RecipeFilter.All,
) {
    val visibleRecipes: List<Recipe> get() = recipes.matching(query, filter)
    val favoriteCount: Int get() = recipes.count { it.isFavorite }
    val averagePrep: Int get() = recipes.map { it.prepMinutes }.average().toInt()
}

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalRecipeStore(application)

    var state by mutableStateOf(RecipeUiState())
        private set

    init {
        val custom = store.load()
        if (custom.isNotEmpty()) {
            state = state.copy(recipes = custom + SampleRecipes.all)
        }
    }

    fun setQuery(query: String) { state = state.copy(query = query) }
    fun setFilter(filter: RecipeFilter) { state = state.copy(filter = filter) }

    fun toggleFavorite(id: Long) {
        state = state.copy(recipes = state.recipes.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        })
        persistCustomRecipes()
    }

    fun addCustomRecipe(draft: CustomRecipeDraft): Boolean {
        val nextId = (state.recipes.maxOfOrNull { it.id } ?: 0) + 1
        val recipe = draft.toRecipe(nextId) ?: return false
        state = state.copy(recipes = listOf(recipe) + state.recipes)
        persistCustomRecipes()
        return true
    }

    private fun persistCustomRecipes() {
        store.save(state.recipes)
    }
}
