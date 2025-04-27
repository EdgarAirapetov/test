package com.numplates.nomera3.presentation.view.utils.zoomy;


import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * Created by Álvaro Blanco Cabrero on 12/02/2017.
 * Zoomy.
 */
class ZoomableTouchListener implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {

    private static final int STATE_IDLE = 0;
    private static final int STATE_POINTER_DOWN = 1;
    private static final int STATE_ZOOMING = 2;

    private static final float MIN_SCALE_FACTOR = 0.75f;
    private static final float MAX_SCALE_FACTOR = 5f;
    private static final float DEFAULT_EVENT_PRESSURE = 1;
    private static final float DEFAULT_EVENT_SIZE = 1;
    private static final float DEFAULT_EVENT_COORDS_DIFFERENCE = 1;
    private final CanPerformZoom mCanPerformZoom;

    private int pivotX;
    private int pivotY;

    private final double mAspectRatio;
    private final TapListener mTapListener;
    private final LongPressListener mLongPressListener;
    private final DoubleTapListener mDoubleTapListener;
    private boolean longTapForZoomEnabled;
    private int mState = STATE_IDLE;
    private final TargetContainer mTargetContainer;
    private final View mTarget;
    private final View mTargetDuplicate;
    private View mZoomableView;
    private View noClickView;
    private View mShadow;
    private Double mShiftY;
    private Double mShiftX;
    private final ScaleGestureDetector mScaleGestureDetector;
    private final GestureDetector mGestureDetector;
    private final GestureDetector.SimpleOnGestureListener mGestureListener =
        new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (mTapListener != null && !isLongTapProcessing) mTapListener.onTap(mTarget);
                return true;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                if (e.getPointerCount() > 1) return;
                if (mLongPressListener != null) mLongPressListener.onLongPress(mTarget);
                if (longTapForZoomEnabled) generateAndSendTouchEventForLongTap();
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (mDoubleTapListener != null) mDoubleTapListener.onDoubleTap(mTarget);
                return true;
            }
        };
    private float mScaleFactor = 1f;
    private PointF mCurrentMovementMidPoint = new PointF();
    private PointF mInitialPinchMidPoint = new PointF();
    private Point mTargetViewCords = new Point();
    private float startTouchX = 0;
    private float startTouchY = 0;
    private boolean isLongTapProcessing = false;
    private boolean mAnimatingZoomEnding = false;
    private final Interpolator mEndZoomingInterpolator;
    private final ZoomyConfig mConfig;
    private final ZoomListener mZoomListener;
    private final Runnable mEndingZoomAction = new Runnable() {
        @Override
        public void run() {
            removeFromDecorView(noClickView);
            removeFromDecorView(mShadow);
            removeFromDecorView(mZoomableView);
            mTarget.setVisibility(View.VISIBLE);
            mZoomableView = null;
            noClickView = null;
            mCurrentMovementMidPoint = new PointF();
            mInitialPinchMidPoint = new PointF();
            mAnimatingZoomEnding = false;
            isLongTapProcessing = false;
            mState = STATE_IDLE;

            if (mZoomListener != null) mZoomListener.onViewEndedZooming(mTarget);

            if (mConfig.isImmersiveModeEnabled()) showSystemUI();
        }
    };

    private final View.OnLayoutChangeListener mLayoutListener = (view, i, i1, i2, i3, i4, i5, i6, i7) -> {
        endZoomingView();
    };


    ZoomableTouchListener(
        TargetContainer targetContainer,
        View view,
        View duplicate,
        Double shiftY,
        Double shiftX,
        ZoomyConfig config,
        Interpolator interpolator,
        ZoomListener zoomListener,
        TapListener tapListener,
        LongPressListener longPressListener,
        boolean longTapForZoomEnabled,
        DoubleTapListener doubleTapListener,
        Double aspectRatio,
        @NonNull
        CanPerformZoom canPerformZoom
    ) {
        this.mTargetContainer = targetContainer;
        this.mTarget = view;
        this.mTargetDuplicate = duplicate;
        this.mShiftY = shiftY;
        this.mShiftX = shiftX;
        this.mConfig = config;
        this.mEndZoomingInterpolator = interpolator != null
            ? interpolator : new AccelerateDecelerateInterpolator();
        this.mScaleGestureDetector = new ScaleGestureDetector(view.getContext(), this);
        this.mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
        this.mZoomListener = zoomListener;
        this.mTapListener = tapListener;
        this.mLongPressListener = longPressListener;
        this.mDoubleTapListener = doubleTapListener;
        this.mAspectRatio = aspectRatio;
        this.mCanPerformZoom = canPerformZoom;
        this.longTapForZoomEnabled = longTapForZoomEnabled;
    }

    public void endZoom() {
        mEndingZoomAction.run();
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        if (mAnimatingZoomEnding) return true;
        if (ev.getPointerCount() > 2) {
            mEndingZoomAction.run();
            return true;
        }
        startTouchX = ev.getX();
        startTouchY = ev.getY();
        mScaleGestureDetector.onTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                switch (mState) {
                    case STATE_IDLE:
                        mState = STATE_POINTER_DOWN;
                        break;
                    case STATE_POINTER_DOWN:
                        if (!canPerformZoom()) break;
                        mState = STATE_ZOOMING;

                        if (!longTapForZoomEnabled || !isLongTapProcessing) {
                            MotionUtils.midPointOfEvent(mInitialPinchMidPoint, ev);
                        } else {
                            mInitialPinchMidPoint.set(ev.getX(), ev.getY());
                        }

                        MotionEvent.PointerCoords pointerCoords1 = new MotionEvent.PointerCoords();
                        ev.getPointerCoords(0, pointerCoords1);

                        MotionEvent.PointerCoords pointerCoords2 = new MotionEvent.PointerCoords();
                        ev.getPointerCoords(1, pointerCoords2);

                        int[] twoPointCenter = new int[]{
                            (int) ((pointerCoords2.x + pointerCoords1.x) / 2),
                            (int) ((pointerCoords2.y + pointerCoords1.y) / 2)
                        };

                        pivotX = twoPointCenter[0];
                        pivotY = twoPointCenter[1];
                        startZoomingView(mTarget);
                        break;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mState == STATE_ZOOMING) {
                    if (!longTapForZoomEnabled || !isLongTapProcessing) {
                        MotionUtils.midPointOfEvent(mCurrentMovementMidPoint, ev);
                    } else {
                        mCurrentMovementMidPoint.set(ev.getX(), ev.getY());
                    }
                    //because our initial pinch could be performed in any of the view edges,
                    //we need to substract this difference and add system bars height
                    //as an offset to avoid an initial transition jump
                    mCurrentMovementMidPoint.x -= mInitialPinchMidPoint.x;
                    mCurrentMovementMidPoint.y -= mInitialPinchMidPoint.y;
                    //because previous function returns the midpoint for relative X,Y coords,
                    //we need to add absolute view coords in order to ensure the correct position
                    mCurrentMovementMidPoint.x += mTargetViewCords.x;
                    mCurrentMovementMidPoint.y += mTargetViewCords.y;
                    float x = mCurrentMovementMidPoint.x;
                    float y = mCurrentMovementMidPoint.y;
                    mZoomableView.setX(x + getZoomableViewOffsetX(mZoomableView.getWidth()));
                    mZoomableView.setY(y + getZoomableViewOffsetY(mZoomableView.getHeight()));
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                switch (mState) {
                    case STATE_ZOOMING:
                        endZoomingView();
                        break;
                    case STATE_POINTER_DOWN:
                        mState = STATE_IDLE;
                        break;
                }
                break;
        }
        return true;
    }

    private int getZoomableViewOffsetY(int zoomableViewHeight) {
        return -((int) (((zoomableViewHeight - mTarget.getHeight()) * mShiftY)));
    }

    private int getZoomableViewOffsetX(int zoomableViewWidth) {
        return -((int) (((zoomableViewWidth - mTarget.getWidth()) * mShiftX)));
    }

    private void endZoomingView() {
        if (mConfig.isZoomAnimationEnabled()) {
            mAnimatingZoomEnding = true;
            mZoomableView.animate()
                .x(mTargetViewCords.x + getZoomableViewOffsetX(mZoomableView.getWidth()))
                .y(mTargetViewCords.y + getZoomableViewOffsetY(mZoomableView.getHeight()))
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(mEndingZoomAction);
        } else {
            mEndingZoomAction.run();
        }
    }

    private boolean canPerformZoom() {
        return mCanPerformZoom.canZoom();
    }

    private void startZoomingView(View view) {
        //show the view in the same coords
        mTargetViewCords = ViewUtils.getViewAbsoluteCords(view);
        int zoomableViewWidth;
        int zoomableViewHeight;
        if (mTargetDuplicate == null) {
            // If there is no custom view defined for zooming, simply make a snapshot of the current
            // target and zoom that.
            zoomableViewWidth = mTarget.getWidth();
            zoomableViewHeight = mTarget.getHeight();
            ImageView targetDup = new ImageView(mTarget.getContext());
            targetDup.setLayoutParams(new ViewGroup.LayoutParams(zoomableViewWidth, zoomableViewHeight));
            targetDup.setImageBitmap(ViewUtils.getBitmapFromView(view));
            mZoomableView = targetDup;
        } else {
            // Otherwise, zoom the custom duplicated target
            mZoomableView = mTargetDuplicate;

            if (mAspectRatio > 0) {
                double targetRatio = 1.0 * mTarget.getWidth() / mTarget.getHeight();
                if (targetRatio < mAspectRatio) {
                    zoomableViewWidth = (int) (mTarget.getHeight() * mAspectRatio);
                    zoomableViewHeight = mTarget.getHeight();
                    int diffX = (zoomableViewWidth - mTarget.getWidth()) / 2;
                    mTargetViewCords.x -= diffX;
                    pivotX += diffX;
                } else {
                    zoomableViewWidth = mTarget.getWidth();
                    zoomableViewHeight = (int) (mTarget.getWidth() / mAspectRatio);
                    int diffY = (zoomableViewHeight - mTarget.getHeight()) / 2;
                    mTargetViewCords.y -= diffY;
                    pivotY += diffY;
                }
            } else {
                zoomableViewWidth = mTarget.getWidth();
                zoomableViewHeight = mTarget.getHeight();
            }
            mZoomableView.setLayoutParams(new ViewGroup.LayoutParams(zoomableViewWidth, zoomableViewHeight));
        }

        mZoomableView.setX(mTargetViewCords.x + getZoomableViewOffsetX(zoomableViewWidth));
        mZoomableView.setY(mTargetViewCords.y + getZoomableViewOffsetY(zoomableViewHeight));

        if (mShadow == null) mShadow = new View(mTarget.getContext());
        mShadow.setBackgroundResource(0);
        mShadow.setAlpha(1f);

        noClickView = new View(mTarget.getContext());
        noClickView.setBackgroundColor(Color.TRANSPARENT);
        noClickView.setClickable(true);
        noClickView.setFocusable(true);

        addToDecorView(noClickView);
        addToDecorView(mShadow);
        addToDecorView(mZoomableView);

        //trick for simulating the view is getting out of his parent
        disableParentTouch(mTarget.getParent());
        mTarget.setVisibility(View.INVISIBLE);

        if (mConfig.isImmersiveModeEnabled()) hideSystemUI();
        if (mZoomListener != null) mZoomListener.onViewStartedZooming(mTarget);
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mZoomableView == null) return false;

        mScaleFactor *= detector.getScaleFactor();

        // Don't let the object get too large.
        mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));

        mZoomableView.setPivotX(pivotX - getZoomableViewOffsetX(mZoomableView.getWidth()));
        mZoomableView.setPivotY(pivotY - getZoomableViewOffsetY(mZoomableView.getHeight()));
        mZoomableView.setScaleX(mScaleFactor);
        mZoomableView.setScaleY(mScaleFactor);
        obscureDecorView(mScaleFactor);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return mZoomableView != null;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mScaleFactor = 1f;
    }

    private void addToDecorView(View v) {
        mTargetContainer.getDecorView().addView(v);
    }

    private void removeFromDecorView(View v) {
        mTargetContainer.getDecorView().removeView(v);
    }

    private void addLayoutListener(View v) {
        v.addOnLayoutChangeListener(mLayoutListener);
    }

    private void removeLayoutListener(View v) {
        v.removeOnLayoutChangeListener(mLayoutListener);
    }

    private void obscureDecorView(float factor) {
        //normalize value between 0 and 1
        float normalizedValue = (factor - MIN_SCALE_FACTOR) / (MAX_SCALE_FACTOR - MIN_SCALE_FACTOR);
        normalizedValue = Math.min(0.75f, normalizedValue * 2);
        int obscure = Color.argb((int) (normalizedValue * 255), 0, 0, 0);
        mShadow.setBackgroundColor(obscure);
    }

    private void hideSystemUI() {
//        mTargetContainer.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                | View.SYSTEM_UI_FLAG_FULLSCREEN); // hide status ba;
    }

    private void showSystemUI() {
        //mTargetContainer.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void disableParentTouch(ViewParent view) {
        view.requestDisallowInterceptTouchEvent(true);
        if (view.getParent() != null) disableParentTouch((view.getParent()));
    }

    private double getDistance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private void generateAndSendTouchEventForLongTap() {
        long time = SystemClock.uptimeMillis();
        int action = MotionEvent.ACTION_DOWN;
        float x = startTouchX;
        float y = startTouchY;

        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[2];
        MotionEvent.PointerCoords firstCoords = new MotionEvent.PointerCoords();
        firstCoords.x = x;
        firstCoords.y = y;
        firstCoords.pressure = DEFAULT_EVENT_PRESSURE;
        firstCoords.size = DEFAULT_EVENT_SIZE;

        MotionEvent.PointerCoords secondCoords = new MotionEvent.PointerCoords();
        secondCoords.x = x + DEFAULT_EVENT_COORDS_DIFFERENCE;
        secondCoords.y = y + DEFAULT_EVENT_COORDS_DIFFERENCE;
        secondCoords.pressure = DEFAULT_EVENT_PRESSURE;
        secondCoords.size = DEFAULT_EVENT_SIZE;

        pointerCoords[0] = firstCoords;
        pointerCoords[1] = secondCoords;

        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[2];
        MotionEvent.PointerProperties firstProperties = new MotionEvent.PointerProperties();
        firstProperties.id = 0;
        firstProperties.toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerProperties secondProperties = new MotionEvent.PointerProperties();
        secondProperties.id = 1;
        secondProperties.toolType = MotionEvent.TOOL_TYPE_FINGER;

        properties[0] = firstProperties;
        properties[1] = secondProperties;

        MotionEvent event = MotionEvent.obtain(time, time, action, 2, properties, pointerCoords,
            0, 0, 1.0f, 1.0f, 0, 0, 0, 0);
        isLongTapProcessing = true;

        onTouch(mTarget, event);
        event.recycle();
    }
}
