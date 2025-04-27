package com.numplates.nomera3.modules.moments.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.numplates.nomera3.databinding.FragmentEmptyMusicBinding
import com.numplates.nomera3.modules.music.ui.fragment.AddMusicBottomFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class MomentsMusicFragment : BaseFragmentNew<FragmentEmptyMusicBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEmptyMusicBinding
        get() = FragmentEmptyMusicBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AddMusicBottomFragment.showAddMusicBottomFragment(
            fm = childFragmentManager,
            isAddingMode = true,
        )
    }

}
