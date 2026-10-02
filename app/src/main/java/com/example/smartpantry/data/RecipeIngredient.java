package com.example.smartpantry.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** One ingredient line of a recipe (one recipe has many). */
@Entity(
        tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipeId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("recipeId"))
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long recipeId;
    public String ingredientName;
    public double quantity;
    public String unit;

    public RecipeIngredient() { }

    @Ignore
    public RecipeIngredient(long recipeId, String ingredientName, double quantity, String unit) {
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }
}
