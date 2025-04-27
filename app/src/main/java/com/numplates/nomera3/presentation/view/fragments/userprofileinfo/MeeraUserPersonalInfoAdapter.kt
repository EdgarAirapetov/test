package com.numplates.nomera3.presentation.view.fragments.userprofileinfo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoAvatarItemBinding
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoCheckboxItemBinding
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoInputItemBinding
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoSelectItemBinding
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoTextItemBinding
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders.MeeraUserPersonalInfoAvatarItemHolder
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders.MeeraUserPersonalInfoCheckBoxItemHolder
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders.MeeraUserPersonalInfoInputItemHolder
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders.MeeraUserPersonalInfoSelectItemHolder
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders.MeeraUserPersonalInfoTextItemHolder
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer

class MeeraUserPersonalInfoAdapter(
    private val items: List<UserPersonalInfoItemType>,
    private val userInfoFromContainer: UserPersonalInfoContainer,
    val lifecycleScope: LifecycleCoroutineScope,
    private val actionListener: (UserPersonalInfoAction) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userProfileContainer: UserPersonalInfoContainer = userInfoFromContainer


    override fun getItemViewType(position: Int): Int {
        return items[position].position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val type = UserPersonalInfoItemType.entries.find { it.position == viewType }
        return when (type) {
            UserPersonalInfoItemType.FULL_NAME -> {
                val binding = MeeraUserPersonalInfoInputItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoInputItemHolder(
                    binding,
                    UserPersonalInfoItemType.FULL_NAME,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.UNIQUE_NAME -> {
                val binding = MeeraUserPersonalInfoInputItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoInputItemHolder(
                    binding,
                    UserPersonalInfoItemType.UNIQUE_NAME,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.BIRTHDAY -> {
                val binding = MeeraUserPersonalInfoSelectItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                return MeeraUserPersonalInfoSelectItemHolder(
                    binding,
                    UserPersonalInfoItemType.BIRTHDAY,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.GENDER -> {
                val binding = MeeraUserPersonalInfoCheckboxItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoCheckBoxItemHolder(
                    binding,
                    UserPersonalInfoItemType.GENDER,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.COUNTRY -> {
                val binding = MeeraUserPersonalInfoSelectItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoSelectItemHolder(
                    binding,
                    UserPersonalInfoItemType.COUNTRY,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.CITY -> {
                val binding = MeeraUserPersonalInfoSelectItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoSelectItemHolder(
                    binding,
                    UserPersonalInfoItemType.CITY,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.ACCOUNT_MANAGEMENT -> {
                val binding = MeeraUserPersonalInfoSelectItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoSelectItemHolder(
                    binding,
                    UserPersonalInfoItemType.ACCOUNT_MANAGEMENT,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.PHONE_NUMBER -> {
                val binding = MeeraUserPersonalInfoTextItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoTextItemHolder(
                    binding,
                    UserPersonalInfoItemType.PHONE_NUMBER,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.EMAIL -> {
                val binding = MeeraUserPersonalInfoTextItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoTextItemHolder(
                    binding,
                    UserPersonalInfoItemType.EMAIL,
                    parent.context.resources,
                    actionListener
                )
            }

            UserPersonalInfoItemType.AVATAR -> {
                val binding = MeeraUserPersonalInfoAvatarItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return MeeraUserPersonalInfoAvatarItemHolder(
                    binding,
                    lifecycleScope,
                    actionListener
                )
            }

            null -> throw RuntimeException("Missing data adapter type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MeeraUserPersonalInfoTextItemHolder -> holder.bind(userProfileContainer)
            is MeeraUserPersonalInfoSelectItemHolder -> holder.bind(userProfileContainer)
            is MeeraUserPersonalInfoInputItemHolder -> holder.bind(userProfileContainer)
            is MeeraUserPersonalInfoCheckBoxItemHolder -> holder.bind(userProfileContainer)
            is MeeraUserPersonalInfoAvatarItemHolder -> holder.bind(userProfileContainer)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updatePersonalInfoContainer(userProfileContainerNew: UserPersonalInfoContainer) {
        this.userProfileContainer = userProfileContainerNew
        notifyDataSetChanged()
    }
}
