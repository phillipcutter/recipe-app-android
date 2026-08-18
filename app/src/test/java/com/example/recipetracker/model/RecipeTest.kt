package com.example.recipetracker.model

import com.example.recipetracker.data.RecipeJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTest {
    @Test
    fun `search matches names ingredients tags and plans`() {
        val recipes = SampleRecipes.all

        assertEquals(listOf("Ginger Miso Salmon"), recipes.matching("ginger", RecipeFilter.All).map { it.name })
        assertTrue(recipes.matching("vegetarian", RecipeFilter.All).size >= 2)
        assertEquals(listOf("Lemon Herb Pasta"), recipes.matching("pasta", RecipeFilter.All).map { it.name })
        assertEquals(listOf("Ginger Miso Salmon"), recipes.matching("glaze", RecipeFilter.All).map { it.name })
    }

    @Test
    fun `filters favorites custom and quick recipes`() {
        val recipes = SampleRecipes.all
        val custom = recipes.first().copy(id = 99, isCustom = true)

        assertTrue(recipes.matching("", RecipeFilter.Favorites).all { it.isFavorite })
        assertTrue(recipes.matching("", RecipeFilter.Quick).all { it.prepMinutes <= 30 })
        assertEquals(listOf("Lemon Herb Pasta"), (listOf(custom) + recipes).matching("", RecipeFilter.Custom).map { it.name })
    }

    @Test
    fun `ingredient amount scales with servings`() {
        val ingredient = Ingredient(1.5, "cups", "flour")

        assertEquals("3 cups flour", ingredient.displayAmount(2.0))
        assertEquals("0.8 cups flour", ingredient.displayAmount(0.5))
    }

    @Test
    fun `ingredients and steps flatten from plans`() {
        val recipe = SampleRecipes.all.first()

        assertTrue(recipe.ingredients.any { it.name == "spaghetti" })
        assertTrue(recipe.steps.any { it.contains("al dente") })
        assertEquals(2, recipe.plans.size)
    }

    @Test
    fun `custom recipe draft requires name and a named plan`() {
        val valid = CustomRecipeDraft(
            name = "Braised short ribs",
            description = "Weekend project.",
            prepMinutes = 180,
            servings = 6,
            difficulty = "Hard",
            tags = setOf("Beef"),
            plans = listOf(
                RecipePlan(
                    id = 1,
                    name = "Sear",
                    ingredients = listOf(Ingredient(2.0, "kg", "short ribs")),
                    steps = listOf("Brown on all sides."),
                ),
                RecipePlan(
                    id = 2,
                    name = "Braise",
                    steps = listOf("Cook low and slow for three hours."),
                ),
            ),
        ).toRecipe(10)

        assertNotNull(valid)
        assertTrue(valid!!.isCustom)
        assertEquals(2, valid.plans.size)

        val invalid = CustomRecipeDraft(
            name = " ",
            description = "",
            prepMinutes = 20,
            servings = 2,
            difficulty = "Easy",
            tags = emptySet(),
            plans = listOf(RecipePlan(id = 1, name = "Prep")),
        ).toRecipe(11)

        assertNull(invalid)
    }

    @Test
    fun `custom recipes round trip through json`() {
        val recipe = CustomRecipeDraft(
            name = "Layered lasagna",
            description = "Sunday sauce and noodles.",
            prepMinutes = 90,
            servings = 8,
            difficulty = "Medium",
            tags = setOf("Italian", "Make-ahead"),
            plans = listOf(
                RecipePlan(
                    id = 1,
                    name = "Sauce",
                    notes = "Can be made a day ahead.",
                    ingredients = listOf(Ingredient(800.0, "g", "crushed tomatoes")),
                    steps = listOf("Simmer until thick."),
                ),
            ),
        ).toRecipe(42)!!

        val decoded = RecipeJson.decode(RecipeJson.encode(listOf(recipe))).single()
        assertEquals(recipe, decoded)
    }
}
