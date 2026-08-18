package com.example.recipetracker.data

import com.example.recipetracker.model.Ingredient
import com.example.recipetracker.model.Recipe
import com.example.recipetracker.model.RecipePlan
import org.json.JSONArray
import org.json.JSONObject

object RecipeJson {
    fun encode(recipes: List<Recipe>): String {
        val array = JSONArray()
        recipes.forEach { array.put(encodeRecipe(it)) }
        return array.toString()
    }

    fun decode(text: String): List<Recipe> {
        if (text.isBlank()) return emptyList()
        val array = JSONArray(text)
        return buildList {
            for (index in 0 until array.length()) {
                add(decodeRecipe(array.getJSONObject(index)))
            }
        }
    }

    private fun encodeRecipe(recipe: Recipe): JSONObject = JSONObject().apply {
        put("id", recipe.id)
        put("name", recipe.name)
        put("description", recipe.description)
        put("prepMinutes", recipe.prepMinutes)
        put("servings", recipe.servings)
        put("difficulty", recipe.difficulty)
        put("isFavorite", recipe.isFavorite)
        put("isCustom", recipe.isCustom)
        put("tags", JSONArray(recipe.tags.toList()))
        val plans = JSONArray()
        recipe.plans.forEach { plans.put(encodePlan(it)) }
        put("plans", plans)
    }

    private fun encodePlan(plan: RecipePlan): JSONObject = JSONObject().apply {
        put("id", plan.id)
        put("name", plan.name)
        put("notes", plan.notes)
        val ingredients = JSONArray()
        plan.ingredients.forEach { ingredient ->
            ingredients.put(
                JSONObject().apply {
                    put("amount", ingredient.amount)
                    put("unit", ingredient.unit)
                    put("name", ingredient.name)
                },
            )
        }
        put("ingredients", ingredients)
        put("steps", JSONArray(plan.steps))
    }

    private fun decodeRecipe(json: JSONObject): Recipe {
        val tags = json.optJSONArray("tags").toStringList().toSet()
        val plansJson = json.optJSONArray("plans") ?: JSONArray()
        val plans = buildList {
            for (index in 0 until plansJson.length()) {
                add(decodePlan(plansJson.getJSONObject(index)))
            }
        }
        return Recipe(
            id = json.getLong("id"),
            name = json.getString("name"),
            description = json.optString("description"),
            prepMinutes = json.optInt("prepMinutes", 30),
            servings = json.optInt("servings", 2),
            difficulty = json.optString("difficulty", "Medium"),
            tags = tags,
            plans = plans,
            isFavorite = json.optBoolean("isFavorite"),
            isCustom = json.optBoolean("isCustom", true),
        )
    }

    private fun decodePlan(json: JSONObject): RecipePlan {
        val ingredientsJson = json.optJSONArray("ingredients") ?: JSONArray()
        val ingredients = buildList {
            for (index in 0 until ingredientsJson.length()) {
                val item = ingredientsJson.getJSONObject(index)
                add(
                    Ingredient(
                        amount = item.optDouble("amount", 1.0),
                        unit = item.optString("unit"),
                        name = item.optString("name"),
                    ),
                )
            }
        }
        return RecipePlan(
            id = json.optLong("id"),
            name = json.optString("name"),
            notes = json.optString("notes"),
            ingredients = ingredients,
            steps = json.optJSONArray("steps").toStringList(),
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) add(optString(index))
        }
    }
}
