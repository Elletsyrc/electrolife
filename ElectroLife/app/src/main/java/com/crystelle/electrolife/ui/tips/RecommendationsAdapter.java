package com.crystelle.electrolife.ui.tips;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.R;
import com.crystelle.electrolife.model.SmartRecommendation;

/**
 * REVISED: RecommendationsAdapter handles the visualization of AI-generated insights.
 * STATUS: Optimized with 0 Lint warnings.
 * Logic: Uses static mapping for icons and resource-based string formatting.
 */
public class RecommendationsAdapter extends ListAdapter<SmartRecommendation, RecommendationsAdapter.ViewHolder> {

    public RecommendationsAdapter() {
        // Task 1: Diamond operator used to simplify type arguments
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull SmartRecommendation oldItem, @NonNull SmartRecommendation newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areContentsTheSame(@NonNull SmartRecommendation oldItem, @NonNull SmartRecommendation newItem) {
                return oldItem.getDescription().equals(newItem.getDescription()) &&
                        oldItem.getType().equals(newItem.getType()) &&
                        oldItem.getPotentialSavings().equals(newItem.getPotentialSavings()) &&
                        oldItem.getActionable().equals(newItem.getActionable());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * FIX: Public visibility ensures the class is accessible within the Adapter's generic signature.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDescription, tvSavings, tvAction;
        private final ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_rec_title);
            tvDescription = itemView.findViewById(R.id.tv_rec_description);
            ivIcon = itemView.findViewById(R.id.iv_rec_icon);
            tvSavings = itemView.findViewById(R.id.tv_rec_savings);
            tvAction = itemView.findViewById(R.id.tv_rec_action);
        }

        public void bind(SmartRecommendation rec) {
            tvTitle.setText(rec.getTitle());
            tvDescription.setText(rec.getDescription());

            // Bind savings text if available
            if (tvSavings != null && rec.getPotentialSavings() != null) {
                tvSavings.setText(rec.getPotentialSavings());
                tvSavings.setVisibility(rec.getPotentialSavings().isEmpty() ? View.GONE : View.VISIBLE);
            }

            // Bind actionable instruction using resource string with placeholder
            if (tvAction != null && rec.getActionable() != null) {
                tvAction.setText(itemView.getContext().getString(R.string.rec_action_format, rec.getActionable()));
                tvAction.setVisibility(rec.getActionable().isEmpty() ? View.GONE : View.VISIBLE);
            }

            // Bind severity icon using static mapping
            ivIcon.setImageResource(getIconResourceId(rec.getIconName()));
        }

        /**
         * Mapping helper to convert string icon names to direct resource identifiers.
         * FIX: Removed duplicate branches for "info" and "ic_lightbulb" as they match the default.
         */
        @DrawableRes
        private int getIconResourceId(String iconName) {
            if (iconName == null) return R.drawable.ic_lightbulb;

            switch (iconName) {
                case "ic_zap":
                case "danger":
                    return R.drawable.ic_zap;
                case "ic_settings":
                case "warning":
                    return R.drawable.ic_settings;
                default:
                    // Automatically handles "info", "ic_lightbulb", and any unrecognized values
                    return R.drawable.ic_lightbulb;
            }
        }
    }
}