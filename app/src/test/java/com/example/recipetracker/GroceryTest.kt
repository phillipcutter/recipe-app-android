package com.example.recipetracker

import com.example.recipetracker.model.SampleRecipes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GroceryTest {
    private lateinit var viewModel: RecipeViewModel

    @Before
    fun setUp() {
        viewModel = RecipeViewModel()
    }

    @Test
    fun `add grocery item updates state`() {
        viewModel.addGroceryItem("Milk", "1 gallon")
        viewModel.addGroceryItem("Eggs")

        val items = viewModel.state.groceryItems
        assertEquals(2, items.size)
        assertEquals("Milk", items[0].name)
        assertEquals("1 gallon", items[0].amountAndUnit)
        assertFalse(items[0].isChecked)

        assertEquals("Eggs", items[1].name)
        assertEquals("", items[1].amountAndUnit)
    }

    @Test
    fun `toggle grocery item checks and unchecks item`() {
        viewModel.addGroceryItem("Apples")
        val itemId = viewModel.state.groceryItems.first().id

        viewModel.toggleGroceryItem(itemId)
        assertTrue(viewModel.state.groceryItems.first().isChecked)
        assertEquals(1, viewModel.state.checkedGroceryCount)

        viewModel.toggleGroceryItem(itemId)
        assertFalse(viewModel.state.groceryItems.first().isChecked)
        assertEquals(0, viewModel.state.checkedGroceryCount)
    }

    @Test
    fun `delete grocery item removes item`() {
        viewModel.addGroceryItem("Butter")
        viewModel.addGroceryItem("Bread")
        val butterId = viewModel.state.groceryItems.first { it.name == "Butter" }.id

        viewModel.deleteGroceryItem(butterId)

        val items = viewModel.state.groceryItems
        assertEquals(1, items.size)
        assertEquals("Bread", items[0].name)
    }

    @Test
    fun `clear completed removes checked items only`() {
        viewModel.addGroceryItem("Item 1")
        viewModel.addGroceryItem("Item 2")
        viewModel.addGroceryItem("Item 3")

        val item1Id = viewModel.state.groceryItems[0].id
        val item3Id = viewModel.state.groceryItems[2].id

        viewModel.toggleGroceryItem(item1Id)
        viewModel.toggleGroceryItem(item3Id)

        viewModel.clearCompletedGroceryItems()

        val items = viewModel.state.groceryItems
        assertEquals(1, items.size)
        assertEquals("Item 2", items[0].name)
    }

    @Test
    fun `add recipe ingredients to grocery list adds all ingredients`() {
        val recipe = SampleRecipes.all.first()
        viewModel.addRecipeIngredientsToGrocery(recipe, multiplier = 1.0)

        val items = viewModel.state.groceryItems
        assertEquals(recipe.ingredients.size, items.size)
        assertTrue(items.all { !it.isChecked })
        assertEquals(recipe.ingredients.first().name, items.first().name)
    }
}
