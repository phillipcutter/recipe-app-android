package com.example.recipetracker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryItemTest {
    @Test
    fun `adding a grocery item prepends and trims the name`() {
        val items = emptyList<GroceryItem>().adding("  milk  ", "  1 gal ")

        assertEquals(1, items.size)
        assertEquals("milk", items[0].name)
        assertEquals("1 gal", items[0].note)
        assertFalse(items[0].checked)
    }

    @Test
    fun `blank names are ignored`() {
        assertTrue(emptyList<GroceryItem>().adding("   ").isEmpty())
    }

    @Test
    fun `toggle check and clear completed`() {
        val items = listOf(
            GroceryItem(1, "apples"),
            GroceryItem(2, "bread", checked = true),
        ).toggling(1)

        assertTrue(items.first { it.id == 1L }.checked)
        assertTrue(items.clearingChecked().isEmpty())
        assertEquals(0, items.uncheckedCount())
    }

    @Test
    fun `adding ingredients skips duplicates that are still on the list`() {
        val pasta = Ingredient(200.0, "g", "spaghetti")
        val lemon = Ingredient(1.0, "", "lemon")
        val items = SampleGroceries.starter.addingIngredients(listOf(pasta, lemon))

        assertEquals(1, items.count { it.name.equals("spaghetti", ignoreCase = true) })
        assertTrue(items.any { it.name.equals("lemon", ignoreCase = true) })
    }
}
