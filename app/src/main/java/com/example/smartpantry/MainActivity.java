package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // "+" button: open the form in add mode (no extras).
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditActivity.class)));

        // Tapping a row: open the same form in edit mode, passing the item's data.
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(this, AddEditActivity.class);
            intent.putExtra(AddEditActivity.EXTRA_ID, item.id);
            intent.putExtra(AddEditActivity.EXTRA_NAME, item.name);
            intent.putExtra(AddEditActivity.EXTRA_QUANTITY, item.quantity);
            intent.putExtra(AddEditActivity.EXTRA_UNIT, item.unit);
            if (item.expiryDate != null) {
                intent.putExtra(AddEditActivity.EXTRA_EXPIRY, item.expiryDate);
            }
            startActivity(intent);
        });
    }
}
