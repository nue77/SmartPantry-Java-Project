package com.example.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.Recipe;
import com.example.smartpantry.data.RecipeIngredient;
import com.example.smartpantry.data.RecipeMatcher;

import java.util.List;
import java.util.Map;

/** Lists ONLY the recipes the user can make right now (strict matching). */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle("Suggested Recipes");

        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView recyclerView = findViewById(R.id.recyclerRecipes);
        adapter = new RecipeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Tapping a recipe opens the detail screen, passing the recipe id in the Intent.
        adapter.setOnRecipeClickListener(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });

        BottomNavHelper.setup(this, R.id.nav_recipes);
    }

    // Re-run the matching every time the screen appears, so changes to the pantry show up.
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        Context appContext = getApplicationContext();

        // Database work must not run on the main thread.
        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(appContext);

            List<PantryItem> pantry = db.pantryItemDao().getAllNow();
            List<Recipe> recipes = db.recipeDao().getAllRecipesNow();
            Map<Long, List<RecipeIngredient>> ingredients =
                    RecipeMatcher.groupByRecipe(db.recipeDao().getAllIngredientsNow());

            // The strict-matching rule decides what appears on screen.
            List<Recipe> matches = RecipeMatcher.findMatches(pantry, recipes, ingredients);

            runOnUiThread(() -> {
                adapter.submitList(matches);
                tvEmpty.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }
}
