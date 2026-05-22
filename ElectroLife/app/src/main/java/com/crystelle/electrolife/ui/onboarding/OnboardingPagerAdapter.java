package com.crystelle.electrolife.ui.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.R;

/**
 * Adapter for the ViewPager2 in OnboardingActivity.
 * Renders the swipeable marketing cards dynamically.
 */
public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final Context context;

    // Hardcoded arrays reflecting your original ViewFlipper layouts
    private final int[] slideIcons = {
            R.drawable.ic_zap,
            R.drawable.ic_calculator,
            R.drawable.ic_history,
            R.drawable.ic_monitor,
            R.drawable.ic_lightbulb
    };

    private final String[] slideTitles = {
            "Welcome",
            "Real-Time Analytics",
            "Data Archiving",
            "Usage Goals",
            "Smart Tips"
    };

    private final String[] slideDescriptions = {
            "Take control of your energy consumption today.",
            "Instantly calculate the cost of every appliance.",
            "Keep a history of your monthly savings.",
            "Set kWh limits to stay green and save money.",
            "Get AI recommendations for your household."
    };

    public OnboardingPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the layout for a single slide
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding_slide, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Set the icon and apply your original blue tint
        holder.ivIcon.setImageResource(slideIcons[position]);
        holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.blue_600), android.graphics.PorterDuff.Mode.SRC_IN);

        // Set the texts
        holder.tvTitle.setText(slideTitles[position]);
        holder.tvDescription.setText(slideDescriptions[position]);
    }

    @Override
    public int getItemCount() {
        return slideTitles.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind views from item_onboarding_slide.xml
            ivIcon = itemView.findViewById(R.id.iv_slide_icon);
            tvTitle = itemView.findViewById(R.id.tv_slide_title);
            tvDescription = itemView.findViewById(R.id.tv_slide_description);
        }
    }
}