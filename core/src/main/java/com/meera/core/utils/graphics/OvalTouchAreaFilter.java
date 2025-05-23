package com.meera.core.utils.graphics;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

public class OvalTouchAreaFilter implements View.OnTouchListener {

    private boolean mIgnoreCurrentGesture;

    public OvalTouchAreaFilter() {
        mIgnoreCurrentGesture = false;
    }

    public boolean isInTouchArea(View view, float x, float y) {
        try {
            int w = view.getWidth();
            int h = view.getHeight();
            if (w <= 0 || h <= 0)
                return false;
            float xhat = 2 * x / w - 1;
            float yhat = 2 * y / h - 1;
            return (xhat * xhat + yhat * yhat <= 1);
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        try {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                mIgnoreCurrentGesture = !this.isInTouchArea(view, event.getX(), event.getY());
                return mIgnoreCurrentGesture;
            }
            boolean ignoreCurrentGesture = mIgnoreCurrentGesture;
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                mIgnoreCurrentGesture = false;
            return ignoreCurrentGesture;
        } catch (Exception e){
            return true;
        }
    }
}
