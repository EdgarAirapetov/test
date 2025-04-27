package com.numplates.nomera3.presentation.view.adapter.newfriends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.enableApprovedIcon
import com.meera.core.utils.getAge
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.presentation.view.widgets.VipView
import timber.log.Timber

class FriendsListAdapter(
    private var mData: MutableList<FriendModel> = mutableListOf(),
    private var itemClickListener: IFriendsListInteractor? = null
) : RecyclerView.Adapter<FriendsListAdapter.ViewHolder>() {

    var interactor: IFriendsListInteractor? = null
    private var isSearch = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = mData.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mData[position], position)
    }

    fun addElements(elements: List<FriendModel>) {

        val oldList = mData.toList()

        if (mData.isNotEmpty()) {
            if (elements.isNotEmpty())
                elements.last().needSeparator = false
            mData.last().needSeparator = true
        }
        mData.addAll(elements)

        val newList = mData.toList()

        val diffs = FriendDiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffs)

        diffResult.dispatchUpdatesTo(this)
    }

    fun clear() {
        mData.clear()
        notifyDataSetChanged()
    }

    fun removeItem(friend: FriendModel?) {
        Timber.d("removeItem called")
        friend?.let {
            if (mData.isNotEmpty()) {
                for (i in 0 until mData.size)
                    if (mData[i].userModel.userId == friend.userModel.userId) {
                        mData.removeAt(i)
                        //mData.last().needSeparator = true
                        notifyItemRemoved(i)
                        notifyItemRangeChanged(i, mData.size - 1)
                        return@let
                    }
            }
        }
    }

    fun setSearch(isSearch: Boolean) {
        this.isSearch = isSearch
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var model: FriendModel
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val actionContainer: FrameLayout = view.findViewById(R.id.action_icon_container)
        private val ivAction: ImageView = view.findViewById(R.id.ivStatus)
        private val btnActionsContainer: LinearLayout = view.findViewById(R.id.btn_actions_container)
        private val btnActionAccept: TextView = view.findViewById(R.id.tv_action_accept)
        private val btnActionReject: TextView = view.findViewById(R.id.tv_action_reject)
        private var vvImage: VipView = view.findViewById(R.id.vipView_friend_holder)
        private val llContainer: LinearLayout = view.findViewById(R.id.llContent)
        private val tvAgeLoc: TextView = view.findViewById(R.id.tv_user_age_loc)
        private val uniqueNameTextView: TextView = view.findViewById(R.id.uniqueNameTextView)

        private var currentPos = 0

        init {
            llContainer.setOnClickListener {
                itemClickListener?.onItemClicked(model)
                interactor?.onItemClicked(model)
            }

            ivAction.setOnClickListener {
                itemClickListener?.onActionClicked(model)
                interactor?.onActionClicked(model)
            }

            btnActionAccept.setOnClickListener {
                interactor?.onActionButtonsClicked(model, true)
            }

            btnActionReject.setOnClickListener {
                interactor?.onActionButtonsClicked(model, false)
            }
        }

        fun bind(friend: FriendModel, pos: Int) {
            currentPos = pos
            model = friend

            if (isSearch)
                ivAction.setImageResource(R.drawable.ic_delete_friend_gray)
            else {
                when (model.type) {
                    GetFriendsListUseCase.FRIENDS -> {
                        ivAction.setImageResource(R.drawable.ic_delete_friend_gray)
                    }

                    GetFriendsListUseCase.BLACKLIST -> {
                        ivAction.setImageResource(R.drawable.blocked_user_gray)
                    }

                    GetFriendsListUseCase.INCOMING -> {
                        btnActionsContainer.visible()
                        actionContainer.gone()
                    }

                    GetFriendsListUseCase.OUTCOMING -> {
                        ivAction.setImageResource(R.drawable.outgoing_requests_gray)
                    }
                }
            }

            Timber.i(" USER_MODEL: ${model.userModel}")
            vvImage.setUp(
                itemView.context,
                model.userModel.avatar,
                model.userModel.accountType,
                model.userModel.accountColor ?: 0,
                true
            )
            tvName.text = friend.userModel.name
            tvName.enableApprovedIcon(
                enabled = friend.userModel.approved == 1,
                isVip = friend.userModel.accountType != INetworkValues.ACCOUNT_TYPE_REGULAR
            )
            tvAgeLoc.visible()
            model.userModel.birthday?.let {
                if (it == -1L)//логика ios
                    tvAgeLoc.text = ""
                else
                    tvAgeLoc.text = getAge(it)
            } ?: kotlin.run {
                tvAgeLoc.text = ""
            }
            model.userModel.city?.let {
                if (it != "") {
                    if (tvAgeLoc.text != "")
                        tvAgeLoc.text = String.format("${tvAgeLoc.text}, $it")
                    else tvAgeLoc.text = it
                }
            }

            model.userSimple?.uniqueName?.let { uName: String ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    uniqueNameTextView.text = formattedUniqueName
                    uniqueNameTextView.visible()
                } else {
                    uniqueNameTextView.gone()
                }
            } ?: kotlin.run {
                uniqueNameTextView.gone()
            }
        }
    }

    interface IFriendsListInteractor {

        fun onItemClicked(friend: FriendModel)

        fun onActionClicked(friend: FriendModel)

        fun onActionButtonsClicked(friend: FriendModel, isAccept: Boolean)

        fun onLoadMore() {

        }
    }

}
