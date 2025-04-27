package com.numplates.nomera3.modules.notifications.ui.viewholder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflateLayout

open class BaseViewHolder(
        parent: ViewGroup, @LayoutRes id: Int
) : RecyclerView.ViewHolder(parent.inflateLayout(id))
