package com.example.pawxel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

import java.util.Objects;

public class HowToPlay extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.how_to_play);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ScrollView scrollView = findViewById(R.id.scrollView);
        ImageView arrowDown = findViewById(R.id.scrollArrowDown);
        ImageView arrowUp = findViewById(R.id.scrollArrowUp);


        arrowDown.startAnimation(AnimationUtils.loadAnimation(this, R.anim.arrow_bounce));
        arrowUp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.arrow_bounce));
        arrowUp.setVisibility(View.GONE);


        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            int contentHeight = scrollView.getChildAt(0).getHeight();
            int scrollViewHeight = scrollView.getHeight();

            int threshold = 20;

            if (scrollY <= threshold) {

                arrowDown.setVisibility(View.VISIBLE);
                arrowDown.startAnimation(AnimationUtils.loadAnimation(this, R.anim.arrow_bounce));
                arrowUp.clearAnimation();
                arrowUp.setVisibility(View.GONE);
            } else if (scrollY + scrollViewHeight >= contentHeight - threshold) {

                arrowUp.setVisibility(View.VISIBLE);
                arrowUp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.arrow_bounce));
                arrowDown.clearAnimation();
                arrowDown.setVisibility(View.GONE);
            } else {

                arrowDown.clearAnimation();
                arrowDown.setVisibility(View.GONE);
                arrowUp.clearAnimation();
                arrowUp.setVisibility(View.GONE);
            }

        });

        // Scroll behaviors on click
        arrowUp.setOnClickListener(v -> scrollView.smoothScrollTo(0, 0));
        arrowDown.setOnClickListener(v -> scrollView.smoothScrollBy(0, 300));

        // Navigation buttons
        Button backToStoryButton = findViewById(R.id.backToStoryButton);
        Button goToRoomButton = findViewById(R.id.goToRoomButton);

        backToStoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(HowToPlay.this, Welcome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        goToRoomButton.setOnClickListener(v -> {
            Intent intent = new Intent(HowToPlay.this, Room.class);
            startActivity(intent);
        });
    }
}
