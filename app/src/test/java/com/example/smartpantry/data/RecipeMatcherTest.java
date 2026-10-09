package com.example.smartpantry.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMatcherTest {

    // ----- helpers -----------------------------------------------------

    private PantryItem pantry(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, null);
    }

    private RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(1, name, qty, unit);
    }

    /** Runs the matcher against a single recipe (id 1) with the given ingredients. */
    private List<Recipe> match(List<PantryItem> pantryItems, RecipeIngredient... needs) {
        Recipe recipe = new Recipe("Test recipe", "steps");
        recipe.id = 1;
        Map<Long, List<RecipeIngredient>> map = new HashMap<>();
        map.put(1L, Arrays.asList(needs));
        return RecipeMatcher.findMatches(pantryItems, Collections.singletonList(recipe), map);
    }

    // ----- the strict rule from the brief -------------------------------

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        List<PantryItem> items = Arrays.asList(
                pantry("egg", 2, "pcs"), pantry("tomato", 1, "pcs"), pantry("oil", 50, "ml"));
        assertEquals(1, match(items,
                need("egg", 2, "pcs"), need("tomato", 1, "pcs"), need("oil", 10, "ml")).size());
    }

    @Test
    public void fourOfFiveIngredients_recipeIsNotSuggested() {
        List<PantryItem> items = Arrays.asList(
                pantry("egg", 2, "pcs"), pantry("tomato", 1, "pcs"),
                pantry("oil", 50, "ml"), pantry("salt", 5, "g"));  // no pepper
        assertTrue(match(items,
                need("egg", 2, "pcs"), need("tomato", 1, "pcs"), need("oil", 10, "ml"),
                need("salt", 2, "g"), need("pepper", 1, "g")).isEmpty());
    }

    @Test
    public void addingTheMissingIngredient_makesRecipeAppear() {
        RecipeIngredient[] needs = {need("egg", 2, "pcs"), need("salt", 2, "g")};

        List<PantryItem> before = Collections.singletonList(pantry("egg", 2, "pcs"));
        assertTrue(match(before, needs).isEmpty());

        List<PantryItem> after = Arrays.asList(pantry("egg", 2, "pcs"), pantry("salt", 5, "g"));
        assertEquals(1, match(after, needs).size());
    }

    @Test
    public void emptyPantry_returnsNoRecipes() {
        assertTrue(match(Collections.<PantryItem>emptyList(), need("egg", 1, "pcs")).isEmpty());
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        assertTrue(match(Collections.singletonList(pantry("egg", 2, "pcs"))).isEmpty());
    }

    // ----- quantities ---------------------------------------------------

    @Test
    public void notEnoughQuantity_recipeIsNotSuggested() {
        assertTrue(match(Collections.singletonList(pantry("egg", 1, "pcs")),
                need("egg", 2, "pcs")).isEmpty());
    }

    @Test
    public void exactlyEnoughQuantity_recipeIsSuggested() {
        assertEquals(1, match(Collections.singletonList(pantry("rice", 200, "g")),
                need("rice", 200, "g")).size());
    }

    @Test
    public void duplicatePantryRows_areAddedTogether() {
        List<PantryItem> items = Arrays.asList(pantry("Egg", 1, "pcs"), pantry("eggs", 1, "pcs"));
        assertEquals(1, match(items, need("egg", 2, "pcs")).size());
    }

    // ----- real-world messiness ------------------------------------------

    @Test
    public void singularAndPlural_areTreatedAsTheSame() {
        assertEquals(1, match(Collections.singletonList(pantry("Tomato", 2, "pcs")),
                need("tomatoes", 1, "pcs")).size());
        assertEquals(1, match(Collections.singletonList(pantry("Tomatoes", 2, "pcs")),
                need("tomato", 1, "pcs")).size());
    }

    @Test
    public void capitalisationAndSpaces_areIgnored() {
        assertEquals(1, match(Collections.singletonList(pantry("  EGGS ", 2, "pcs")),
                need("egg", 2, "pcs")).size());
    }

    @Test
    public void unitConversion_kilogramsSatisfyGrams() {
        assertEquals(1, match(Collections.singletonList(pantry("rice", 1, "kg")),
                need("rice", 200, "g")).size());
    }

    @Test
    public void unitConversion_notEnoughAfterConverting() {
        assertTrue(match(Collections.singletonList(pantry("rice", 0.1, "kg")),
                need("rice", 200, "g")).isEmpty());
    }

    @Test
    public void incompatibleUnits_areNotMatched() {
        // 2 whole eggs cannot be compared with 100 g of egg
        assertTrue(match(Collections.singletonList(pantry("egg", 2, "pcs")),
                need("egg", 100, "g")).isEmpty());
    }

    // ----- name normalising ---------------------------------------------

    @Test
    public void normalizeName_handlesCommonPlurals() {
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("berry", RecipeMatcher.normalizeName("Berries"));
        assertEquals("peach", RecipeMatcher.normalizeName("Peaches"));
        assertEquals("egg", RecipeMatcher.normalizeName("  EGGS "));
        assertEquals("cherry tomato", RecipeMatcher.normalizeName("Cherry Tomatoes"));
        assertEquals(RecipeMatcher.normalizeName("cookie"), RecipeMatcher.normalizeName("cookies"));
    }

    @Test
    public void normalizeName_leavesWordsThatEndInSAlone() {
        assertEquals("hummus", RecipeMatcher.normalizeName("Hummus"));
        assertEquals("asparagus", RecipeMatcher.normalizeName("asparagus"));
    }
}