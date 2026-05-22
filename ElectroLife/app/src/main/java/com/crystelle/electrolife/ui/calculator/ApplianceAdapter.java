package com.crystelle.electrolife.ui.calculator;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.crystelle.electrolife.R;
import com.crystelle.electrolife.model.TrackedAppliance;
import com.crystelle.electrolife.utils.CalculationsUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.util.Locale;

/**
 * FULLY REVISED: ApplianceAdapter (Ultra-Stable UX Edition)
 * DATA SYNC FIX: Bypassed CostBreakdown object to use explicit inline math formulas,
 * guaranteeing 100% accuracy matching the Summary Fragment.
 */
public class ApplianceAdapter extends ListAdapter<TrackedAppliance, ApplianceAdapter.ViewHolder> {

    // Baseline fallback rate
    private double currentRate = 12.00;
    private final OnApplianceInteractionListener listener;

    public interface OnApplianceInteractionListener {
        void onRemove(String id);
        void onHoursChanged(String id, double hours);
        void onLimitOverride(String id, double kwhLimit);
    }

    public ApplianceAdapter(OnApplianceInteractionListener listener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull TrackedAppliance oldItem, @NonNull TrackedAppliance newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull TrackedAppliance oldItem, @NonNull TrackedAppliance newItem) {
                boolean hoursMatch = Math.abs(oldItem.getHoursPerDay() - newItem.getHoursPerDay()) < 0.1;
                boolean limitsMatch = Math.abs(oldItem.getCustomKwhLimit() - newItem.getCustomKwhLimit()) < 0.1;
                return hoursMatch && limitsMatch && oldItem.isEnergyVampire() == newItem.isEnergyVampire();
            }
        });
        this.listener = listener;
    }

    /**
     * FIX: Replaced notifyItemRangeChanged with notifyDataSetChanged
     * to forcefully break the cache and apply the new rate immediately to all cards.
     */
    public void setRate(double rate) {
        if (this.currentRate != rate) {
            this.currentRate = rate;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appliance_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), currentRate, listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvWattage, tvHoursValue, tvCostHour, tvCostDay, tvCostMonth, tvApplianceKwh;
        private final ImageView ivIcon;
        private final View iconContainer;
        private final Slider sliderHours;
        private final ImageButton btnRemove;
        private final MaterialCardView cardRoot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = (MaterialCardView) itemView;
            tvName = itemView.findViewById(R.id.tv_appliance_name);
            tvWattage = itemView.findViewById(R.id.tv_appliance_wattage);
            tvApplianceKwh = itemView.findViewById(R.id.tv_appliance_kwh);
            tvHoursValue = itemView.findViewById(R.id.tv_hours_value);
            tvCostHour = itemView.findViewById(R.id.tv_cost_hour);
            tvCostDay = itemView.findViewById(R.id.tv_cost_day);
            tvCostMonth = itemView.findViewById(R.id.tv_cost_month);
            ivIcon = itemView.findViewById(R.id.iv_appliance_icon);
            iconContainer = itemView.findViewById(R.id.icon_container);
            sliderHours = itemView.findViewById(R.id.slider_hours);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(TrackedAppliance item, double rate, OnApplianceInteractionListener listener) {
            if (item.getAppliance() == null) {
                tvName.setText(R.string.loading_device);
                return;
            }

            Object tag = sliderHours.getTag();
            boolean isUserDragging = tag != null && (boolean) tag;

            tvName.setText(item.getAppliance().getName());
            tvWattage.setText(CalculationsUtil.formatWattage(item.getAppliance().getDefaultWattage()));
            @DrawableRes int iconResId = getIconResourceId(item.getAppliance().getIconName());

            if (item.hasCustomLimit()) {
                tvApplianceKwh.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.blue_600));
            } else {
                tvApplianceKwh.setTextColor(Color.parseColor("#757575"));
            }

            if (item.isEnergyVampire()) {
                iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.red_100)));
                ivIcon.setImageResource(R.drawable.ic_zap);
                ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.red_600)));
                tvCostMonth.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red_600));
                cardRoot.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.red_600));
                cardRoot.setStrokeWidth(4);
            } else {
                iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.blue_100)));
                ivIcon.setImageResource(iconResId);
                ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.blue_600)));
                tvCostMonth.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green_600));
                cardRoot.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.gray_200));
                cardRoot.setStrokeWidth(2);
            }

            sliderHours.clearOnChangeListeners();
            sliderHours.clearOnSliderTouchListeners();

            if (!isUserDragging) {
                float targetVal = (float) item.getHoursPerDay();
                float clampedVal = Math.max(0f, Math.min(24f, targetVal));

                sliderHours.setValue(clampedVal);
                updateCardDynamicData(item, clampedVal, rate);
            }

            btnRemove.setOnClickListener(v -> listener.onRemove(item.getId()));

            sliderHours.addOnChangeListener((slider, value, fromUser) -> {
                if (fromUser) {
                    updateCardDynamicData(item, value, rate);
                }
            });

            sliderHours.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    sliderHours.setTag(true);
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    sliderHours.setTag(false);
                    playPulseAnimation(tvHoursValue, tvCostMonth);

                    double roundedValue = Math.round(slider.getValue() * 10.0) / 10.0;
                    listener.onHoursChanged(item.getId(), roundedValue);
                }
            });

            GestureDetector gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    showLimitOverrideDialog(itemView.getContext(), item, listener);
                    return true;
                }
            });

            itemView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) v.performClick();
                return true;
            });

            tvHoursValue.setOnLongClickListener(v -> {
                showManualHoursDialog(itemView.getContext(), item, listener);
                return true;
            });
        }

        /**
         * MATH FIX: Explicit raw calculations to guarantee correct data
         * Bypasses CostBreakdown to avoid mapping errors.
         */
        private void updateCardDynamicData(TrackedAppliance item, double hours, double rate) {
            double wattage = item.getAppliance().getDefaultWattage();

            // 1. Update Hours Display
            tvHoursValue.setText(String.format(Locale.getDefault(), "%.1fh", hours));

            // 2. EXPLICIT MATH formulas
            double hourlyKwh = wattage / 1000.0;             // e.g., 60 / 1000 = 0.06
            double dailyKwh = hourlyKwh * hours;             // e.g., 0.06 * 8 = 0.48

            double explicitCostHour = hourlyKwh * rate;      // e.g., 0.06 * 12.00 = 0.72
            double explicitCostDay = dailyKwh * rate;        // e.g., 0.48 * 12.00 = 5.76
            double explicitCostMonth = explicitCostDay * 30.0; // e.g., 5.76 * 30 = 172.80

            // 3. Update Daily Energy (kWh) Display
            if (item.hasCustomLimit()) {
                tvApplianceKwh.setText(String.format(Locale.getDefault(), "%.2f kWh/day | 🎯 Limit: %.1f", dailyKwh, item.getCustomKwhLimit()));
            } else {
                tvApplianceKwh.setText(String.format(Locale.getDefault(), "%.2f kWh/day", dailyKwh));
            }

            // 4. Update Financial Cost Display to the UI
            tvCostHour.setText(CalculationsUtil.formatCurrency(explicitCostHour));
            tvCostDay.setText(CalculationsUtil.formatCurrency(explicitCostDay));
            tvCostMonth.setText(CalculationsUtil.formatCurrency(explicitCostMonth));
        }

        private void playPulseAnimation(View... views) {
            for (View v : views) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.15f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.15f, 1f);
                scaleX.setDuration(350);
                scaleY.setDuration(350);
                scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleX.start();
                scaleY.start();
            }
        }

        @DrawableRes
        private int getIconResourceId(String iconName) {
            if (iconName == null) return R.drawable.ic_monitor;
            switch (iconName.toLowerCase(Locale.ROOT)) {
                case "light": return R.drawable.ic_lightbulb;
                case "zap":
                case "vampire": return R.drawable.ic_zap;
                default: return R.drawable.ic_monitor;
            }
        }

        private void showLimitOverrideDialog(android.content.Context context, TrackedAppliance item, OnApplianceInteractionListener listener) {
            EditText input = new EditText(context);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Set Energy Limit")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        try {
                            String val = input.getText().toString();
                            if (!val.isEmpty()) {
                                listener.onLimitOverride(item.getId(), Double.parseDouble(val));
                            }
                        } catch (Exception ignored) {}
                    })
                    .show();
        }

        private void showManualHoursDialog(android.content.Context context, TrackedAppliance item, OnApplianceInteractionListener listener) {
            EditText input = new EditText(context);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Set Exact Hours")
                    .setView(input)
                    .setPositiveButton("Apply", (dialog, which) -> {
                        try {
                            String val = input.getText().toString();
                            if (!val.isEmpty()) {
                                listener.onHoursChanged(item.getId(), Double.parseDouble(val));
                            }
                        } catch (Exception ignored) {}
                    })
                    .show();
        }
    }
}