package com.example.recipetracker.model

data class GroceryItem(
    val id: Long,
    val name: String,
    val note: String = "",
    val checked: Boolean = false,
)

fun List<GroceryItem>.uncheckedCount(): Int = count { !it.checked }

fun List<GroceryItem>.adding(
    name: String,
    note: String = "",
): List<GroceryItem> {
    val cleanedName = name.trim()
    if (cleanedName.isEmpty()) return this
    val nextId = (maxOfOrNull { it.id } ?: 0) + 1
    return listOf(
        GroceryItem(id = nextId, name = cleanedName, note = note.trim()),
    ) + this
}

fun List<GroceryItem>.toggling(id: Long): List<GroceryItem> =
    map { if (it.id == id) it.copy(checked = !it.checked) else it }

fun List<GroceryItem>.removing(id: Long): List<GroceryItem> = filterNot { it.id == id }

fun List<GroceryItem>.clearingChecked(): List<GroceryItem> = filterNot { it.checked }

fun List<GroceryItem>.addingIngredients(ingredients: List<Ingredient>): List<GroceryItem> {
    var items = this
    ingredients.forEach { ingredient ->
        val alreadyListed = items.any {
            !it.checked && it.name.equals(ingredient.name, ignoreCase = true)
        }
        if (!alreadyListed) {
            items = items.adding(
                name = ingredient.name,
                note = ingredient.displayAmount(),
            )
        }
    }
    return items
}

object SampleGroceries {
    val starter = listOf(
        GroceryItem(id = 1, name = "spaghetti", note = "200 g"),
        GroceryItem(id = 2, name = "lemons", note = "2"),
        GroceryItem(id = 3, name = "olive oil", note = "if running low"),
        GroceryItem(id = 4, name = "chickpeas", note = "2 cans", checked = true),
    )
}
