package com.noomeera.nmravatarssdk.data

import com.google.gson.Gson

internal data class AvatarState(
   val gender: String,
   val assets: AvatarStateAssets
) {
   fun allAvatarStateAssets() = listOf(
      assets.head,
      assets.hair,
      assets.eyes,
      assets.eyebrows,
      assets.nose,
      assets.mouth,
      assets.beard,
      assets.shirt,
      assets.background,
      assets.accessories,
      assets.hat
   )

   fun toJson() = Gson().toJson(this)
}

internal data class AvatarStateAssets(
   val head: AvatarStateAsset,
   val hair: AvatarStateAsset,
   val eyes: AvatarStateAsset,
   val eyebrows: AvatarStateAsset,
   val nose: AvatarStateAsset,
   val mouth: AvatarStateAsset,
   val beard: AvatarStateAsset,
   val shirt: AvatarStateAsset,
   val background: AvatarStateAsset,
   val accessories: AvatarStateAsset,
   val hat: AvatarStateAsset
)

internal data class AvatarStateAsset(
   val id: String,
   val color: String?
)