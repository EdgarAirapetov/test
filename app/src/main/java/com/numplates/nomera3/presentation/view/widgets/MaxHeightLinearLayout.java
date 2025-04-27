package com.numplates.nomera3.presentation.view.widgets;

import static com.meera.core.extensions.CommonKt.dpToPx;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.numplates.nomera3.R;

/**
 * Created by artem on 08.06.18
 */
public class MaxHeightLinearLayout extends LinearLayout {

    private float maxHeight;

    public MaxHeightLinearLayout(Context context) {
        super(context);
        this.maxHeight = dpToPx(160);
    }

    public MaxHeightLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaxHeightLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.MaxHeightLinearLayout, 0, 0);
        try {
            maxHeight = typedArray.getDimension(
                    R.styleable.MaxHeightLinearLayout_maxHeight, maxHeight);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

//        int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(Math.min(heightSize, (int)maxHeight), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }


}
