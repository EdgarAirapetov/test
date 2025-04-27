package com.numplates.nomera3.presentation.view.adapter.checkedList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedListModel

class CheckedListAdapter(
    var mData: MutableList<CheckedListModel>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isEnabled: Boolean = true
        set(value) {
            field  = value
            notifyDataSetChanged()
        }


    var interactor: IAdapterInteractor? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            VIEW_TYPE_CHECK_BOX -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_cheked_list, parent, false)
                return CheckBoxViewHolder(v)
            }
            VIEW_TYPE_SELECTOR -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_selector, parent, false)
                return SelectorViewHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_selector, parent, false)
                return CheckBoxViewHolder(v)
            }
        }
    }

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = mData[position]

        when(model.viewType){
            VIEW_TYPE_CHECK_BOX -> {
                (holder as CheckBoxViewHolder).bind(model)
            }

            VIEW_TYPE_SELECTOR -> {
                (holder as SelectorViewHolder).bind(model)
            }
        }
    }

    fun replaceAll(models: MutableList<CheckedListModel>){
        mData.clear()
        mData.addAll(models)
        notifyDataSetChanged()
    }

    fun addData(models: MutableList<CheckedListModel>){
        val insPos = mData.size
        mData.addAll(models)
        notifyItemInserted(insPos)
    }

    override fun getItemViewType(position: Int): Int {
        return mData[position].viewType
    }


    inner class CheckBoxViewHolder(v: View): RecyclerView.ViewHolder(v){
        private val checkBox: CheckBox = v.findViewById(R.id.cb_checked_list)
        private val tvSetting: TextView = v.findViewById(R.id.tv_checked_list)

        fun bind(model: CheckedListModel){
            if (isEnabled)
                setUpEnabled(model)
            else setUpNotEnabled(model)



        }

        private fun setUpNotEnabled(model: CheckedListModel) {
            checkBox.isChecked = model.isChecked
            tvSetting.text = model.text
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    interactor?.onChecked(model)
                else interactor?.onUnCheck(model)
            }
        }

        private fun setUpEnabled(model: CheckedListModel) {
            checkBox.isChecked = model.isChecked
            tvSetting.text = model.text
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    interactor?.onChecked(model)
                else interactor?.onUnCheck(model)
            }
        }
    }

    inner class SelectorViewHolder(v: View): RecyclerView.ViewHolder(v){
        private val selector: Switch = v.findViewById(R.id.sw_selector)

        fun bind(model: CheckedListModel){
            selector.text = model.text
            selector.isChecked = model.isChecked

            selector.setOnCheckedChangeListener{ _, isChecked ->
                if (isChecked)
                    interactor?.onChecked(model)
                else interactor?.onUnCheck(model)
            }
        }
    }

    interface IAdapterInteractor{
        fun onChecked(model: CheckedListModel)
        fun onUnCheck(model: CheckedListModel)
    }

    companion object {
        const val VIEW_TYPE_CHECK_BOX = 0
        const val VIEW_TYPE_SELECTOR = 1
    }
}