package com.example.Whack_a_Pikachu;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class GamePlayer extends ConstraintLayout implements Serializable {

    private TextView name_view;
    private TextView score_view;
    private ImageButton location_btn;
    private LatLng location;

    public GamePlayer(Context context) {
        super(context);
    }

    public GamePlayer(Context context, String name, int score, LatLng location) {
        super(context);
        this.setId(ViewCompat.generateViewId());
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setMargins(this, 10, 10, 10 ,10);

        this.location = location;

        // Name View
        name_view = new TextView(context);
        name_view.setId(ViewCompat.generateViewId());
        name_view.setText(name);
        name_view.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        // Score View
        score_view = new TextView(context);
        score_view.setId(ViewCompat.generateViewId());
        score_view.setText(String.valueOf(score));
        score_view.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
        // Location Button
        location_btn = new ImageButton(context);
        location_btn.setId(ViewCompat.generateViewId());
        location_btn.setBackgroundResource(R.color.transparent);
        location_btn.setImageResource(R.drawable.ic_location_on);
        this.addView(name_view);
        this.addView(score_view);
        this.addView(location_btn);
        applyConstraints();
    }

    public ImageButton getLocationBtn() { return location_btn; }

    public LatLng getLocation() { return location; }

    public String getName() { return name_view.getText().toString(); }

    private void applyConstraints() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        // Name
        constraintSet.connect(name_view.getId(), ConstraintSet.START, this.getId(), ConstraintSet.START);
        constraintSet.connect(name_view.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP);
        constraintSet.connect(name_view.getId(), ConstraintSet.BOTTOM, this.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(name_view.getId(), ConstraintSet.END, score_view.getId(), ConstraintSet.END);
        constraintSet.setHorizontalBias(name_view.getId(), 0.5f);
        // Score
        constraintSet.connect(score_view.getId(), ConstraintSet.BOTTOM, name_view.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(score_view.getId(), ConstraintSet.END, location_btn.getId(), ConstraintSet.START);
        constraintSet.connect(score_view.getId(), ConstraintSet.START, name_view.getId(), ConstraintSet.END);
        constraintSet.connect(score_view.getId(), ConstraintSet.TOP, name_view.getId(), ConstraintSet.TOP);
        constraintSet.setHorizontalBias(score_view.getId(), 0.5f);
        // Location Button
        constraintSet.connect(location_btn.getId(), ConstraintSet.BOTTOM, score_view.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(location_btn.getId(), ConstraintSet.END, this.getId(), ConstraintSet.END);
        constraintSet.connect(location_btn.getId(), ConstraintSet.START, score_view.getId(), ConstraintSet.END);
        constraintSet.connect(location_btn.getId(), ConstraintSet.TOP, score_view.getId(), ConstraintSet.TOP);
        constraintSet.setHorizontalBias(location_btn.getId(), 0.5f);
        constraintSet.applyTo(this);
    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}
