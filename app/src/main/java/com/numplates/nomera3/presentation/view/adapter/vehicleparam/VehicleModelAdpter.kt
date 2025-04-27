package com.numplates.nomera3.presentation.view.adapter.vehicleparam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.numplates.nomera3.R
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import android.view.animation.OvershootInterpolator
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedChildModel
import com.numplates.nomera3.presentation.model.adaptermodel.ExpandedCheckedData


class VehicleModelAdpterExpanded(
        val list: MutableList<ExpandedCheckedData>
): ExpandableRecyclerViewAdapter<VehicleModelAdpterExpanded.TitleViewHolder, VehicleModelAdpterExpanded.ContentViewHolder>(list){

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): TitleViewHolder {
        val v = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_header_expanded, parent,false)
        return TitleViewHolder(v)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ContentViewHolder {
        val v = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_vehicle_param_select, parent, false)
        return ContentViewHolder(v)
    }

    override fun onBindChildViewHolder(holder: ContentViewHolder?, flatPosition: Int, group: ExpandableGroup<*>?, childIndex: Int) {
        val model = (group as ExpandedCheckedData).items[childIndex]
        holder?.bind(model)
    }

    override fun onBindGroupViewHolder(holder: TitleViewHolder?, flatPosition: Int, group: ExpandableGroup<*>?) {
        holder?.setTitle(group?.title ?: "")
    }

    fun addItems(items: List<ExpandedCheckedData>) {
        list.addAll(items)
        notifyDataSetChanged()
    }


    inner class  TitleViewHolder(v: View): GroupViewHolder(v){

        val header: TextView = v.findViewById(R.id.tv_expanded_header)
        private val arrow: ImageView = v.findViewById(R.id.iv_arrow_expanded)

        fun setTitle(title: String){
            header.text = title
        }

        override fun expand() {
            arrow.animate().rotation( 0f).setInterpolator(OvershootInterpolator()).duration = 300
        }

        override fun collapse() {
            arrow.animate().rotation(-90f).setInterpolator(OvershootInterpolator()).duration = 300
        }
    }

    inner class ContentViewHolder (v: View): ChildViewHolder(v){

        val name: TextView = v.findViewById(R.id.tv_vehicle_param_select)

        fun bind(model: CheckedChildModel) {
            name.text = model.brandName
        }

    }
}