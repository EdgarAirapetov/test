package com.meera.core.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView

const val DEFAULT_ELEVATION_WHEN_RECYCLER_SCROLL = 25f

fun setShadowWhenRecyclerScroll(
    recyclerView: RecyclerView,
    elevation: Float,
    vararg views: View
) {
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!recyclerView.canScrollVertically(-1)) {
                setViewsElevation(elevation = 0f, views = views)
            } else {
                setViewsElevation(elevation = elevation, views = views)
            }
        }
    })
}

private fun setViewsElevation(elevation: Float, vararg views: View) {
    views.forEach {view ->
        view.elevation = elevation
    }
}
