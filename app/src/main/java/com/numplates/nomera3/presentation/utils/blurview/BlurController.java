package com.numplates.nomera3.presentation.utils.blurview;

import android.graphics.Canvas;

interface BlurController extends BlurViewFacade {

    float DEFAULT_SCALE_FACTOR = 8f;
    float DEFAULT_BLUR_RADIUS = 16f;

    /**
     * Draws blurred content on given canvas
     *
     * @return true if BlurView should proceed with drawing itself and its children
     */
    boolean draw(Canvas canvas);

    /**
     * Must be used to notify Controller when BlurView's size has changed
     */
    void updateBlurViewSize();

    /**
     * Frees allocated resources
     */
    void destroy();
}
