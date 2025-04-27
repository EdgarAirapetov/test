package com.numplates.nomera3.modules.newroads.util;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * There is a bug with TouchDelegate where ancestor views can get into an awkward state after
 * a delegate view has been actioned upon by the touch delegate.
 * <p>
 * This class is a direct copy of https://github.com/android/platform_frameworks_base/blob/master/core/java/android/view/TouchDelegate.java
 * but with the fix.
 * <p>
 * See: https://code.google.com/p/android/issues/detail?id=36445 for details.
 */
public class HackedTouchDelegate extends TouchDelegate {
    /**
     * View that should receive forwarded touch events
     */
    private final View mDelegateView;

    /**
     * Bounds in local coordinates of the containing view that should be mapped to the delegate
     * view. This rect is used for initial hit testing.
     */
    private final Rect mBounds;

    /**
     * mBounds inflated to include some slop. This rect is to track whether the motion events
     * should be considered to be be within the delegate view.
     */
    private final Rect mSlopBounds;

    /**
     * True if the delegate had been targeted on a down event (intersected mBounds).
     */
    private boolean mDelegateTargeted;

    /**
     * The touchable region of the View extends above its actual extent.
     */
    public static final int ABOVE = 1;

    /**
     * The touchable region of the View extends below its actual extent.
     */
    public static final int BELOW = 2;

    /**
     * The touchable region of the View extends to the left of its
     * actual extent.
     */
    public static final int TO_LEFT = 4;

    /**
     * The touchable region of the View extends to the right of its
     * actual extent.
     */
    public static final int TO_RIGHT = 8;

    private final int mSlop;

    /**
     * Constructor
     *
     * @param bounds       Bounds in local coordinates of the containing view that should be mapped to
     *                     the delegate view
     * @param delegateView The view that should receive motion events
     */
    public HackedTouchDelegate(Rect bounds, View delegateView) {
        super(bounds, delegateView); // Doesn't actually matter since we are overriding the entire implementation

        mBounds = bounds;

        mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
        mSlopBounds = new Rect(bounds);
        mSlopBounds.inset(-mSlop, -mSlop);
        mDelegateView = delegateView;
    }

    /**
     * Will forward touch events to the delegate view if the event is within the bounds
     * specified in the constructor.
     *
     * @param event The touch event to forward
     * @return True if the event was forwarded to the delegate, false otherwise.
     */
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean sendToDelegate = false;
        boolean hit = true;
        boolean handled = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Rect bounds = mBounds;

                if (bounds.contains(x, y)) {
                    mDelegateTargeted = true;
                    sendToDelegate = true;
                } else { // NOTE: This else block reflects the only effective change to this class.
                    mDelegateTargeted = false;
                    sendToDelegate = false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                sendToDelegate = mDelegateTargeted;
                if (sendToDelegate) {
                    Rect slopBounds = mSlopBounds;
                    if (!slopBounds.contains(x, y)) {
                        hit = false;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                sendToDelegate = mDelegateTargeted;
                mDelegateTargeted = false;
                break;
        }
        if (sendToDelegate) {
            final View delegateView = mDelegateView;

            if (hit) {
                // Offset event coordinates to be inside the target view
                event.setLocation(delegateView.getWidth() / 2, delegateView.getHeight() / 2);
            } else {
                // Offset event coordinates to be outside the target view (in case it does
                // something like tracking pressed state)
                int slop = mSlop;
                event.setLocation(-(slop * 2), -(slop * 2));
            }
            handled = delegateView.dispatchTouchEvent(event);
        }
        return handled;
    }
}
