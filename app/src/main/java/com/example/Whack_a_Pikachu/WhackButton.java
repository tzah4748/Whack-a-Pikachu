package com.example.Whack_a_Pikachu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

public class WhackButton extends AppCompatImageView {
    enum States {EMPTY, GOOD, BAD}

    public States state;
    public int myIndex;
    public TextView givenScoreView;
    private static final int VISIBLE = 255;
    private static final int INVISIBLE = 0;

    public WhackButton(Context context, int buttonWidth, int buttonHeight, int myIndex, TextView givenScoreView) {
        super(context);
        this.setId(View.generateViewId());
        this.myIndex = myIndex;
        this.givenScoreView = givenScoreView;
        this.setLayoutParams(new ViewGroup.LayoutParams(buttonWidth, buttonHeight));
        this.setCropToPadding(true);
        this.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.setStateEmpty();
    }

    public WhackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WhackButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setStateEmpty() {
        state = States.EMPTY;
        this.setImageAlpha(INVISIBLE);
    }

    void setStateGood() {
        state = States.GOOD;
        Glide.with(this).load(R.drawable.whack_pikachu).into(this);
        this.setImageAlpha(VISIBLE);
    }

    void setStateBad() {
        state = States.BAD;
        Glide.with(this).load(R.drawable.whack_haunter).into(this);
        this.setImageAlpha(VISIBLE);
    }

    @Override
    public String toString() {
        return String.valueOf(myIndex);
    }
}
