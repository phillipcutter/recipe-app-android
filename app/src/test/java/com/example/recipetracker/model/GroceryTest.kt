package com.example.recipetracker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryTest {
    private val emptyList = emptyList<GroceryItem>()

    @Test
    fun `parses an amount and unit out of free text`() {
        val item = parseGroceryInput("2 cups flour")!!

        assertEquals(2.0, item.amount!!, 0.001)
        assertEquals("cups", item.unit)
        assertEquals("flour", item.name)
        assertEquals("2 cups flour", item.label)
    }

    @Test
    fun `keeps plain text as a name without an amount`() {
        val item = parseGroceryInput("olive oil")!!

        assertNull(item.amount)
        assertEquals("olive oil", item.name)
        assertEquals("olive oil", item.label)
    }

    @Test
    fun `treats an unrecognised second word as part of the name`() {
        val item = parseGroceryInput("3 lemons")!!

        assertEquals(3.0, item.amount!!, 0.001)
        assertEquals("", item.unit)
        assertEquals("lemons", item.name)
    }

    @Test
    fun `ignores blank input`() {
        assertNull(parseGroceryInput("   "))
    }

    @Test
    fun `adding a duplicate stacks the amount instead of repeating the line`() {
        val list = emptyList
            .plusItem(GroceryItem(0, "flour", 2.0, "cups"))
            .plusItem(GroceryItem(0, "Flour", 1.0, "cups"))

        assertEquals(1, list.size)
        assertEquals(3.0, list.single().amount!!, 0.001)
        assertEquals("3 cups flour", list.single().label)
    }

    @Test
    fun `a stacked line goes back to unpurchased`() {
        val bought = emptyList
            .plusItem(GroceryItem(0, "milk", 1.0, "l"))
            .map { it.copy(isPurchased = true) }

        val list = bought.plusItem(GroceryItem(0, "milk", 1.0, "l"))

        assertFalse(list.single().isPurchased)
    }

    @Test
    fun `different units stay on separate lines`() {
        val list = emptyList
            .plusItem(GroceryItem(0, "milk", 1.0, "l"))
            .plusItem(GroceryItem(0, "milk", 200.0, "ml"))

        assertEquals(2, list.size)
    }

    @Test
    fun `items get unique ids`() {
        val list = emptyList
            .plusItem(GroceryItem(0, "eggs", 6.0, ""))
            .plusItem(GroceryItem(0, "bread", null, ""))

        assertEquals(2, list.map { it.id }.distinct().size)
        assertTrue(list.none { it.id == 0L })
    }

    @Test
    fun `adding a recipe brings every ingredient across with its source`() {
        val recipe = SampleRecipes.all.first { it.name == "Ginger Miso Salmon" }

        val list = emptyList.plusRecipe(recipe)

        assertEquals(recipe.ingredients.size, list.size)
        assertTrue(list.all { it.recipeName == "Ginger Miso Salmon" })
        assertTrue(list.none { it.isPurchased })
    }

    @Test
    fun `adding a recipe scales amounts to the chosen servings`() {
        val recipe = SampleRecipes.all.first { it.name == "Ginger Miso Salmon" }

        val list = emptyList.plusRecipe(recipe, servings = recipe.servings * 2)

        val salmon = list.first { it.name == "salmon fillets" }
        assertEquals(4.0, salmon.amount!!, 0.001)
    }

    @Test
    fun `two recipes sharing an ingredient stack onto one line`() {
        val pasta = SampleRecipes.all.first { it.name == "Lemon Herb Pasta" }

        val list = emptyList.plusRecipe(pasta).plusRecipe(pasta)

        assertEquals(pasta.ingredients.size, list.size)
        val oil = list.first { it.name == "olive oil" }
        assertEquals(4.0, oil.amount!!, 0.001)
    }

    @Test
    fun `to buy and purchased split the list`() {
        val list = emptyList
            .plusItem(GroceryItem(0, "eggs", 6.0, ""))
            .plusItem(GroceryItem(0, "bread", null, ""))
            .map { if (it.name == "bread") it.copy(isPurchased = true) else it }

        assertEquals(listOf("eggs"), list.toBuy().map { it.name })
        assertEquals(listOf("bread"), list.purchased().map { it.name })
    }

    @Test
    fun `label rounds half amounts to one decimal`() {
        assertEquals("0.5 cup tahini", GroceryItem(1, "tahini", 0.5, "cup").label)
        assertEquals("2 lemons", GroceryItem(1, "lemons", 2.0, "").label)
    }
}
