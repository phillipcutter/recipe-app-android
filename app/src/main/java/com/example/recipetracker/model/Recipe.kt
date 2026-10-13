package com.example.recipetracker.model

data class Ingredient(
    val amount: Double,
    val unit: String,
    val name: String,
) {
    fun displayAmount(multiplier: Double = 1.0): String {
        val scaled = amount * multiplier
        val number = if (scaled % 1.0 == 0.0) scaled.toInt().toString()
        else "%.1f".format(scaled)
        return listOf(number, unit, name).filter { it.isNotBlank() }.joinToString(" ")
    }
}

data class Recipe(
    val id: Long,
    val name: String,
    val description: String,
    val prepMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val tags: Set<String>,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    val isFavorite: Boolean = false,
    val remixedFromId: Long? = null,
)

enum class RecipeFilter(val label: String) {
    All("All"),
    Favorites("Favorites"),
    Quick("Under 30 min"),
    Vegetarian("Vegetarian"),
    Remixes("Remixes"),
}

fun List<Recipe>.matching(query: String, filter: RecipeFilter): List<Recipe> {
    val needle = query.trim().lowercase()
    return filter { recipe ->
        val matchesQuery = needle.isEmpty() ||
            recipe.name.lowercase().contains(needle) ||
            recipe.ingredients.any { it.name.lowercase().contains(needle) } ||
            recipe.tags.any { it.lowercase().contains(needle) }
        val matchesFilter = when (filter) {
            RecipeFilter.All -> true
            RecipeFilter.Favorites -> recipe.isFavorite
            RecipeFilter.Quick -> recipe.prepMinutes <= 30
            RecipeFilter.Vegetarian -> "vegetarian" in recipe.tags.map { it.lowercase() }
            RecipeFilter.Remixes -> recipe.remixedFromId != null || "remix" in recipe.tags.map { it.lowercase() }
        }
        matchesQuery && matchesFilter
    }
}
