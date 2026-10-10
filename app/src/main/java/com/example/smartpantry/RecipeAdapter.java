package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.Recipe;

/** Shows one suggested recipe per row. */
public class RecipeAdapter extends ListAdapter<Recipe, RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private OnRecipeClickListener listener;

    public RecipeAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Recipe> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Recipe>() {
                @Override
                public boolean areItemsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                    return a.name.equals(b.name);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = getItem(position);
        holder.tvName.setText(recipe.name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(recipe);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
        }
    }
}
