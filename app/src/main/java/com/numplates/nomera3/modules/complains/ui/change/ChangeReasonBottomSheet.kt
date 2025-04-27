package com.numplates.nomera3.modules.complains.ui.change

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.modules.complains.ui.KEY_EXTRA_USER_COMPLAIN
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.user.BaseListFragmentsBottomSheet
import com.meera.core.base.BaseFragment
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE

class ChangeReasonBottomSheet : BaseListFragmentsBottomSheet() {

    override fun getRootFragment(): BaseFragment = ChangeReasonFragment()

    companion object {
        private const val TAG = "tag_change_reason_bottom_sheet"

        fun showInstance(
            fragmentManager: FragmentManager,
            complain: UserComplainUiModel?,
            complainType: Int?
        ) {
            ChangeReasonBottomSheet().apply {
                arguments = bundleOf(
                    KEY_EXTRA_USER_COMPLAIN to complain,
                    KEY_COMPLAIN_TYPE to complainType
                )
                show(fragmentManager, TAG)
            }
        }
    }
}
