package com.noomeera.nmravatarssdk.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import com.caverock.androidsvg.RenderOptions
import com.google.gson.Gson
import com.noomeera.nmravatarssdk.Constants
import com.noomeera.nmravatarssdk.NMRAvatarsSDK
import com.noomeera.nmravatarssdk.R
import com.noomeera.nmravatarssdk.data.AssetsConfig
import com.noomeera.nmravatarssdk.data.AvatarState
import com.noomeera.nmravatarssdk.data.LayeredSvg
import com.noomeera.nmravatarssdk.ui.computationThreadPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.abs

class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle), SensorEventListener {

    private data class BitmapAsset(
        val bitmap: Bitmap,
        val parallaxLayer: String,
        val mask: Boolean,
        val applyMask: Boolean
    )

    private inner class SensorInterpreter {
        private val oldRotationVector = FloatArray(5)
        private val deltaVector = FloatArray(3)
        private val mRotationMatrix = FloatArray(16)
        private val mOldRotationMatrix = FloatArray(16)
        private var init = false
        private val sensitivity = 2f

        fun reset() {
            init = false
        }

        fun interpretSensorEvent(event: SensorEvent): FloatArray {
            val rotationVector = event.values
            if (!init) {
                System.arraycopy(rotationVector, 0, oldRotationVector, 0, rotationVector.size)
                init = true
            }
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, rotationVector)
            SensorManager.getRotationMatrixFromVector(mOldRotationMatrix, oldRotationVector)
            SensorManager.getAngleChange(deltaVector, mRotationMatrix, mOldRotationMatrix)
            for (i in deltaVector.indices) {
                // here deltaVector contains radian values
                deltaVector[i] = deltaVector[i] * sensitivity
            }
            System.arraycopy(rotationVector, 0, oldRotationVector, 0, rotationVector.size)
            // sensitivity * angle change (in rads)
            return deltaVector
        }
    }

    var avatarIsReadyCallback: (() -> Unit)? = null
    val avatarIsReady: Boolean
       get() {
           return avatarIsReadyInternal.get()
       }

    private val mExtraBitmapSpace = 1.15f
    private val mMaximumAngle = 0.3f
    private val mMaximumJump = 0.02f
    private val mMinimumJump = 0.0001f
    private val mInterpolationEffect = 0.75f
    private val mSpeedMultiplier = 1.5f
    private val mMovementMultiplier = 2f
    private val mMinimumMovementRedraw = 0.015f
    private val mInterpolator: Interpolator = DecelerateInterpolator()
    private val mSensorInterpreter: SensorInterpreter = SensorInterpreter()
    private var mSensorManager: SensorManager? = null
    private var mXTranslation = 0f
    private var mYTranslation = 0f
    private var mXOptimizedTranslation = 0f
    private var mYOptimizedTranslation = 0f

    private var currentWidth = 0
    private var currentHeight = 0

    var quality = 1f
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Quality cannot be 0 or less")
            }
            field = value
            mYTranslation = 0f
            mXTranslation = 0f
            changeBitmapSize(currentHeight, currentWidth)
        }

    private val mBgGradientPaint = Paint()
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(
            PorterDuff.Mode.DST_IN
        )
    }

    private val updateLock = ReentrantLock()
    private val oldBitmapsLock = ReentrantLock()

    private var layeredSvgs: List<LayeredSvg> = listOf()
    private val assetBitmaps = mutableListOf<BitmapAsset>()
    private val tmpAssetBitmaps = mutableListOf<BitmapAsset>()

    private var viewBitmap: Bitmap? = null
    private val viewBitmapLock = ReentrantLock()
    private var tmpViewBitmap: Bitmap? = null
    private var tmpViewBitmapCanvas: Canvas? = null

    // Just to be sure markToInvalidate is running in single thread
    private val invalidationLock = ReentrantLock()
    private val invalidateRequested = AtomicBoolean(false)

    private val redrawBitmapsRequested = AtomicBoolean(false)

    private val viewBitmapDrawExecutor = Executors.newSingleThreadExecutor()

    private var wasParallaxBeforeDetachedFromWindow = false

    private val assetsSet = AtomicBoolean(false)
    private val assetBitmapsDrawn = AtomicBoolean(false)
    private val avatarIsReadyInternal = AtomicBoolean(false)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.AvatarView).apply {
            quality =
                getFloat(
                    R.styleable.AvatarView_quality,
                    1f
                )
            recycle()
        }
    }

    private fun markToInvalidate() {
        invalidateRequested.set(true)
        viewBitmapDrawExecutor.execute {
            while (invalidateRequested.get()) {
                invalidationLock.withLock {
                    invalidateRequested.set(false)

                    val width = tmpViewBitmapCanvas?.width ?: 0
                    val height = tmpViewBitmapCanvas?.height ?: 0

                    if (width == 0 || height == 0)
                        return@withLock

                    mXOptimizedTranslation = mXTranslation
                    mYOptimizedTranslation = mYTranslation

                    tmpViewBitmapCanvas?.let { tmpViewBitmapCanvas ->
                        tmpViewBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        if (updateLock.isLocked) {
                            oldBitmapsLock.withLock {
                                tmpAssetBitmaps.forEach {
                                    drawBitmapAsset(tmpViewBitmapCanvas, it)
                                }
                            }
                        } else {
                            updateLock.withLock {
                                assetBitmaps.forEach {
                                    drawBitmapAsset(tmpViewBitmapCanvas, it)
                                }
                            }
                        }
                    }

                    viewBitmapLock.withLock {
                        tmpViewBitmap?.let {
                            val oldSnapshotBitmap = viewBitmap
                            viewBitmap = it.copy(Bitmap.Config.ARGB_8888, false)
                            oldSnapshotBitmap?.recycle()
                        }
                    }

                    if (assetsSet.get()) {
                        assetBitmapsDrawn.set(true)
                    }
                    postInvalidate()
                }
            }
        }
    }

    private fun drawBitmaps(width: Int, height: Int): List<BitmapAsset> {
        val bitmaps = mutableListOf<BitmapAsset>()

        val svgWidth = (width * mExtraBitmapSpace).toInt()
        val svgHeight = (height * mExtraBitmapSpace).toInt()

        val renderOptions = RenderOptions.create().apply {
            viewBox(0f, 0f, 2000f * mExtraBitmapSpace, 2000f * mExtraBitmapSpace)
        }

        layeredSvgs.sortedByDescending { it.layer }.forEach {
            if (it.mask)
                return@forEach

            val bm =
                Bitmap.createBitmap(svgWidth, svgHeight, Bitmap.Config.ARGB_8888)

            it.svg.renderToCanvas(Canvas(bm), renderOptions)

            bitmaps.add(BitmapAsset(bm, it.parallaxLayer, it.mask, it.applyMask))
        }

        layeredSvgs.sortedByDescending { it.layer }.forEach {
            if (!it.mask)
                return@forEach

            val bm =
                Bitmap.createBitmap(svgWidth, svgHeight, Bitmap.Config.ARGB_8888)

            it.svg.renderToCanvas(Canvas(bm), renderOptions)

            val newBitmaps = bitmaps.map { bmAsset ->
                if (!bmAsset.applyMask)
                    return@map bmAsset

                val newBm = Bitmap.createBitmap(bmAsset.bitmap)
                Canvas(newBm).drawBitmap(bm, 0f, 0f, maskPaint)
                bmAsset.bitmap.recycle()
                bmAsset.copy(bitmap = newBm)
            }

            bitmaps.clear()
            bitmaps.addAll(newBitmaps)
        }

        return bitmaps
    }

    private fun redrawBitmaps() {
        redrawBitmapsRequested.set(true)
        computationThreadPool.execute {
            while (redrawBitmapsRequested.get()) {
                updateLock.withLock {
                    redrawBitmapsRequested.set(false)

                    val width = (currentWidth * quality).toInt()
                    val height = (currentHeight * quality).toInt()

                    if (width == 0 || height == 0)
                        return@withLock

                    oldBitmapsLock.withLock {
                        tmpAssetBitmaps.addAll(assetBitmaps)
                        assetBitmaps.clear()
                    }

                    assetBitmaps.addAll(drawBitmaps(width, height))

                    oldBitmapsLock.withLock {
                        tmpAssetBitmaps.forEach { it.bitmap.recycle() }
                        tmpAssetBitmaps.clear()
                    }
                }
                markToInvalidate()
            }
        }
    }

    internal fun setAssets(svgAssets: List<LayeredSvg>) {
        this.layeredSvgs = svgAssets
        assetsSet.set(true)
        redrawBitmaps()
    }

    fun setState(avatarState: String) {
        avatarIsReadyInternal.set(false)
        if (!NMRAvatarsSDK.isSdkReady(context)) {
            return
        }
        val configJson = File("${NMRAvatarsSDK.getResourcesDirPath(context)}/${Constants.CONFIG_JSON_PATH}")
            .inputStream()
            .use {
                return@use it.bufferedReader().readText()
            }
        val assetsConfig = try {
            Gson().fromJson(configJson, AssetsConfig::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        val avatarStateObj = try {
            Gson().fromJson(avatarState, AvatarState::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        setAssets(avatarStateObj.allAvatarStateAssets().map {
            assetsConfig.layeredSvgsFromStateAsset(context, it)
        }.flatten())
    }

    fun setStateAsync(avatarState: String, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            setState(avatarState)
        }
    }

    fun clear() {
        setAssets(listOf())
    }

    var drawBackgroundGradient = false
        private set

    fun setDrawBackgroundGradient(draw: Boolean) {
        drawBackgroundGradient = draw
        markToInvalidate()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (mSensorManager == null) return
        val vectors = mSensorInterpreter.interpretSensorEvent(event)
        addTranslate(vectors[2], -vectors[1])
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    fun startParallaxEffect(samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_FASTEST) {
        if (context == null || mSensorManager != null) return

        // Acquires a sensor manager
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager?.let { mSensorManager ->
            mSensorInterpreter.reset()
            mSensorManager.registerListener(
                this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                samplingPeriodUs
            )
        }
    }

    fun stopParallaxEffect(resetTranslation: Boolean = false) {
        mSensorManager?.unregisterListener(this)
        mSensorManager = null
        if (resetTranslation) {
            mYTranslation = 0f
            mXTranslation = 0f
            postInvalidate()
        }
    }

    fun destroy() {
        stopParallaxEffect(false)
        setAssets(listOf())
    }

    private fun addTranslate(xArg: Float, yArg: Float) {
        var x = xArg
        var y = yArg

        // Include distance from center factor
        x *= mSpeedMultiplier - mInterpolator.getInterpolation(abs(mXTranslation / mMaximumAngle)) * mInterpolationEffect
        y *= mSpeedMultiplier - mInterpolator.getInterpolation(abs(mYTranslation / mMaximumAngle)) * mInterpolationEffect
        if (mMaximumJump > 0) {
            // Limit x jump
            if (x > mMaximumJump) {
                x = mMaximumJump
            }
            if (x < -mMaximumJump) {
                x = -mMaximumJump
            }
            // Limit y jump
            if (y > mMaximumJump) {
                y = mMaximumJump
            }
            if (y < -mMaximumJump) {
                y = -mMaximumJump
            }
        }

        if (mMinimumJump > 0) {
            if (abs(x) < mMinimumJump) x = 0f
            if (abs(y) < mMinimumJump) y = 0f
        }

        mXTranslation += x
        mYTranslation += y

        if (mXTranslation > mMaximumAngle) mXTranslation = mMaximumAngle
        if (mXTranslation < -mMaximumAngle) mXTranslation = -mMaximumAngle
        if (mYTranslation > mMaximumAngle) mYTranslation = mMaximumAngle
        if (mYTranslation < -mMaximumAngle) mYTranslation = -mMaximumAngle

        val xDelta = abs(mXTranslation - mXOptimizedTranslation)
        val yDelta = abs(mYTranslation - mYOptimizedTranslation)

        if (yDelta > mMinimumMovementRedraw || xDelta > mMinimumMovementRedraw) {
            markToInvalidate()
        }
    }

    private fun translateRectF(
        xMultiplier: Float,
        yMultiplier: Float
    ): RectF {
        val x =
            -mXOptimizedTranslation / mMaximumAngle * width.toFloat() / 2000f * xMultiplier * mMovementMultiplier * quality
        val y =
            -mYOptimizedTranslation / mMaximumAngle * height.toFloat() / 2000f * yMultiplier * mMovementMultiplier * quality
        return RectF(x, y, x, y)
    }


    private fun translateRectFForParallax(parallaxLayer: String): RectF = when (parallaxLayer) {
        "body" -> translateRectF(BODY_GROUP_X, BODY_GROUP_Y)
        "head" -> translateRectF(HEAD_GROUP_X, HEAD_GROUP_Y)
        "ears" -> translateRectF(EARS_GROUP_X, EARS_GROUP_Y)
        "face" -> translateRectF(FACE_GROUP_X, FACE_GROUP_Y)
        else -> translateRectF(0f, 0f)
    }

    private fun changeBitmapSize(width: Int, height: Int) {
        if (width == 0 || height == 0)
            return

        invalidationLock.withLock {
            tmpViewBitmap = Bitmap.createBitmap(
                (width * quality).toInt(),
                (height * quality).toInt(), Bitmap.Config.ARGB_8888
            ).apply {
                tmpViewBitmapCanvas = Canvas(this)
            }
        }
        mBgGradientPaint.apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(), intArrayOf(
                    0x7F000000,
                    0x00000000,
                    0x00000000,
                    0x7F000000
                ), floatArrayOf(
                    0.0f, 0.33f, 0.66f, 1.0f
                ),
                Shader.TileMode.CLAMP
            )
        }
        redrawBitmaps()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentHeight = h
        currentWidth = w
        changeBitmapSize(w, h)
    }


    private fun drawBitmapAsset(canvas: Canvas, asset: BitmapAsset) {
        val translation = translateRectFForParallax(asset.parallaxLayer)

        val svgWidth = (asset.bitmap.width / mExtraBitmapSpace).toInt()
        val svgHeight = (asset.bitmap.height / mExtraBitmapSpace).toInt()

        val bmRect = Rect(
            translation.left.toInt(),
            translation.top.toInt(),
            svgWidth + translation.left.toInt(),
            svgHeight + translation.top.toInt()
        )

        canvas.drawBitmap(asset.bitmap, bmRect, Rect(0, 0, canvas.width, canvas.height), null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        viewBitmapLock.withLock {
            viewBitmap?.let {
                canvas.drawBitmap(
                    it,
                    null,
                    Rect(0, 0, canvas.height, canvas.width),
                    null
                )
                if (assetBitmapsDrawn.get()) {
                    avatarIsReadyInternal.set(true)
                    handler.post {
                        avatarIsReadyCallback?.invoke()
                        avatarIsReadyCallback = null
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        avatarIsReadyCallback = null
        wasParallaxBeforeDetachedFromWindow = mSensorManager != null
        stopParallaxEffect()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (wasParallaxBeforeDetachedFromWindow)
            startParallaxEffect()
    }

    companion object {
        private const val BODY_GROUP_X = 18f
        private const val BODY_GROUP_Y = 17f
        private const val HEAD_GROUP_X = 24f
        private const val HEAD_GROUP_Y = 23f
        private const val EARS_GROUP_X = 20f
        private const val EARS_GROUP_Y = 18f
        private const val FACE_GROUP_X = 30f
        private const val FACE_GROUP_Y = 30f

        fun createBitmap(context: Context, avatarState: String, resolution: Int = 2000): Bitmap {
            val avatarView = AvatarView(context)
            avatarView.setState(avatarState)
            avatarView.assetBitmaps.addAll(avatarView.drawBitmaps(resolution, resolution))
            avatarView.onSizeChanged(resolution, resolution, 0, 0)
            while (avatarView.viewBitmap == null) {
                Thread.sleep(50)
            }
            return avatarView.viewBitmap?.copy(Bitmap.Config.ARGB_8888, true)
                ?: throw RuntimeException("Avatar bitmap is null.")
        }
    }
}
