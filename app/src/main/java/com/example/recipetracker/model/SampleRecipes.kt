package com.example.recipetracker.model

object SampleRecipes {
    val all = listOf(
        Recipe(
            id = 1,
            name = "Lemon Herb Pasta",
            description = "Bright, silky pasta with lemon, herbs, and parmesan.",
            prepMinutes = 25,
            servings = 2,
            difficulty = "Easy",
            tags = setOf("Vegetarian", "Quick", "Italian"),
            plans = listOf(
                RecipePlan(
                    id = 1,
                    name = "Pasta",
                    notes = "Salt the water generously so the noodles season themselves.",
                    ingredients = listOf(Ingredient(200.0, "g", "spaghetti")),
                    steps = listOf("Cook the pasta until al dente, reserving a cup of pasta water."),
                ),
                RecipePlan(
                    id = 2,
                    name = "Lemon sauce",
                    ingredients = listOf(
                        Ingredient(1.0, "", "lemon"),
                        Ingredient(2.0, "tbsp", "olive oil"),
                        Ingredient(40.0, "g", "parmesan"),
                        Ingredient(2.0, "tbsp", "chopped parsley"),
                    ),
                    steps = listOf(
                        "Whisk lemon zest, juice, olive oil, and parmesan in a warm pan.",
                        "Toss with pasta and enough cooking water to make a glossy sauce.",
                        "Finish with parsley, pepper, and extra parmesan.",
                    ),
                ),
            ),
            isFavorite = true,
        ),
        Recipe(
            id = 2,
            name = "Smoky Chickpea Bowls",
            description = "A colorful weeknight bowl with crisp chickpeas and tahini drizzle.",
            prepMinutes = 35,
            servings = 4,
            difficulty = "Easy",
            tags = setOf("Vegetarian", "Meal prep", "High protein"),
            plans = listOf(
                RecipePlan(
                    id = 1,
                    name = "Chickpeas",
                    ingredients = listOf(
                        Ingredient(2.0, "cans", "chickpeas"),
                        Ingredient(2.0, "tsp", "smoked paprika"),
                    ),
                    steps = listOf("Roast chickpeas with paprika and salt at 220°C until crisp."),
                ),
                RecipePlan(
                    id = 2,
                    name = "Bowls",
                    ingredients = listOf(
                        Ingredient(2.0, "cups", "cooked brown rice"),
                        Ingredient(1.0, "", "cucumber"),
                        Ingredient(0.25, "cup", "tahini"),
                    ),
                    steps = listOf(
                        "Whisk tahini with lemon juice and a splash of water.",
                        "Divide rice and vegetables between bowls.",
                        "Top with chickpeas and tahini sauce.",
                    ),
                ),
            ),
        ),
        Recipe(
            id = 3,
            name = "Ginger Miso Salmon",
            description = "Caramelized salmon with a savory-sweet miso glaze.",
            prepMinutes = 20,
            servings = 2,
            difficulty = "Medium",
            tags = setOf("Quick", "Seafood", "Japanese-inspired"),
            plans = listOf(
                RecipePlan(
                    id = 1,
                    name = "Glaze",
                    ingredients = listOf(
                        Ingredient(1.5, "tbsp", "white miso"),
                        Ingredient(1.0, "tbsp", "soy sauce"),
                        Ingredient(1.0, "tsp", "fresh ginger"),
                        Ingredient(1.0, "tsp", "honey"),
                    ),
                    steps = listOf("Mix miso, soy, ginger, and honey into a glaze."),
                ),
                RecipePlan(
                    id = 2,
                    name = "Broil",
                    ingredients = listOf(Ingredient(2.0, "", "salmon fillets")),
                    steps = listOf(
                        "Brush glaze over salmon and rest for five minutes.",
                        "Broil for 7–9 minutes until caramelized and just cooked.",
                    ),
                ),
            ),
        ),
        Recipe(
            id = 4,
            name = "Sunday Tomato Soup",
            description = "Slow-roasted tomatoes blended into a deeply comforting soup.",
            prepMinutes = 55,
            servings = 6,
            difficulty = "Easy",
            tags = setOf("Vegetarian", "Soup", "Freezer friendly"),
            plans = listOf(
                RecipePlan(
                    id = 1,
                    name = "Roast",
                    ingredients = listOf(
                        Ingredient(1.5, "kg", "ripe tomatoes"),
                        Ingredient(1.0, "", "yellow onion"),
                        Ingredient(4.0, "cloves", "garlic"),
                    ),
                    steps = listOf("Roast tomatoes, onion, and garlic until browned at the edges."),
                ),
                RecipePlan(
                    id = 2,
                    name = "Simmer & blend",
                    ingredients = listOf(
                        Ingredient(3.0, "cups", "vegetable stock"),
                        Ingredient(0.5, "cup", "cream"),
                    ),
                    steps = listOf(
                        "Simmer with stock for fifteen minutes.",
                        "Blend until smooth, stir in cream, and season to taste.",
                    ),
                ),
            ),
        ),
    )
}
