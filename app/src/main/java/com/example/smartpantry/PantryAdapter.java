package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.data.PantryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/** Turns each PantryItem into one row on the pantry list screen. */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private OnItemClickListener listener;

    public PantryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Lets the list animate only the rows that actually changed.
    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PantryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
                    return Objects.equals(a.name, b.name)
                            && a.quantity == b.quantity
                            && Objects.equals(a.unit, b.unit)
                            && Objects.equals(a.expiryDate, b.expiryDate);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = getItem(position);

        holder.tvName.setText(item.name);

        // Show 3 instead of 3.0 for whole numbers.
        String qty = (item.quantity == (long) item.quantity)
                ? String.valueOf((long) item.quantity)
                : String.valueOf(item.quantity);
        holder.tvQuantity.setText(qty + " " + item.unit);

        if (item.expiryDate != null) {
            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(new Date(item.expiryDate));
            holder.tvExpiry.setText("Expires: " + date);
        } else {
            holder.tvExpiry.setText("No expiry date");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvQuantity, tvExpiry;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
        }
    }
}
