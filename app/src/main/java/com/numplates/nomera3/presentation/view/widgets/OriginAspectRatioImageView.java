package com.numplates.nomera3.presentation.view.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.numplates.nomera3.R;


//import com.azimolabs.maskformatter.MaskFormatter;

/**
 * Created by artem on 08.06.18
 */
public class OriginAspectRatioImageView extends androidx.appcompat.widget.AppCompatImageView {

    private float aspectRatio;

    public OriginAspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OriginAspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.OriginAspectRatioImageView, 0, 0);
        try {
            aspectRatio = typedArray.getFloat(
                    R.styleable.OriginAspectRatioImageView_aspectRatio, aspectRatio);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(Math.round(widthSize*aspectRatio), MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }


}
