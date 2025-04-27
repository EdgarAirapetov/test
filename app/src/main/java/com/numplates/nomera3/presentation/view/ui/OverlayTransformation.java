package com.numplates.nomera3.presentation.view.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.numplates.nomera3.R;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import androidx.annotation.NonNull;

/**
 * Created by javiergonzalezcabezas on 2/4/16.
 *
 * Multilayer transparent rounded corneres
 */
public class OverlayTransformation extends BitmapTransformation {
    private static final String ID = "com.bumptech.glide.transformations.FillSpace";
    private static final byte[] ID_BYTES = ID.getBytes(StandardCharsets.UTF_8);
    private final Context context;

    public OverlayTransformation(Context context) {
        this.context = context;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }



    @Override
    public Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
//        if (toTransform.getWidth() == outWidth && toTransform.getHeight() == outHeight) {
//            return toTransform;
//        }

        return applyOverlay(context, toTransform);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OverlayTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }


    public Bitmap applyOverlay(Context context, Bitmap sourceImage){
        Bitmap bitmap = null;
        try{
            Resources r = context.getResources();
            Drawable imageAsDrawable = new BitmapDrawable(r, sourceImage);
            Drawable[] layers = new Drawable[2];
            layers[0] = imageAsDrawable;

            int[] colors = {context.getResources().getColor(R.color.ui_black),
                    context.getResources().getColor(R.color.colorTransparent),
                    context.getResources().getColor(R.color.ui_black)};

            layers[1] = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            bitmap = drawableToBitmap(layerDrawable);
        }catch (Exception ex){}
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
