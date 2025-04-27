package com.meera.core.utils.graphics;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class for work with images
 * (read EXIF and rotate image)
 */
public class ExifUtils {

    /**
     * Rotate bitmap depends on orientation
     * @param src       - source image path
     * @param bitmap    - source bitmap
     * @return          - returned bitmap
     */
    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            int orientation = getExifOrientation(src);

            if (orientation == 1) { return bitmap; }

            Matrix matrix = new Matrix();
            switch (orientation) {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * Rotate image in normal position
     * @param src
     * @return      - rotate angle on degrees
     */
    public static float setNormalOrientation(File src) {
        float angle = 0.0f;
        try {
            int orientation = getExifOrientation(src.getAbsolutePath());
            if (orientation == 1) {
                return angle;
            }
            switch (orientation) {
                case 3: angle = 180.0f; break;
                case 6: angle = 90.0f; break;
                case 8: angle = -90.0f; break;
                default: angle = 0.0f;
            }
            return angle;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0f;
    }


    /**
     * Get exif orientation
     * @param src
     * @return
     * @throws IOException
     */
    private static int getExifOrientation(String src) throws IOException {
        int orientation = 1;
        try {
            /**
             * if your are targeting only api level >= 5
             * ExifInterface exif = new ExifInterface(src);
             * orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
             */
            if (Build.VERSION.SDK_INT >= 5) {
                Class<?> exifClass = Class.forName("android.media.ExifInterface");
                Constructor<?> exifConstructor =
                    exifClass.getConstructor(String.class);
                Object exifInstance =
                    exifConstructor.newInstance(src);
                Method getAttributeInt =
                    exifClass.getMethod("getAttributeInt", String.class, int.class);
                Field tagOrientationField =
                    exifClass.getField("TAG_ORIENTATION");
                String tagOrientation = (String) tagOrientationField.get(null);
                orientation =
                    (Integer) getAttributeInt.invoke(exifInstance, new Object[] { tagOrientation, 1});
            }
        } catch (ClassNotFoundException |
            SecurityException |
            NoSuchMethodException |
            IllegalArgumentException |
            InstantiationException |
            IllegalAccessException |
            InvocationTargetException |
            NoSuchFieldException e) {
            e.printStackTrace();
        }
        return orientation;
    }

}

