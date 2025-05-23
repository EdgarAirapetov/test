package com.numplates.nomera3.presentation.view.utils.zoomy;

import android.app.Dialog;
import android.view.ViewGroup;

/**
 * Created by Álvaro Blanco Cabrero on 01/05/2017.
 * Zoomy.
 */

public class DialogContainer implements TargetContainer {

    private final Dialog mDialog;

    DialogContainer(Dialog dialog) {
        this.mDialog = dialog;
    }

    @Override
    public final ViewGroup getDecorView() {
        return mDialog.getWindow() != null ? (ViewGroup) mDialog.getWindow().getDecorView() : null;
    }
}
