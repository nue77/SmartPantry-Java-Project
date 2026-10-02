package com.example.smartpantry.data;

import java.util.Arrays;

/** Pre-loads recipes on first run. Two samples for now; we expand to 15-20 in a later step. */
public class SeedData {

    public static void populate(RecipeDao dao) {
        long id = dao.insertRecipe(new Recipe(
                "Tomato Omelette",
                "1. Beat the eggs.\n2. Chop the tomato.\n3. Fry tomato in a little oil.\n4. Pour in eggs and cook until set."));
        dao.insertIngredients(Arrays.asList(
                new RecipeIngredient(id, "egg", 2, "pcs"),
                new RecipeIngredient(id, "tomato", 1, "pcs"),
                new RecipeIngredient(id, "oil", 10, "ml")));

        id = dao.insertRecipe(new Recipe(
                "Buttered Rice",
                "1. Cook the rice in water.\n2. Drain and stir in the butter.\n3. Season with salt."));
        dao.insertIngredients(Arrays.asList(
                new RecipeIngredient(id, "rice", 200, "g"),
                new RecipeIngredient(id, "butter", 20, "g"),
                new RecipeIngredient(id, "salt", 2, "g")));
    }
}
