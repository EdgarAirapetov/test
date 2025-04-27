package com.noomeera.nmravatarssdk.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.caverock.androidsvg.SVG
import com.google.gson.annotations.SerializedName
import com.noomeera.nmravatarssdk.NMRAvatarsSDK
import com.noomeera.nmravatarssdk.extensions.SvgCache
import com.noomeera.nmravatarssdk.ui.view.AvatarView

internal data class LayeredSvg(
    val svg: SVG,
    val layer: Int,
    val parallaxLayer: String,
    val mask: Boolean,
    val applyMask: Boolean
)

internal data class AssetsConfig(
    val format: String,
    @SerializedName("json-version")
    val jsonVersion: Int,
    val man: AssetCategories,
    val woman: AssetCategories
) {
    internal fun layeredSvgsFromStateAsset(
        context: Context,
        stateAsset: AvatarStateAsset
    ): List<LayeredSvg> {
        val assetSet = allAssetSets().find { it.id == stateAsset.id }
            ?: throw RuntimeException("Unknown stateAsset")
        return listOf(
            assetSet.assets?.map {
                it.toLayeredSvg(context)
            } ?: listOf(),
            assetSet.variants?.find { it.color.id == stateAsset.color }?.assets?.map {
                it.toLayeredSvg(context)
            } ?: listOf()
        ).flatten()
    }

    fun allColors(): List<AssetColor> {
        return allAssetSets().mapNotNull { it.variants }
            .map { it.map { coloredAsset -> coloredAsset.color } }.flatten()
    }

    internal fun allAssetSets() = listOf(
        man.head,
        man.hair,
        man.eyes,
        man.eyebrows,
        man.nose,
        man.mouth,
        man.beard,
        man.shirt,
        man.background,
        man.accessories,
        man.hat,

        woman.head,
        woman.hair,
        woman.eyes,
        woman.eyebrows,
        woman.nose,
        woman.mouth,
        woman.beard,
        woman.shirt,
        woman.background,
        woman.accessories,
        woman.hat
    ).flatten()

    internal fun randomAvatarState(womanGender: Boolean = false): AvatarState {
        val categories = if (womanGender) woman else man
        return AvatarState(
            gender = if (womanGender) "female" else "male",
            assets = AvatarStateAssets(
                head = categories.head.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                hair = categories.hair.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                eyes = categories.eyes.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                eyebrows = categories.eyebrows.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                nose = categories.nose.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                mouth = categories.mouth.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                beard = categories.beard.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                shirt = categories.shirt.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                background = categories.background.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                accessories = categories.accessories.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                },
                hat = categories.hat.random().let {
                    val color = it.variants?.random()?.color?.id
                    return@let AvatarStateAsset(it.id, color)
                }
            )
        )
    }
}


internal data class AssetCategories(
    val head: List<AssetSet>,
    val hair: List<AssetSet>,
    val eyes: List<AssetSet>,
    val eyebrows: List<AssetSet>,
    val nose: List<AssetSet>,
    val mouth: List<AssetSet>,
    val beard: List<AssetSet>,
    val shirt: List<AssetSet>,
    val background: List<AssetSet>,
    val accessories: List<AssetSet>,
    val hat: List<AssetSet>
)

internal data class AssetSet(
    val assets: List<StaticAsset>?,
    val variants: List<ColoredAsset>?,
    val preview: String?,
    val id: String
)

internal data class StaticAsset(
    val layer: Int,
    @SerializedName("animation-layer")
    val animationLayer: String,
    val path: String,
    @SerializedName("show-in-selector")
    val showInSelector: Boolean,
    val mask: Boolean
) {
    internal fun toLayeredSvg(context: Context): LayeredSvg {
        return LayeredSvg(
            SvgCache.fromFile("${NMRAvatarsSDK.getResourcesDirPath(context)}/$path"),
            layer,
            animationLayer,
            mask,
            layer != 168
        )
    }
}

internal data class AssetColor(
    val path: String,
    val id: String
)

internal data class ColoredAsset(
    val color: AssetColor,
    val assets: List<StaticAsset>
)