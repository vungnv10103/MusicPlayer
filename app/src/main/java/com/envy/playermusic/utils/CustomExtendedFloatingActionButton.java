package com.envy.playermusic.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CustomExtendedFloatingActionButton extends FloatingActionButton {
    private boolean isExtended = false;
    private TextView textView;

    public CustomExtendedFloatingActionButton(@NonNull Context context) {
        super(context);
    }

    public CustomExtendedFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomExtendedFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void extend() {
        if (!isExtended) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 16, 0); // Add margins if needed
            textView = new TextView(getContext());
            textView.setLayoutParams(layoutParams);
            textView.setText(""); // Set the desired text

            ViewGroup parent = (ViewGroup) getParent();
            parent.addView(textView); // Add the TextView to the parent ViewGroup

            isExtended = true;
        }
    }


    public void shrink() {
        if (isExtended) {
            // Shrink the FloatingActionButton
            ViewGroup parent = (ViewGroup) getParent();
            parent.removeView(textView); // Remove the TextView from the parent ViewGroup
            isExtended = false;
        }
    }
}
