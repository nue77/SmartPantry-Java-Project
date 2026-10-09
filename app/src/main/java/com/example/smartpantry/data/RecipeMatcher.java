package com.example.smartpantry.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The strict-matching rule (assignment Section 2.3).
 *
 * A recipe is suggested ONLY if every ingredient it needs is in the pantry,
 * in at least the required quantity. One missing or short ingredient = not suggested.
 *
 * Pure Java (no Android classes) so it can be unit tested quickly.
 */
public class RecipeMatcher {

    private enum UnitType { WEIGHT, VOLUME, COUNT }

    private static class UnitInfo {
        final UnitType type;
        final double factor;  // multiply by this to reach the base unit (g, ml or pcs)

        UnitInfo(UnitType type, double factor) {
            this.type = type;
            this.factor = factor;
        }
    }

    private static final double EPSILON = 1e-9;
    private static final Map<String, UnitInfo> UNITS = new HashMap<>();
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    static {
        addUnit(UnitType.WEIGHT, 1, "g", "gram", "grams");
        addUnit(UnitType.WEIGHT, 1000, "kg", "kilogram", "kilograms");
        addUnit(UnitType.VOLUME, 1, "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        addUnit(UnitType.VOLUME, 1000, "l", "litre", "litres", "liter", "liters");
        addUnit(UnitType.VOLUME, 5, "tsp", "teaspoon", "teaspoons");
        addUnit(UnitType.VOLUME, 15, "tbsp", "tablespoon", "tablespoons");
        addUnit(UnitType.VOLUME, 250, "cup", "cups");
        addUnit(UnitType.COUNT, 1, "pcs", "pc", "piece", "pieces", "unit", "units");

        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("molasses", "molasses");
    }

    private static void addUnit(UnitType type, double factor, String... names) {
        for (String n : names) UNITS.put(n, new UnitInfo(type, factor));
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Returns only the recipes the user can make right now. */
    public static List<Recipe> findMatches(List<PantryItem> pantry,
                                           List<Recipe> recipes,
                                           Map<Long, List<RecipeIngredient>> ingredientsByRecipe) {
        List<Recipe> result = new ArrayList<>();
        if (pantry == null || pantry.isEmpty() || recipes == null) return result;

        Map<String, Double> pantryTotals = buildPantryTotals(pantry);
        for (Recipe recipe : recipes) {
            if (canMake(pantryTotals, ingredientsByRecipe.get(recipe.id))) {
                result.add(recipe);
            }
        }
        return result;
    }

    /** True only if EVERY needed ingredient is available in enough quantity. */
    public static boolean canMake(Map<String, Double> pantryTotals, List<RecipeIngredient> needed) {
        if (needed == null || needed.isEmpty()) return false;  // nothing to cook = not suggested

        // Add up what the recipe needs per ingredient (in case a name appears twice).
        Map<String, Double> required = new HashMap<>();
        for (RecipeIngredient ing : needed) {
            String key = keyFor(ing.ingredientName, ing.unit);
            if (key == null) return false;  // unknown unit: cannot verify, so do not suggest
            double amount = ing.quantity * unitInfo(ing.unit).factor;
            Double soFar = required.get(key);
            required.put(key, (soFar == null ? 0 : soFar) + amount);
        }

        // Every single requirement must be covered by the pantry.
        for (Map.Entry<String, Double> need : required.entrySet()) {
            Double have = pantryTotals.get(need.getKey());
            if (have == null || have + EPSILON < need.getValue()) return false;
        }
        return true;
    }

    /** Totals the pantry per ingredient, converting units and merging duplicate rows. */
    public static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = keyFor(item.name, item.unit);
            if (key == null) continue;
            double amount = item.quantity * unitInfo(item.unit).factor;
            Double soFar = totals.get(key);
            totals.put(key, (soFar == null ? 0 : soFar) + amount);
        }
        return totals;
    }

    /** Groups a flat list of recipe ingredients by recipe id. */
    public static Map<Long, List<RecipeIngredient>> groupByRecipe(List<RecipeIngredient> all) {
        Map<Long, List<RecipeIngredient>> map = new HashMap<>();
        for (RecipeIngredient ing : all) {
            List<RecipeIngredient> list = map.get(ing.recipeId);
            if (list == null) {
                list = new ArrayList<>();
                map.put(ing.recipeId, list);
            }
            list.add(ing);
        }
        return map;
    }

    // ------------------------------------------------------------------
    // Name normalising: "Tomatoes " and "tomato" must be the same ingredient
    // ------------------------------------------------------------------

    public static String normalizeName(String name) {
        if (name == null) return "";
        String cleaned = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N} ]", " ")
                .trim()
                .replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(singularize(word));
        }
        return sb.toString();
    }

    /**
     * Simple plural handling, not full NLP. It is applied to BOTH the pantry and the
     * recipe, so even an imperfect result is fine as long as both sides agree.
     */
    private static String singularize(String w) {
        String irregular = IRREGULAR.get(w);
        if (irregular != null) return irregular;

        String s = w;
        if (s.length() > 4 && s.endsWith("ies")) {                       // berries -> berry
            s = s.substring(0, s.length() - 3) + "y";
        } else if (s.length() > 4 && s.endsWith("oes")) {                // tomatoes -> tomato
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("ches") || s.endsWith("shes")
                || s.endsWith("xes") || s.endsWith("sses")) {            // peaches -> peach
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("ss") || s.endsWith("us")) {
            // leave alone: "hummus", "asparagus", "glass"
        } else if (s.length() > 3 && s.endsWith("s")) {                  // eggs -> egg
            s = s.substring(0, s.length() - 1);
        }

        if (s.length() > 2 && s.endsWith("ie")) {                        // cookie and cookies agree
            s = s.substring(0, s.length() - 2) + "y";
        }
        return s;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static UnitInfo unitInfo(String unit) {
        if (unit == null) return null;
        return UNITS.get(unit.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Key = normalised name + unit family. A weight (g) can never satisfy a count (pcs),
     * so "100 g egg" will not be matched by "2 pcs egg". Returns null if unit is unknown.
     */
    private static String keyFor(String name, String unit) {
        UnitInfo info = unitInfo(unit);
        String norm = normalizeName(name);
        if (info == null || norm.isEmpty()) return null;
        return norm + "|" + info.type.name();
    }
}