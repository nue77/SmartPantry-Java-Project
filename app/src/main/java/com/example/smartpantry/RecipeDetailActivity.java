package com.example.smartpantry;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.Recipe;
import com.example.smartpantry.data.RecipeIngredient;

import java.util.List;

/** Shows the full ingredient list and method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvIngredients = findViewById(R.id.tvIngredients);
        TextView tvSteps = findViewById(R.id.tvSteps);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Context appContext = getApplicationContext();

        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(appContext);
            Recipe recipe = db.recipeDao().getRecipeById(recipeId);
            List<RecipeIngredient> ingredients = db.recipeDao().getIngredientsForRecipe(recipeId);

            runOnUiThread(() -> {
                if (recipe == null) {
                    finish();  // bad id: nothing to show
                    return;
                }
                setTitle(recipe.name);

                StringBuilder sb = new StringBuilder();
                for (RecipeIngredient ing : ingredients) {
                    if (sb.length() > 0) sb.append('\n');
                    sb.append("\u2022 ")
                            .append(formatQuantity(ing.quantity)).append(' ')
                            .append(ing.unit).append(' ')
                            .append(ing.ingredientName);
                }
                tvIngredients.setText(sb.toString());
                tvSteps.setText(recipe.steps);
            });
        });
    }

    // Show 3 instead of 3.0 for whole numbers.
    private String formatQuantity(double q) {
        return (q == (long) q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
