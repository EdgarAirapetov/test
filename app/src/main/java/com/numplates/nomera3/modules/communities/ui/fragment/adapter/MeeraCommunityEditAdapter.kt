package com.numplates.nomera3.modules.communities.ui.fragment.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.databinding.MeeraEditGroupPhotoItemBinding
import com.numplates.nomera3.databinding.MeeraEditGroupSwitchItemBinding
import com.numplates.nomera3.databinding.MeeraEditGroupTextItemBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.ui.fragment.MeeraCommunityEditAction
import com.numplates.nomera3.modules.communities.ui.fragment.holder.MeeraCommunityEditPhotoHolder
import com.numplates.nomera3.modules.communities.ui.fragment.holder.MeeraCommunityEditSwitchHolder
import com.numplates.nomera3.modules.communities.ui.fragment.holder.MeeraCommunityEditTextHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraCommunityEditAdapter(
    val listener: (action: MeeraCommunityEditAction) -> Unit
) : ListAdapter<CommunityEditItemType, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var community: CommunityEntity? = null

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return when (item.position) {
            CommunityEditItemType.PHOTO_ITEM.position -> {
                CommunityEditItemType.PHOTO_ITEM.position
            }

            CommunityEditItemType.NAME_TEXT_ITEM.position -> {
                CommunityEditItemType.NAME_TEXT_ITEM.position
            }

            CommunityEditItemType.DESCRIPTION_TEXT_ITEM.position -> {
                CommunityEditItemType.DESCRIPTION_TEXT_ITEM.position
            }

            CommunityEditItemType.SWITCH_ITEM.position -> {
                CommunityEditItemType.SWITCH_ITEM.position
            }

            else -> error("No such a view type.")
        }
    }

    override fun submitList(list: List<CommunityEditItemType>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CommunityEditItemType.PHOTO_ITEM.position -> {
                MeeraCommunityEditPhotoHolder(
                    parent.inflateBinding(MeeraEditGroupPhotoItemBinding::inflate),
                    listener
                )
            }

            CommunityEditItemType.NAME_TEXT_ITEM.position -> {
                MeeraCommunityEditTextHolder(
                    parent.inflateBinding(MeeraEditGroupTextItemBinding::inflate),
                    CommunityEditItemType.NAME_TEXT_ITEM,
                    listener
                )
            }

            CommunityEditItemType.DESCRIPTION_TEXT_ITEM.position -> {
                MeeraCommunityEditTextHolder(
                    parent.inflateBinding(MeeraEditGroupTextItemBinding::inflate),
                    CommunityEditItemType.DESCRIPTION_TEXT_ITEM,
                    listener
                )
            }

            CommunityEditItemType.SWITCH_ITEM.position -> {
                MeeraCommunityEditSwitchHolder(
                    parent.inflateBinding(MeeraEditGroupSwitchItemBinding::inflate),
                    listener
                )
            }

            else -> error("No such a view type.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MeeraCommunityEditPhotoHolder -> {

                holder.bind(community?.avatarBig)
            }

            is MeeraCommunityEditTextHolder -> {
                if (CommunityEditItemType.NAME_TEXT_ITEM.position == position) {
                    holder.bind(name = community?.name)
                }
                if (CommunityEditItemType.DESCRIPTION_TEXT_ITEM.position == position) {
                    holder.bind(description = community?.description)
                }
            }

            is MeeraCommunityEditSwitchHolder -> {
                holder.bind(private = community?.private.toBoolean(), royalty = community?.royalty.toBoolean())
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCommunityInfo(community: CommunityEntity){
        this.community = community
        notifyDataSetChanged()
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommunityEditItemType>() {
            override fun areItemsTheSame(
                oldItem: CommunityEditItemType,
                newItem: CommunityEditItemType
            ): Boolean {
                return oldItem.ordinal == newItem.ordinal
            }

            override fun areContentsTheSame(
                oldItem: CommunityEditItemType,
                newItem: CommunityEditItemType
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

enum class CommunityEditItemType(val position: Int) {
    PHOTO_ITEM(0),
    NAME_TEXT_ITEM(1),
    DESCRIPTION_TEXT_ITEM(2),
    SWITCH_ITEM(3)
}
