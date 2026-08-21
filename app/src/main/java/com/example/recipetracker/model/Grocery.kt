package com.example.recipetracker.model

data class GroceryItem(
    val id: Long,
    val name: String,
    val amount: Double?,
    val unit: String,
    val recipeName: String? = null,
    val isPurchased: Boolean = false,
) {
    val label: String
        get() {
            if (amount == null) return name
            val number = if (amount % 1.0 == 0.0) amount.toInt().toString() else "%.1f".format(amount)
            return listOf(number, unit, name).filter { it.isNotBlank() }.joinToString(" ")
        }

    /** Two entries stack when they name the same thing in the same unit. */
    fun stacksWith(other: GroceryItem): Boolean =
        name.equals(other.name, ignoreCase = true) && unit.equals(other.unit, ignoreCase = true)
}

private val KNOWN_UNITS = setOf(
    "g", "kg", "mg", "ml", "l", "oz", "lb", "lbs",
    "cup", "cups", "tbsp", "tsp", "clove", "cloves",
    "can", "cans", "pack", "packs", "bunch", "bunches", "slice", "slices",
)

/**
 * Turns a single line of free text into a grocery entry, so the quick-add field
 * can stay one box: "2 cups flour" splits, while "olive oil" stays as written.
 */
fun parseGroceryInput(text: String): GroceryItem? {
    val tokens = text.trim().split(" ").filter { it.isNotBlank() }
    if (tokens.isEmpty()) return null

    val amount = tokens.first().replace(',', '.').toDoubleOrNull()
    if (amount == null || amount <= 0.0 || tokens.size == 1) {
        return GroceryItem(id = 0, name = tokens.joinToString(" "), amount = null, unit = "")
    }

    val maybeUnit = tokens[1]
    val hasUnit = maybeUnit.lowercase() in KNOWN_UNITS && tokens.size > 2
    val name = tokens.drop(if (hasUnit) 2 else 1).joinToString(" ")
    return GroceryItem(id = 0, name = name, amount = amount, unit = if (hasUnit) maybeUnit else "")
}

/**
 * Adds an entry to the list, stacking amounts onto a matching line instead of
 * repeating it. A stacked line becomes unpurchased again — there is more to buy.
 */
fun List<GroceryItem>.plusItem(item: GroceryItem): List<GroceryItem> {
    val existing = firstOrNull { it.stacksWith(item) }
    if (existing == null) {
        return this + item.copy(id = nextGroceryId())
    }
    val stackedAmount = when {
        existing.amount == null || item.amount == null -> existing.amount
        else -> existing.amount + item.amount
    }
    return map {
        if (it.id == existing.id) {
            it.copy(
                amount = stackedAmount,
                recipeName = existing.recipeName ?: item.recipeName,
                isPurchased = false,
            )
        } else {
            it
        }
    }
}

/** Adds every ingredient of a recipe, scaled to the servings the cook picked. */
fun List<GroceryItem>.plusRecipe(recipe: Recipe, servings: Int = recipe.servings): List<GroceryItem> {
    val multiplier = servings.toDouble() / recipe.servings
    return recipe.ingredients.fold(this) { list, ingredient ->
        list.plusItem(
            GroceryItem(
                id = 0,
                name = ingredient.name,
                amount = ingredient.amount * multiplier,
                unit = ingredient.unit,
                recipeName = recipe.name,
            )
        )
    }
}

fun List<GroceryItem>.nextGroceryId(): Long = (maxOfOrNull { it.id } ?: 0L) + 1L

fun List<GroceryItem>.toBuy(): List<GroceryItem> = filter { !it.isPurchased }

fun List<GroceryItem>.purchased(): List<GroceryItem> = filter { it.isPurchased }
