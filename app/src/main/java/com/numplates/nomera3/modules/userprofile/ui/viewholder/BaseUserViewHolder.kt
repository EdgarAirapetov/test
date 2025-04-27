package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflateLayout
import com.numplates.nomera3.modules.userprofile.ui.entity.UserUIEntity


abstract class BaseUserViewHolder<in T : UserUIEntity> : RecyclerView.ViewHolder {

    constructor(parent: ViewGroup, @LayoutRes id: Int) : super(parent.inflateLayout(id))
    constructor(itemView: View) : super(itemView)

    abstract fun bind(data: T)
}
