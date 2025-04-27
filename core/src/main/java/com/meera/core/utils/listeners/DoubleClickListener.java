package com.meera.core.utils.listeners;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {

    // The time in which the second tap should be done in order to qualify as
    // a double click
    private static final long DEFAULT_QUALIFICATION_SPAN = 250;
    private long doubleClickQualificationSpanInMillis;
    private long timestampLastClick;
    private Boolean doubleClicked = false;

    private Runnable task = () -> {
        if (!doubleClicked) {
            onViewClick();
        }else doubleClicked = false;
    };

    public DoubleClickListener() {
        doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
        timestampLastClick = 0;
    }

    public DoubleClickListener(long doubleClickQualificationSpanInMillis) {
        this.doubleClickQualificationSpanInMillis = doubleClickQualificationSpanInMillis;
        timestampLastClick = 0;
    }

    @Override
    public void onClick(View v) {
        if((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
            doubleClicked = true;
            onDoubleClick();
        }
        timestampLastClick = SystemClock.elapsedRealtime();

        Handler handler = new Handler();
        handler.removeCallbacks(task);
        handler.postDelayed(task, DEFAULT_QUALIFICATION_SPAN + 10);
    }

    public abstract void onDoubleClick();

    public void onViewClick(){

    }

}
