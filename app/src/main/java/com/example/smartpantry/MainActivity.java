package com.example.smartpantry;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/** Pantry List screen: shows every ingredient currently in the pantry. */
public class MainActivity extends AppCompatActivity {

    private PantryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(findViewById(R.id.toolbar));

        RecyclerView recyclerView = findViewById(R.id.recyclerPantry);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        PantryAdapter adapter = new PantryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        // Runs every time the pantry table changes, so the list always stays current.
        viewModel.getPantryItems().observe(this, items -> {
            adapter.submitList(items);
            tvEmpty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // TEMPORARY: lets us test the list before the Add/Edit screen exists.
        fabAdd.setOnClickListener(v -> {
            long threeDays = 3L * 24 * 60 * 60 * 1000;
            viewModel.insert(new PantryItem("Test item", 1, "pcs",
                    System.currentTimeMillis() + threeDays));
        });

        // TEMPORARY: replaced by opening the Edit screen next step.
        adapter.setOnItemClickListener(item ->
                Toast.makeText(this, "Clicked: " + item.name, Toast.LENGTH_SHORT).show());
    }
}