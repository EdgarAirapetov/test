package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.R
import timber.log.Timber
import kotlin.properties.Delegates

class BaseSettingsUserListAdapter(
    private val isDeleteAllAvailable: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var collection: MutableList<UserExclusionsItem> by Delegates.observable(mutableListOf()) { _, _, _ ->
        notifyDataSetChanged()
    }


    fun addItems(items: List<UserExclusionsItem>) {
        // Timber.e("Add USERS adapt size:${items.size}")
        /*val size = collection.size
        collection.addAll(size - 1, items)
        notifyItemRangeInserted(size - 1, items.size)
        notifyItemChanged(collection.size - items.size - 2)*/
        // ------- Example add range ----------------------

        // Remove DeleteAll items if Exists
        try {
            collection.removeAll { it.itemType == ITEM_DELETE_ALL }
            collection.addAll(items)
            if (collection.isNotEmpty() && isDeleteAllAvailable) {
                collection.add(UserExclusionsItem(ITEM_DELETE_ALL, null, 0))
            }
            notifyDataSetChanged()
        }catch (e: Exception){
            Timber.d(e)
        }
    }

    fun removeItemAndDecreaseExclusionsCount(position: Int){
        try {
            collection.removeAt(position)
            collection[1].exclusionAmount = collection[1].exclusionAmount - 1
            notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun clearUserItems() {
        collection.clear()
    }

    fun getAllUsersIds(): List<Long> {
        val uids = mutableListOf<Long>()
        collection.map {
            it.user?.userId?.let { uid ->
                uids.add(uid)
            }
        }
        return uids
    }


    fun changeExclusionsCount(count: Long){
        try {
            collection[1].exclusionAmount = count
            notifyItemChanged(1)
        }catch (e:Exception){ Timber.e(e) }
    }

    internal var clickUserMenuListener: (UserExclusionsItem, Int) -> Unit = { _, _ -> }

    internal var clickAddExclusionListener: () -> Unit = { }

    internal var clickDeleteAllUsers: () -> Unit = {}

    internal var clickUserListener: (UserExclusionsItem, Int) -> Unit = { _, _ -> }



    override fun getItemViewType(position: Int) = collection[position].itemType

    override fun getItemCount(): Int = collection.size

    fun getUserCount(): Int = collection.size - COUNT_OF_NON_USER_ITEMS


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_ADD_EXCLUSION -> AddExclusionViewHolder(parent.inflate(R.layout.item_setting_add_exclusion))
            ITEM_TYPE_EXCLUSION_AMOUNT -> ExclusionAmountViewHolder(parent.inflate(R.layout.item_setting_exclusion_amount))
            ITEM_TYPE_USER -> ExclusionUserViewHolder(parent.inflate(R.layout.item_setting_user))
            ITEM_DELETE_ALL -> DeleteAllUsersViewHolder(parent.inflate(R.layout.item_setting_delete_all_users))
            ITEM_TOP_SPACE -> TopSpaceViewHolder(parent.inflate(R.layout.item_setting_top_space))
            else -> ExclusionUserViewHolder(parent.inflate(R.layout.item_setting_user))
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val nextItem = if (position < itemCount - 1) collection[position + 1] else null
        when (holder.itemViewType) {
            ITEM_TYPE_ADD_EXCLUSION -> (holder as AddExclusionViewHolder)
                    .bind(collection[position], clickAddExclusionListener)
            ITEM_TYPE_EXCLUSION_AMOUNT -> (holder as ExclusionAmountViewHolder)
                    .bind(collection[position])
            ITEM_TYPE_USER -> (holder as ExclusionUserViewHolder)
                    .bind(collection[position], nextItem, position, clickUserMenuListener)
            ITEM_DELETE_ALL -> (holder as DeleteAllUsersViewHolder)
                    .bind(clickDeleteAllUsers)
        }
    }


    class AddExclusionViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvBtnAddExclusion: TextView = itemView.findViewById(R.id.tv_btn_add_exclusion)

        fun bind(item: UserExclusionsItem, clickListener: () -> Unit) {
            tvBtnAddExclusion.text = item.addUserItemTitle
            tvBtnAddExclusion.setOnClickListener { clickListener() }
        }
    }

    class ExclusionAmountViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvExclusionAmount: TextView = itemView.findViewById(R.id.tv_exclusion_users_amount)

        fun bind(item: UserExclusionsItem) {
            val strCount = item.exclusionAmount.toInt()

            tvExclusionAmount.text = itemView.context.pluralString(R.plurals.settings_users, strCount)
            //https://nomera.atlassian.net/browse/BR-27328 - 3-й пункт. Старый код
            //Удалить после теста локализации
//            if (strCount == "11" || strCount == "12" || strCount == "13" || strCount == "14"){
//                tvExclusionAmount.text = "$strCount ыуепользователей"
//            }
//            else if (strCount.endsWith("1")){
//                tvExclusionAmount.text = "$strCount пользователь"
//            }else if (strCount.endsWith("0")){
//                tvExclusionAmount.text = "$strCount пользователей"
//            }else if (strCount.endsWith("2") || strCount.endsWith("3") || strCount.endsWith("4")){
//                tvExclusionAmount.text = "$strCount пользователя"
//            }else if (strCount.endsWith("5") || strCount.endsWith("6")
//                    || strCount.endsWith("7")|| strCount.endsWith("8")
//                    || strCount.endsWith("9")){
//                tvExclusionAmount.text = "$strCount пользователей"
//            }
        }

    }


    inner class ExclusionUserViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val ivDotsMenuUser: ImageView = itemView.findViewById(R.id.iv_user_dots_menu)
        private val ivSeparator: ImageView = itemView.findViewById(R.id.imageView19)
        private val clLayout: ConstraintLayout = itemView.findViewById(R.id.cl_item_setting_user)

        fun bind(item: UserExclusionsItem, nextItem: UserExclusionsItem?, position: Int, clickListener: (UserExclusionsItem, Int) -> Unit) {
            Glide.with(itemView.context)
                    .load(item.user?.avatarSmall)
                    .apply(RequestOptions.circleCropTransform())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.fill_8_round)
                    .into(ivUserAvatar)
            tvUserName.text = item.user?.name
            ivDotsMenuUser.setOnClickListener { clickListener(item, position) }
            clLayout.setOnClickListener { clickUserListener(item, position) }

            // Hide last item separator
            if (nextItem?.user == null) {
                ivSeparator.gone()
            } else
                ivSeparator.visible()
        }

    }

    class DeleteAllUsersViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView){
        private val tvBtnDeleteALl: TextView = itemView.findViewById(R.id.tv_btn_delete_all)

        fun bind(clickListener: () -> Unit){
            tvBtnDeleteALl.setOnClickListener { clickListener() }
        }
    }

    inner class TopSpaceViewHolder(val itemView: View): RecyclerView.ViewHolder(itemView)

    /**
     * Data class for item adapter
     */
    data class UserExclusionsItem(
            val itemType: Int,
            val user: UserSimple?,
            var exclusionAmount: Long,
            var addUserItemTitle: String = String.empty()
    )


    companion object {
        const val ITEM_TYPE_ADD_EXCLUSION = 0
        const val ITEM_TYPE_EXCLUSION_AMOUNT = 1
        const val ITEM_TYPE_USER = 3
        const val ITEM_DELETE_ALL = 4
        const val ITEM_TOP_SPACE = 5

        const val COUNT_OF_NON_USER_ITEMS = 2
    }


}
