package com.numplates.nomera3.presentation.view.utils.zoomy;

import android.app.Activity;
import android.view.ViewGroup;

/**
 * Created by Álvaro Blanco Cabrero on 01/05/2017.
 * Zoomy.
 */

public class ActivityContainer implements TargetContainer{

    private final Activity mActivity;

    ActivityContainer(Activity activity){
        this.mActivity = activity;
    }

    @Override
    public ViewGroup getDecorView() {
        return (ViewGroup) mActivity.getWindow().getDecorView();
    }
}
