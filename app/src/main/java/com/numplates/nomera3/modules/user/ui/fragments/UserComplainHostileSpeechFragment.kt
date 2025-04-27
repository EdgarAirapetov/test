package com.numplates.nomera3.modules.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentComplainHostileSpeechBinding
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import com.numplates.nomera3.modules.user.ui.entity.UserComplainEntity


class UserComplainHostileSpeechFragment :
        BaseUserListComplains<FragmentComplainHostileSpeechBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentComplainHostileSpeechBinding
        get() = FragmentComplainHostileSpeechBinding::inflate

    override fun getRecycler(): RecyclerView? = binding?.rvComplaintReasonList

    override fun getCloseView(): View? = binding?.vgItemMenuCancel

    override fun getListComplains(): List<UserComplainEntity> {
        val entity = mutableListOf<UserComplainEntity>()
        entity.add(UserComplainEntity(getString(R.string.user_complain_hostile_speech_abuse),
                statusId = ComplainReasonId.ABUSE.key))
        entity.add(UserComplainEntity(getString(R.string.user_complain_hostile_speech_violence),
                statusId = ComplainReasonId.VIOLENCE.key))
        entity.add(UserComplainEntity(getString(R.string.user_complain_hostile_speech_dangerous_people),
                statusId = ComplainReasonId.DANGEROUS_PEOPLE.key))
        entity.add(UserComplainEntity(getString(R.string.user_complain_hostile_speech_animal_abuse),
                statusId = ComplainReasonId.ANIMAL_ABUSE.key))
        return entity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.vgBackButton?.click { onBackFragment() }
    }

    override fun scrollTopShadowsVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding?.vShadowTop?.visible()
        } else {
            binding?.vShadowBottom?.invisible()
        }
    }

    override fun scrollBottomShadowVisibility(isVisible: Boolean) {
        /** STUB */
    }
}
