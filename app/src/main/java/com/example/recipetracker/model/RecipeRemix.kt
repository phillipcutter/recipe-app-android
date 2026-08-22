package com.example.recipetracker.model

enum class RemixStyle(val label: String, val tag: String) {
    Spicy("Spicy", "Spicy"),
    Citrus("Citrus", "Citrus"),
    Creamy("Creamy", "Creamy"),
    OnePan("One-pan", "One-pan"),
    HerbGarden("Herb garden", "Herby"),
}

data class RemixResult(
    val name: String,
    val description: String,
    val prepMinutes: Int,
    val difficulty: String,
    val tags: Set<String>,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
)

fun Recipe.remix(style: RemixStyle): RemixResult {
    val extraIngredients: List<Ingredient>
    val extraSteps: List<String>
    val minutesDelta: Int
    val newDifficulty: String
    val flavorNote: String

    when (style) {
        RemixStyle.Spicy -> {
            extraIngredients = listOf(
                Ingredient(1.0, "tsp", "chili flakes"),
                Ingredient(0.5, "tsp", "smoked paprika"),
            )
            extraSteps = listOf("Bloom chili flakes and paprika in a little oil before combining with the rest.")
            minutesDelta = 2
            newDifficulty = bumpDifficulty(difficulty)
            flavorNote = "A fiery take with chili heat in every bite."
        }
        RemixStyle.Citrus -> {
            extraIngredients = listOf(
                Ingredient(1.0, "", "lemon"),
                Ingredient(1.0, "tsp", "lemon zest"),
            )
            extraSteps = listOf("Finish with extra lemon zest and a squeeze of juice right before serving.")
            minutesDelta = 1
            newDifficulty = difficulty
            flavorNote = "Brighter and more lemon-forward than the original."
        }
        RemixStyle.Creamy -> {
            extraIngredients = listOf(
                Ingredient(0.25, "cup", "cream"),
                Ingredient(1.0, "tbsp", "butter"),
            )
            extraSteps = listOf("Stir in cream and butter off the heat until the sauce turns glossy.")
            minutesDelta = 3
            newDifficulty = difficulty
            flavorNote = "A richer, silkier version of the original."
        }
        RemixStyle.OnePan -> {
            extraIngredients = emptyList()
            extraSteps = listOf("Keep everything in one pan: cook aromatics, then the rest, and finish in the same vessel.")
            minutesDelta = -5
            newDifficulty = "Easy"
            flavorNote = "Streamlined so it all happens in a single pan."
        }
        RemixStyle.HerbGarden -> {
            extraIngredients = listOf(
                Ingredient(2.0, "tbsp", "chopped mixed herbs"),
                Ingredient(1.0, "handful", "fresh basil or parsley"),
            )
            extraSteps = listOf("Fold in a handful of fresh herbs at the end so they stay vivid.")
            minutesDelta = 2
            newDifficulty = difficulty
            flavorNote = "Loaded with extra herbs for a garden-fresh finish."
        }
    }

    val mergedIngredients = mergeIngredients(ingredients + extraIngredients)
    val mergedSteps = (steps + extraSteps).distinct()
    return RemixResult(
        name = "$name (${style.label} remix)",
        description = "$description $flavorNote",
        prepMinutes = (prepMinutes + minutesDelta).coerceIn(5, 180),
        difficulty = newDifficulty,
        tags = tags + style.tag + "Remix",
        ingredients = mergedIngredients,
        steps = mergedSteps,
    )
}

internal fun bumpDifficulty(current: String): String = when (current.lowercase()) {
    "easy" -> "Medium"
    "medium" -> "Hard"
    else -> current
}

internal fun mergeIngredients(ingredients: List<Ingredient>): List<Ingredient> {
    val merged = linkedMapOf<String, Ingredient>()
    for (ingredient in ingredients) {
        val key = "${ingredient.unit.lowercase()}|${ingredient.name.lowercase()}"
        val existing = merged[key]
        merged[key] = if (existing == null) ingredient
        else existing.copy(amount = existing.amount + ingredient.amount)
    }
    return merged.values.toList()
}
