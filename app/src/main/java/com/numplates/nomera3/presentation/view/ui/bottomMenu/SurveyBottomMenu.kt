package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.simpleName
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.SurveyBottomMenuLayoutBinding
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class SurveyBottomMenu : BaseBottomSheetDialogFragment<SurveyBottomMenuLayoutBinding>() {

    /*
    * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2288550188/-#Создание-поста-в-сообществе
    * если isCommunityCommentingOptionMode == true,
    * то нижнее диалоговое меню открыто при создании
    * нового поста для группы/сообщества и следующие
    * варианты кто может комментировать пост:
    *    - Все
    *    - Участники
    *    - Никто
    * иначе:
    *    - Все
    *    - Друзья
    *    - Никто
    * */
    var isCommunityCommentingOptionMode: Boolean = false

    var allClickedListener: () -> Unit = {}
    var noOneClickedListener: () -> Unit = {}
    var friendsClickedListener: () -> Unit = {}

    var state = WhoCanCommentPostEnum.EVERYONE
    var isRoad = true
    var isEvent = false


    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()

        when (state) {
            WhoCanCommentPostEnum.EVERYONE -> {
                allClicked()
            }
            WhoCanCommentPostEnum.NOBODY -> {
                noOneClicked()
            }
            WhoCanCommentPostEnum.COMMUNITY_MEMBERS,
            WhoCanCommentPostEnum.FRIENDS -> {
                friendsClicked()
            }
        }
        initClickListeners()
    }


    private fun setupView() {
        if (isCommunityCommentingOptionMode) {
            binding?.tvFriendsOnly?.text = getString(R.string.community_member_can_comment_post_option)
        } else {
            if (!isRoad) {
                binding?.flFriendsContainer?.gone()
                binding?.flAllContainer?.post {
                    val params = binding?.flAllContainer?.layoutParams as? LinearLayout.LayoutParams
                    params?.marginEnd = 20.dp
                    binding?.flAllContainer?.layoutParams = params
                }
            }
        }
        binding?.tvSurveyBottomMenuTitle?.setText(
            if (isEvent) R.string.who_can_comment_event else R.string.who_can_comment
        )
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null)
            return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    private fun initClickListeners() {
        binding?.tvAll?.setOnClickListener {
            allClicked()
        }

        binding?.tvFriendsOnly?.setOnClickListener {
            friendsClicked()
        }

        binding?.tvNoOne?.setOnClickListener {
            noOneClicked()
        }
    }

    private fun allClicked() {
        context?.let {
            //setup background
            binding?.flAllContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_purple_gradient)
            binding?.flFriendsContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_white_round_corners)
            binding?.flNoOneContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_white_round_corners)

            //setup text color
            binding?.tvAll?.setTextColor(Color.WHITE)
            binding?.tvFriendsOnly?.setTextColor(ContextCompat.getColor(it, R.color.ui_gray))
            binding?.tvNoOne?.setTextColor(ContextCompat.getColor(it, R.color.ui_gray))
            allClickedListener()
        }
    }

    private fun friendsClicked() {
        context?.let {
            binding?.flAllContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_white_round_corners)
            binding?.flFriendsContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_purple_gradient)
            binding?.flNoOneContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_white_round_corners)

            //setup text color
            binding?.tvAll?.setTextColor(ContextCompat.getColor(it, R.color.ui_gray))
            binding?.tvFriendsOnly?.setTextColor(Color.WHITE)
            binding?.tvNoOne?.setTextColor(ContextCompat.getColor(it, R.color.ui_gray))
            friendsClickedListener()
        }
    }

    private fun noOneClicked() {
        context?.let {
            binding?.flAllContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_white_round_corners)
            binding?.flFriendsContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_white_round_corners)
            binding?.flNoOneContainer?.background = ContextCompat.getDrawable(it, R.drawable.stroke_purple_gradient)

            //setup text color
            binding?.tvAll?.setTextColor(ContextCompat.getColor(it, R.color.ui_gray))
            binding?.tvFriendsOnly?.setTextColor(ContextCompat.getColor(it, R.color.ui_gray))
            binding?.tvNoOne?.setTextColor(Color.WHITE)
            noOneClickedListener()
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> SurveyBottomMenuLayoutBinding
        get() = SurveyBottomMenuLayoutBinding::inflate

}
