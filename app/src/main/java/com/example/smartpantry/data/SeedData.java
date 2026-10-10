package com.example.smartpantry.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Pre-loads 20 recipes the first time the database is created.
 * Countable items (egg, tomato, onion...) use "pcs"; solids use g; liquids use ml/tsp/tbsp.
 */
public class SeedData {

    // Tiny holder so each recipe can be written on a few readable lines.
    private static class I {
        final String name;
        final double qty;
        final String unit;

        I(String name, double qty, String unit) {
            this.name = name;
            this.qty = qty;
            this.unit = unit;
        }
    }

    private static I i(String name, double qty, String unit) {
        return new I(name, qty, unit);
    }

    private static void addRecipe(RecipeDao dao, String name, String steps, I... items) {
        long id = dao.insertRecipe(new Recipe(name, steps));
        List<RecipeIngredient> list = new ArrayList<>();
        for (I item : items) {
            list.add(new RecipeIngredient(id, item.name, item.qty, item.unit));
        }
        dao.insertIngredients(list);
    }

    public static void populate(RecipeDao dao) {
        addRecipe(dao, "Tomato Omelette",
                "1. Beat the eggs.\n2. Chop the tomato.\n3. Fry the tomato in the oil for 2 minutes.\n4. Pour in the eggs and cook until set.",
                i("egg", 2, "pcs"), i("tomato", 1, "pcs"), i("oil", 10, "ml"));

        addRecipe(dao, "Buttered Rice",
                "1. Cook the rice in boiling water until soft.\n2. Drain well.\n3. Stir in the butter and salt.",
                i("rice", 200, "g"), i("butter", 20, "g"), i("salt", 2, "g"));

        addRecipe(dao, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Melt the butter in a pan.\n3. Add the beaten eggs and salt, stirring gently until just set.\n4. Serve on the toast.",
                i("egg", 3, "pcs"), i("bread", 2, "pcs"), i("butter", 10, "g"), i("salt", 1, "g"));

        addRecipe(dao, "Cheese Toastie",
                "1. Butter one side of each slice of bread.\n2. Place the cheese between the unbuttered sides.\n3. Fry on both sides until golden and melted.",
                i("bread", 2, "pcs"), i("cheese", 50, "g"), i("butter", 10, "g"));

        addRecipe(dao, "Egg Fried Rice",
                "1. Chop the onion and fry it in the oil.\n2. Add the cooked rice and stir-fry for 3 minutes.\n3. Push the rice aside and scramble the eggs in the pan.\n4. Mix together and add the soy sauce.",
                i("rice", 200, "g"), i("egg", 2, "pcs"), i("onion", 1, "pcs"), i("oil", 15, "ml"), i("soy sauce", 1, "tbsp"));

        addRecipe(dao, "Tomato Pasta",
                "1. Boil the pasta in salted water until tender.\n2. Fry the chopped onion in the oil.\n3. Add the chopped tomatoes and simmer for 10 minutes.\n4. Toss the drained pasta in the sauce.",
                i("pasta", 200, "g"), i("tomato", 3, "pcs"), i("onion", 1, "pcs"), i("oil", 15, "ml"), i("salt", 2, "g"));

        addRecipe(dao, "Garlic Butter Pasta",
                "1. Boil the pasta until tender and drain.\n2. Melt the butter and gently fry the crushed garlic.\n3. Toss the pasta in the garlic butter and season with salt.",
                i("pasta", 200, "g"), i("butter", 30, "g"), i("garlic", 3, "pcs"), i("salt", 2, "g"));

        addRecipe(dao, "Mashed Potatoes",
                "1. Peel and chop the potatoes.\n2. Boil until soft, about 15 minutes, then drain.\n3. Mash with the butter, warm milk and salt.",
                i("potato", 4, "pcs"), i("butter", 30, "g"), i("milk", 100, "ml"), i("salt", 2, "g"));

        addRecipe(dao, "Pancakes",
                "1. Whisk the flour, egg, milk and sugar into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in a ladle of batter and cook until bubbles appear.\n4. Flip and cook the other side.",
                i("flour", 150, "g"), i("egg", 1, "pcs"), i("milk", 250, "ml"), i("sugar", 1, "tbsp"), i("butter", 10, "g"));

        addRecipe(dao, "French Toast",
                "1. Whisk the eggs and milk together.\n2. Dip each slice of bread in the mixture.\n3. Fry in the butter until golden on both sides.",
                i("bread", 4, "pcs"), i("egg", 2, "pcs"), i("milk", 100, "ml"), i("butter", 10, "g"));

        addRecipe(dao, "Cheese Omelette",
                "1. Beat the eggs with the salt.\n2. Melt the butter in a pan and pour in the eggs.\n3. When almost set, add the grated cheese and fold over.",
                i("egg", 3, "pcs"), i("cheese", 40, "g"), i("butter", 10, "g"), i("salt", 1, "g"));

        addRecipe(dao, "Roast Potatoes",
                "1. Heat the oven to 200 C.\n2. Cut the potatoes into chunks and toss with the oil and salt.\n3. Roast for 40 minutes, turning once, until crisp.",
                i("potato", 4, "pcs"), i("oil", 30, "ml"), i("salt", 2, "g"));

        addRecipe(dao, "Vegetable Soup",
                "1. Chop all the vegetables.\n2. Fry the onion in the oil for 3 minutes.\n3. Add the carrots, potatoes and enough water to cover.\n4. Simmer for 25 minutes and season with salt.",
                i("carrot", 2, "pcs"), i("potato", 2, "pcs"), i("onion", 1, "pcs"), i("oil", 10, "ml"), i("salt", 2, "g"));

        addRecipe(dao, "Tomato Soup",
                "1. Chop the tomatoes and onion.\n2. Melt the butter and soften the onion.\n3. Add the tomatoes and a cup of water, then simmer for 20 minutes.\n4. Blend until smooth and season with salt.",
                i("tomato", 5, "pcs"), i("onion", 1, "pcs"), i("butter", 20, "g"), i("salt", 2, "g"));

        addRecipe(dao, "Creamy Porridge",
                "1. Combine the oats and milk in a pot.\n2. Cook over medium heat, stirring, for 5 minutes.\n3. Stir in the sugar and serve warm.",
                i("oats", 80, "g"), i("milk", 250, "ml"), i("sugar", 1, "tsp"));

        addRecipe(dao, "Rice Pudding",
                "1. Put the rice, milk and sugar in a pot.\n2. Simmer gently for 30 minutes, stirring often.\n3. Serve warm or chilled.",
                i("rice", 100, "g"), i("milk", 500, "ml"), i("sugar", 2, "tbsp"));

        addRecipe(dao, "Cheesy Pasta",
                "1. Boil the pasta until tender and drain.\n2. Melt the butter, then stir in the milk and grated cheese until smooth.\n3. Toss the pasta in the sauce.",
                i("pasta", 200, "g"), i("cheese", 80, "g"), i("butter", 20, "g"), i("milk", 50, "ml"));

        addRecipe(dao, "Garlic Bread",
                "1. Mix the soft butter with the crushed garlic.\n2. Spread on the bread slices.\n3. Bake at 200 C for 8 minutes until golden.",
                i("bread", 4, "pcs"), i("butter", 40, "g"), i("garlic", 2, "pcs"));

        addRecipe(dao, "Banana Pancakes",
                "1. Mash the bananas in a bowl.\n2. Whisk in the eggs and flour.\n3. Cook spoonfuls in a hot pan for 2 minutes per side.",
                i("banana", 2, "pcs"), i("egg", 2, "pcs"), i("flour", 50, "g"));

        addRecipe(dao, "Tuna Pasta",
                "1. Boil the pasta until tender and drain.\n2. Fry the chopped onion in the oil.\n3. Add the drained tuna and warm through.\n4. Toss with the pasta.",
                i("pasta", 200, "g"), i("tuna", 1, "pcs"), i("onion", 1, "pcs"), i("oil", 10, "ml"));
    }
}