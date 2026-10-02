package com.example.smartpantry.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/** A recipe. Its ingredients live in the RecipeIngredient table. */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String steps;
    public Recipe() { }

    @Ignore
    public Recipe(String name, String steps){
        this.name = name;
        this.steps = steps;
    }
}
