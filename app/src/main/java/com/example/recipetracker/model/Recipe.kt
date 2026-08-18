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

data class RecipePlan(
    val id: Long,
    val name: String,
    val notes: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList(),
)

data class Recipe(
    val id: Long,
    val name: String,
    val description: String,
    val prepMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val tags: Set<String>,
    val plans: List<RecipePlan>,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
) {
    val ingredients: List<Ingredient> get() = plans.flatMap { it.ingredients }
    val steps: List<String> get() = plans.flatMap { it.steps }
}

enum class RecipeFilter(val label: String) {
    All("All"),
    Favorites("Favorites"),
    Custom("My recipes"),
    Quick("Under 30 min"),
    Vegetarian("Vegetarian"),
}

fun List<Recipe>.matching(query: String, filter: RecipeFilter): List<Recipe> {
    val needle = query.trim().lowercase()
    return filter { recipe ->
        val matchesQuery = needle.isEmpty() ||
            recipe.name.lowercase().contains(needle) ||
            recipe.description.lowercase().contains(needle) ||
            recipe.ingredients.any { it.name.lowercase().contains(needle) } ||
            recipe.plans.any { it.name.lowercase().contains(needle) || it.notes.lowercase().contains(needle) } ||
            recipe.tags.any { it.lowercase().contains(needle) }
        val matchesFilter = when (filter) {
            RecipeFilter.All -> true
            RecipeFilter.Favorites -> recipe.isFavorite
            RecipeFilter.Custom -> recipe.isCustom
            RecipeFilter.Quick -> recipe.prepMinutes <= 30
            RecipeFilter.Vegetarian -> "vegetarian" in recipe.tags.map { it.lowercase() }
        }
        matchesQuery && matchesFilter
    }
}

data class CustomRecipeDraft(
    val name: String,
    val description: String,
    val prepMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val tags: Set<String>,
    val plans: List<RecipePlan>,
) {
    fun toRecipe(id: Long): Recipe? {
        val cleanedName = name.trim()
        val cleanedPlans = plans.mapNotNull { plan ->
            val planName = plan.name.trim()
            if (planName.isEmpty()) return@mapNotNull null
            val ingredients = plan.ingredients.filter { it.name.isNotBlank() }
            val steps = plan.steps.map { it.trim() }.filter { it.isNotEmpty() }
            RecipePlan(
                id = plan.id,
                name = planName,
                notes = plan.notes.trim(),
                ingredients = ingredients,
                steps = steps,
            )
        }
        if (cleanedName.isEmpty() || cleanedPlans.isEmpty()) return null
        return Recipe(
            id = id,
            name = cleanedName,
            description = description.trim().ifEmpty { "A custom recipe saved on this device." },
            prepMinutes = prepMinutes.coerceIn(1, 999),
            servings = servings.coerceIn(1, 99),
            difficulty = difficulty.ifBlank { "Medium" },
            tags = tags.map { it.trim() }.filter { it.isNotEmpty() }.toSet().ifEmpty { setOf("Homemade") },
            plans = cleanedPlans,
            isCustom = true,
        )
    }
}
