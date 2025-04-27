package com.numplates.nomera3.presentation.view.utils.zoomy;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Álvaro Blanco Cabrero on 12/02/2017.
 * Zoomy.
 */

public class Zoomy {

    private static final ZoomyConfig mDefaultConfig = new ZoomyConfig();

    private Zoomy() {
    }

    public static class Builder {

        private boolean mDisposed = false;

        private ZoomyConfig mConfig;
        private final TargetContainer mTargetContainer;
        private View mTargetView;
        private View mTargetDuplicate;
        private ZoomListener mZoomListener;
        private Interpolator mZoomInterpolator;
        private TapListener mTapListener;
        private LongPressListener mLongPressListener;
        private CanPerformZoom mCanPerformZoom;
        private DoubleTapListener mdDoubleTapListener;
        private Double mAspectRatio = -1.0;
        private Double mShiftY = 0.0;
        private Double mShiftX = 0.0;
        private boolean enableLongTapForZoom = false;
        private ZoomableTouchListener listener;

        public Builder(Activity activity) {
            this.mTargetContainer = new ActivityContainer(activity);
        }

        public Builder(Dialog dialog) {
            this.mTargetContainer = new DialogContainer(dialog);
        }

        public Builder(DialogFragment dialogFragment) {
            this.mTargetContainer = new DialogFragmentContainer(dialogFragment);
        }

        public Builder target(View target) {
            this.mTargetView = target;
            return this;
        }

        public Builder setShiftY(Double shiftY) {
            if (shiftY != null) this.mShiftY = shiftY;
            return this;
        }

        public Builder setShiftX(Double shiftX) {
            if (shiftX != null) this.mShiftX = shiftX;
            return this;
        }

        public Builder setTargetDuplicate(View duplicate) {
            this.mTargetDuplicate = duplicate;
            return this;
        }

        public Builder aspectRatio(@Nullable Double aspectRatio) {
            checkNotDisposed();
            if (aspectRatio != null && aspectRatio > 0) {
                this.mAspectRatio = aspectRatio;
            }
            return this;
        }

        public Builder animateZooming(boolean animate) {
            checkNotDisposed();
            if (mConfig == null) mConfig = new ZoomyConfig();
            this.mConfig.setZoomAnimationEnabled(animate);
            return this;
        }

        public Builder enableImmersiveMode(boolean enable) {
            checkNotDisposed();
            if (mConfig == null) mConfig = new ZoomyConfig();
            this.mConfig.setImmersiveModeEnabled(enable);
            return this;
        }

        public Builder interpolator(Interpolator interpolator) {
            checkNotDisposed();
            this.mZoomInterpolator = interpolator;
            return this;
        }

        public Builder zoomListener(ZoomListener listener) {
            checkNotDisposed();
            this.mZoomListener = listener;
            return this;
        }

        public Builder tapListener(TapListener listener) {
            checkNotDisposed();
            this.mTapListener = listener;
            return this;
        }

        public Builder longPressListener(LongPressListener listener) {
            checkNotDisposed();
            this.mLongPressListener = listener;
            return this;
        }

        public Builder doubleTapListener(DoubleTapListener listener) {
            checkNotDisposed();
            this.mdDoubleTapListener = listener;
            return this;
        }

        public Builder canPerformZoom(CanPerformZoom listener) {
            checkNotDisposed();
            this.mCanPerformZoom = listener;
            return this;
        }

        public Builder enableLongPressForZoom(boolean enable) {
            this.enableLongTapForZoom = enable;
            return this;
        }

        public void register() {
            checkNotDisposed();
            if (mConfig == null) mConfig = mDefaultConfig;
            if (mTargetContainer == null)
                throw new IllegalArgumentException("Target container must not be null");
            if (mTargetView == null)
                throw new IllegalArgumentException("Target view must not be null");
            if (mCanPerformZoom == null) {
                mCanPerformZoom = () -> true;
            }
            listener = new ZoomableTouchListener(
                mTargetContainer,
                mTargetView,
                mTargetDuplicate,
                mShiftY,
                mShiftX,
                mConfig,
                mZoomInterpolator,
                mZoomListener,
                mTapListener,
                mLongPressListener,
                enableLongTapForZoom,
                mdDoubleTapListener,
                mAspectRatio,
                mCanPerformZoom
            );
            mTargetView.setOnTouchListener(listener);
            mDisposed = true;
        }

        public void endZoom() {
            if (listener != null) {
                listener.endZoom();
            }
        }

        private void checkNotDisposed() {
            if (mDisposed) throw new IllegalStateException("Builder already disposed");
        }

        public void clearResources() {
            listener = null;
            mTargetView = null;
            mZoomInterpolator = null;
            mTapListener = null;
            mZoomListener = null;
            mdDoubleTapListener = null;
            mLongPressListener = null;
            mCanPerformZoom = null;
            mTargetDuplicate = null;
        }
    }

    public interface ZoomyProvider {
        @NonNull
        Builder provideBuilder();
    }

}
