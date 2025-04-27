package com.numplates.nomera3.modules.feed.ui

import androidx.annotation.DrawableRes
import com.numplates.nomera3.R

enum class LikeDrawableState(@DrawableRes val drawable: Int) {
    LIKE_ACTIVE_COMMON(R.drawable.like_active),
    LIKE_INACTIVE_COMMON(R.drawable.like_inactive),
    DISLIKE_ACTIVE_COMMON(R.drawable.dislike_active),
    DISLIKE_INACTIVE_COMMON(R.drawable.dislike_inactive),

    LIKE_ACTIVE_VIP(R.drawable.like_active_vip),
    LIKE_INACTIVE_VIP(R.drawable.like_inactive_vip),
    DISLIKE_ACTIVE_VIP(R.drawable.dislike_active_vip),
    DISLIKE_INACTIVE_VIP(R.drawable.dislike_inactive_vip)
}

fun getLikeActive(isVip: Boolean) =
    if (!isVip) LikeDrawableState.LIKE_ACTIVE_COMMON.drawable else LikeDrawableState.LIKE_ACTIVE_VIP.drawable

fun getLikeInActive(isVip: Boolean) =
    if (!isVip) LikeDrawableState.LIKE_INACTIVE_COMMON.drawable else LikeDrawableState.LIKE_INACTIVE_VIP.drawable

fun getDislikeActive(isVip: Boolean) =
    if (!isVip) LikeDrawableState.DISLIKE_ACTIVE_COMMON.drawable else LikeDrawableState.DISLIKE_ACTIVE_VIP.drawable

fun getDislikeInActive(isVip: Boolean) =
    if (!isVip) LikeDrawableState.DISLIKE_INACTIVE_COMMON.drawable else LikeDrawableState.DISLIKE_INACTIVE_VIP.drawable

