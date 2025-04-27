package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.ContactsModel
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import timber.log.Timber


class ContactListAdapter(val callback: ContactsAdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_CONTACTS_LOCAL = 1
        const val TYPE_CONTACTS_FROM_SERVER = 2
        const val TYPE_CONTACTS_LOADER = 3
        const val TYPE_CONTACTS_HEADER = 4
    }

    private var contactList: MutableList<ContactsModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_CONTACTS_LOCAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
                return LocalContactViewHolder(view)
            }
            TYPE_CONTACTS_FROM_SERVER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
                return ServerContactsViewHolder(view)
            }
            TYPE_CONTACTS_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_header, parent, false)
                return HeaderContactsViewHolder(view)
            }
            TYPE_CONTACTS_LOADER -> {

            }
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return LocalContactViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        val contactsModel = contactList[position]
        return when {
            contactsModel.contact?.contactId != null -> contactsModel.contact?.contactId!!
            contactsModel.user?.userId != null -> contactsModel.user?.userId!!
            else -> 0L
        }
    }

    override fun getItemCount(): Int = contactList.size

    override fun getItemViewType(position: Int): Int {
        return contactList[position].holderType
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LocalContactViewHolder)
            holder.bind(contactList[position])
        if (holder is ServerContactsViewHolder)
            holder.bind(contactList[position])
    }

    fun setContactList(list: MutableList<ContactsModel>) {
        contactList.clear()
        contactList.addAll(list)
        Timber.i(" CONTACT_LIST_SIZE: ${contactList.size}")
        contactList.forEach {
            if (it.holderType == TYPE_CONTACTS_LOCAL) {
                notifyDataSetChanged()
                return
            }
        }
        val header = contactList.find {
            it.holderType == TYPE_CONTACTS_HEADER
        }
        contactList.remove(header)
        notifyDataSetChanged()
    }

    fun clear() {
        contactList.clear()
        Timber.i(" CONTACT_LIST_SIZE: ${contactList.size}")
        notifyDataSetChanged()
    }


    inner class LocalContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var iv_contact_avatar: ImageView = view.findViewById(R.id.iv_contact_avatar)
        private var tv_contact_user_name: TextView = view.findViewById(R.id.tv_contact_user_name)
        private var iv_contact_icon: ImageView = view.findViewById(R.id.iv_contact_icon)
        private var tv_contact_name: TextView = view.findViewById(R.id.tv_contact_name)
        private var nvNumber_contact: NumberPlateEditView = view.findViewById(R.id.nvNumber_contact)
        private var iv_contact_status: ImageView = view.findViewById(R.id.iv_contact_status)


        fun bind(contactsModel: ContactsModel) {
            iv_contact_icon.invisible()
            iv_contact_icon.layoutParams.width = 0
            tv_contact_user_name.text = contactsModel.contact?.name
            tv_contact_name.text = contactsModel.contact?.phone

            Glide.with(itemView.context)
                    .load(contactsModel.contact?.avatar)
                    .apply(RequestOptions.placeholderOf(R.drawable.fill_8_round))
                    .apply(RequestOptions.circleCropTransform())
                    .into(iv_contact_avatar)

            iv_contact_status.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.invitation))

            itemView.setOnClickListener {
                callback.onItemClicked(contactsModel)
            }

            nvNumber_contact.gone()
        }
    }

    inner class ServerContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var iv_contact_avatar: ImageView = itemView.findViewById(R.id.iv_contact_avatar)
        private var tv_contact_user_name: TextView = itemView.findViewById(R.id.tv_contact_user_name)
        private var iv_contact_icon: ImageView = itemView.findViewById(R.id.iv_contact_icon)
        private var tv_contact_name: TextView = itemView.findViewById(R.id.tv_contact_name)
        private var cl_contact_container: ConstraintLayout = itemView.findViewById(R.id.cl_contact_container)

        fun bind(contactsModel: ContactsModel) {
            contactsModel.user?.let {user->
                Glide.with(iv_contact_avatar.context)
                        .load(user.avatarSmall)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.fill_8_round)
                        .into(iv_contact_avatar)
                tv_contact_user_name.text = user.name ?: ""
                tv_contact_name.text = contactsModel.contact?.name ?: ""
                iv_contact_icon.setImageResource(R.drawable.contacts_violet)

                cl_contact_container.setOnClickListener {
                    callback.onNumUserClicked(contactsModel)
                }

                // TODO: Vehicle - отсутствуют в новой модели UserChat
                /*user.vehicle?.let {
                    if (it.number != null) {
                        val vehicle = VehicleEntity(
                                it.number!!,
                                VehicleType(it.type!!.typeId!!.toInt()),
                                VehicleCountry(it.country!!.countryId!!.toLong()))
                        NumberPlateEditView.Builder(nvNumber_contact)
                                .setVehicleNew(vehicle)
                                .build()

                        if (it.type!!.typeId!!.toInt() == 1) {
                            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            params.setMargins(
                                    NGraphics.dpToPx(-74),
                                    NGraphics.dpToPx(-16),
                                    NGraphics.dpToPx(-74),
                                    NGraphics.dpToPx(-16)
                            )
                            nvNumber_contact.layoutParams = params
                        } else if (it.type!!.typeId!!.toInt() == 2) {
                            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            params.setMargins(
                                    NGraphics.dpToPx(-47),
                                    NGraphics.dpToPx(-26),
                                    NGraphics.dpToPx(-47),
                                    NGraphics.dpToPx(-26)
                            )
                            nvNumber_contact.layoutParams = params
                        }
                        nvNumber_contact.scaleX = 0.5f
                        nvNumber_contact.scaleY = 0.5f
                    }
                }*/

            }?:kotlin.run {

            }
        }
    }

    inner class HeaderContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface ContactsAdapterCallback {

        fun onItemClicked(contactsModel: ContactsModel)

        fun onActionClicked(contactsModel: ContactsModel)

        fun onLoadMore() {

        }

        fun onNumUserClicked(contactsModel: ContactsModel){

        }
    }

}
