package com.example.recipetracker.data

import android.content.Context
import com.example.recipetracker.model.Recipe

class LocalRecipeStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<Recipe> = RecipeJson.decode(prefs.getString(KEY_RECIPES, "") ?: "")

    fun save(recipes: List<Recipe>) {
        prefs.edit().putString(KEY_RECIPES, RecipeJson.encode(recipes.filter { it.isCustom })).apply()
    }

    private companion object {
        const val PREFS_NAME = "custom_recipes"
        const val KEY_RECIPES = "recipes"
    }
}
