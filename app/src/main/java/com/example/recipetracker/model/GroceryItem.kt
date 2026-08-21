package com.example.recipetracker.model

data class GroceryItem(
    val id: Long,
    val name: String,
    val amountAndUnit: String = "",
    val isChecked: Boolean = false,
) {
    val displayTitle: String get() = if (amountAndUnit.isNotBlank()) "$name ($amountAndUnit)" else name
}
