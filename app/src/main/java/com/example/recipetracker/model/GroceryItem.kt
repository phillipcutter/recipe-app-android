package com.example.recipetracker.model

data class GroceryItem(
    val id: Long,
    val name: String,
    val quantity: String = "",
    val isChecked: Boolean = false,
    val recipeName: String? = null,
) {
    /** "2 L milk", or just "milk" when no quantity was given. */
    val label: String
        get() = listOf(quantity, name).filter { it.isNotBlank() }.joinToString(" ")
}

enum class GroceryFilter(val label: String) {
    All("All"),
    ToBuy("To buy"),
    InCart("In cart"),
}

fun List<GroceryItem>.matching(filter: GroceryFilter): List<GroceryItem> = when (filter) {
    GroceryFilter.All -> this
    GroceryFilter.ToBuy -> this.filterNot { it.isChecked }
    GroceryFilter.InCart -> this.filter { it.isChecked }
}

fun List<GroceryItem>.nextId(): Long = (maxOfOrNull { it.id } ?: 0L) + 1L

fun List<GroceryItem>.hasItemNamed(name: String): Boolean {
    val needle = name.trim()
    return any { it.name.equals(needle, ignoreCase = true) }
}

/** Appends an item, ignoring blank names and anything already on the list. */
fun List<GroceryItem>.plusItem(
    name: String,
    quantity: String = "",
    recipeName: String? = null,
): List<GroceryItem> {
    val cleanedName = name.trim()
    if (cleanedName.isEmpty() || hasItemNamed(cleanedName)) return this
    return this + GroceryItem(
        id = nextId(),
        name = cleanedName,
        quantity = quantity.trim(),
        recipeName = recipeName,
    )
}

/** Appends every ingredient of [recipe], scaled by [multiplier], skipping duplicates. */
fun List<GroceryItem>.plusIngredients(recipe: Recipe, multiplier: Double = 1.0): List<GroceryItem> =
    recipe.ingredients.fold(this) { items, ingredient ->
        items.plusItem(
            name = ingredient.name,
            quantity = ingredient.displayQuantity(multiplier),
            recipeName = recipe.name,
        )
    }

fun List<GroceryItem>.toggling(id: Long): List<GroceryItem> =
    map { if (it.id == id) it.copy(isChecked = !it.isChecked) else it }

fun List<GroceryItem>.without(id: Long): List<GroceryItem> = filterNot { it.id == id }

fun List<GroceryItem>.withoutChecked(): List<GroceryItem> = filterNot { it.isChecked }
