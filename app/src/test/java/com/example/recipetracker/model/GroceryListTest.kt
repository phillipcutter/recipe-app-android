package com.example.recipetracker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryListTest {
    private val pasta = SampleRecipes.all.first { it.name == "Lemon Herb Pasta" }
    private val empty = emptyList<GroceryItem>()

    @Test
    fun `adds items with trimmed names and incrementing ids`() {
        val list = empty.plusItem("  Milk  ", "2 L").plusItem("Eggs")

        assertEquals(listOf("Milk", "Eggs"), list.map { it.name })
        assertEquals(listOf(1L, 2L), list.map { it.id })
        assertEquals("2 L Milk", list.first().label)
        assertEquals("Eggs", list.last().label)
    }

    @Test
    fun `ignores blank names and duplicates regardless of case`() {
        val list = empty.plusItem("Milk").plusItem("   ").plusItem("milk")

        assertEquals(1, list.size)
        assertTrue(list.hasItemNamed("MILK"))
    }

    @Test
    fun `adds recipe ingredients scaled to the chosen servings`() {
        val list = empty.plusIngredients(pasta, multiplier = 2.0)

        assertEquals(pasta.ingredients.size, list.size)
        assertTrue(list.all { it.recipeName == "Lemon Herb Pasta" })
        assertEquals("400 g spaghetti", list.first { it.name == "spaghetti" }.label)
        assertEquals("2 lemon", list.first { it.name == "lemon" }.label)
    }

    @Test
    fun `adding the same recipe twice does not duplicate ingredients`() {
        val once = empty.plusIngredients(pasta)
        val twice = once.plusIngredients(pasta)

        assertEquals(once.size, twice.size)
    }

    @Test
    fun `checking an item moves it between the to-buy and in-cart filters`() {
        val list = empty.plusItem("Milk").plusItem("Eggs")
        val toggled = list.toggling(list.first().id)

        assertEquals(listOf("Eggs"), toggled.matching(GroceryFilter.ToBuy).map { it.name })
        assertEquals(listOf("Milk"), toggled.matching(GroceryFilter.InCart).map { it.name })
        assertEquals(2, toggled.matching(GroceryFilter.All).size)
        assertTrue(toggled.toggling(list.first().id).none { it.isChecked })
    }

    @Test
    fun `removes a single item and clears everything in the cart`() {
        val list = empty.plusItem("Milk").plusItem("Eggs").plusItem("Bread")
        val checked = list.toggling(list[0].id).toggling(list[2].id)

        assertEquals(listOf("Eggs"), checked.withoutChecked().map { it.name })
        assertFalse(list.without(list[1].id).hasItemNamed("Eggs"))
        assertEquals(3, list.size)
    }
}
