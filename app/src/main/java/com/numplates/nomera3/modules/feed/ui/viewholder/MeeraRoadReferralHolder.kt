package com.numplates.nomera3.modules.feed.ui.viewholder

import android.animation.AnimatorInflater
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.referrals.ui.model.ReferralDataUIModel
import com.numplates.nomera3.databinding.ItemRoadReferralBinding
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.divider.IDividedPost

class MeeraRoadReferralHolder(
    private val binding: ItemRoadReferralBinding
) : ViewHolder(binding.root), IDividedPost, PostCallbackHolder {

    override fun isVip() = false

    private var postCallback: MeeraPostCallback? = null

    init {
        binding.btnRoadInviteFriend.setThrottledClickListener { postCallback?.onReferralClicked() }
        binding.root.setThrottledClickListener { postCallback?.onReferralClicked() }
    }

    override fun initCallback(meeraPostCallback: MeeraPostCallback?) {
        this.postCallback = meeraPostCallback
    }

    fun bind(item: PostUIEntity) {
        val referralInfo = item.featureData?.referralInfo ?: return
        setupViews(referralInfo)
        handleReferralProgress(referralInfo)
    }

    fun clearResources() {
        binding.cvReferralActivateVip.stateListAnimator = null
        binding.cvReferralActivateVip.setOnClickListener(null)
        postCallback = null
    }

    private fun setupViews(referralInfo: ReferralDataUIModel) {
        binding.apply {
            tvReferralTitle.text = referralInfo.title
            tvReferralDescription.text = referralInfo.text
        }
    }

    private fun handleReferralProgress(data: ReferralDataUIModel) {
        binding.apply {
            if (data.availableVips > 0) { // limit == count
                showGetVipButton(data)
            } else {
                showPointsView(data)
            }
        }
    }

    private fun showGetVipButton(data: ReferralDataUIModel) {
        binding.apply {
            cvReferralCounter.gone()
            cvReferralActivateVip.visible()
            cvReferralActivateVip.stateListAnimator = AnimatorInflater.loadStateListAnimator(
                root.context,
                com.meera.referrals.R.animator.shadow_animator
            )
            cvReferralActivateVip.setMonths(data.availableVips)
            cvReferralActivateVip.applyRoundedOutline(com.meera.referrals.R.dimen.material6)
            cvReferralActivateVip.setThrottledClickListener { postCallback?.onActivateVipClicked(data) }
        }
    }

    private fun showPointsView(data: ReferralDataUIModel) {
        binding.apply {
            cvReferralActivateVip.gone()
            cvReferralCounter.visible()
            cvReferralCounter.applyRoundedOutline(com.meera.referrals.R.dimen.material6)
            cvReferralCounter.setData(data)
        }
    }

}
