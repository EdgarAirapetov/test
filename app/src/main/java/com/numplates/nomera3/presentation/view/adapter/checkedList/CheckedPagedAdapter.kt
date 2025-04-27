package com.numplates.nomera3.presentation.view.adapter.checkedList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedListModel

class CheckedPagedAdapter: PagedListAdapter<CheckedListModel, RecyclerView.ViewHolder>(diffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            VIEW_TYPE_CHECKED_ITEM -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_cheked_list, parent, false)
                return ViewHolderCheckedItem(v)
            }
            VIEW_TYPE_CHECKED_LOCATION ->{
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_checked_location, parent, false)
                return ViewHolderCheckedLocation(v)
            }
            VIEW_TYPE_HEADER ->{
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_header_location, parent, false)
                return ViewHolderHeader(v)
            }
            else ->{
                throw IllegalArgumentException("View type is not supported")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) as CheckedListModel
        when(item.viewType){
            VIEW_TYPE_CHECKED_ITEM ->{
                (holder as ViewHolderCheckedItem).bind()
            }
            VIEW_TYPE_CHECKED_LOCATION ->{
                (holder as ViewHolderCheckedLocation).bind()
            }
            VIEW_TYPE_HEADER ->{
                (holder as ViewHolderHeader).bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position) as CheckedListModel
        return item.viewType
    }

    inner class ViewHolderCheckedItem(v: View): RecyclerView.ViewHolder(v){

        private var txt: TextView = v.findViewById(R.id.tv_checked_list)

        fun bind(){
            txt.setPadding(
                    dpToPx(46),
                    dpToPx(16),
                    0,
                    dpToPx(16)
            )

        }
    }

    inner class ViewHolderHeader(v: View): RecyclerView.ViewHolder(v){
        fun bind() = Unit
    }

    inner class ViewHolderCheckedLocation(v: View): RecyclerView.ViewHolder(v){
        fun bind() = Unit
    }

    companion object{

        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_CHECKED_LOCATION = 1
        const val VIEW_TYPE_CHECKED_ITEM = 3


        private val diffCallback = object : DiffUtil.ItemCallback<CheckedListModel>() {
            override fun areItemsTheSame(oldI: CheckedListModel, newI: CheckedListModel) = oldI == newI


            override fun areContentsTheSame(oldI: CheckedListModel, newI: CheckedListModel): Boolean {
                return false
            }
        }
    }
}
