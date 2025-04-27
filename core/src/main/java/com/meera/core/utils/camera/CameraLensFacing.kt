package com.meera.core.utils.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.IntDef


@Retention(AnnotationRetention.BINARY)
@IntDef(value = [CameraCharacteristics.LENS_FACING_FRONT, CameraCharacteristics.LENS_FACING_BACK])
annotation class CameraLensFacing
