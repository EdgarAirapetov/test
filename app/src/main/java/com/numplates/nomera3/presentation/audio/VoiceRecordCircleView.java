package com.numplates.nomera3.presentation.audio;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.numplates.nomera3.R;

import timber.log.Timber;

/**
 * Вьюха окружность-подложка под анимацией
 * кнопки записи
 */
public class VoiceRecordCircleView extends View {

    float startRadiusPosition;
    float endRadiusPosition = 0f;

    Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ObjectAnimator radiusAnimator;
    private float radius;

    public VoiceRecordCircleView(Context context) {
        super(context);
    }

    public VoiceRecordCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoiceRecordCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRadius(float value) {
        this.radius = value;
        invalidate();
    }


    public void startAnimation(float radiusPosition) {
        Timber.d("START----------->VoiceRec");
        this.endRadiusPosition = radiusPosition;

        Keyframe kf0 = Keyframe.ofFloat(0.0f, startRadiusPosition);
        Keyframe kf1 = Keyframe.ofFloat(1.0f, endRadiusPosition);

        PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("radius", kf0, kf1);
        radiusAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvhRotation);
        radiusAnimator.setInterpolator(new DecelerateInterpolator());
        radiusAnimator.setDuration(130);
        radiusAnimator.start();

        startRadiusPosition = endRadiusPosition;
    }

    public void stopAnimation() {
        endRadiusPosition = 0f;
        if (radiusAnimator != null) {
            Timber.e("STOP=>VoiceRec");
            radiusAnimator.setupEndValues();
            radiusAnimator.cancel();
            radiusAnimator.removeAllListeners();
            radiusAnimator = null;
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        circlePaint.setColor(getContext().getResources().getColor(R.color.purple_voice_message_recrd));
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, circlePaint);
    }
}
