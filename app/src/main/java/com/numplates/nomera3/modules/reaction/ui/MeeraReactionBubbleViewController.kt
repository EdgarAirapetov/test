package com.numplates.nomera3.modules.reaction.ui

import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.meera.core.extensions.invisible
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.utils.layouts.intercept.InterceptTouchFrameLayout
import com.meera.core.utils.layouts.intercept.InterceptTouchLayout
import com.numplates.nomera3.ActActions
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactionsParams
import com.numplates.nomera3.modules.featuretoggles.TimeOfDayReactionsFeatureToggle
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.data.net.isMine
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionBubble
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.ReactionEvent

class MeeraReactionBubbleViewController {

    private var actionHandler: ((ActActions) -> Unit)? = null

    var onReactionBubbleShow: () -> Unit? = {}
    var onReactionBubbleHide: () -> Unit? = {}
    private var currentReactionBubble: MeeraReactionBubble? = null

    private var act: MeeraAct? = null
    private var viewsToHide: List<View>? = null
    private var reactionTip: View? = null
    private lateinit var timeOfDayReactionsFeatureToggle: TimeOfDayReactionsFeatureToggle
    private var containerInfo: ContainerInfo? = null


    fun init(
        timeOfDayReactionsFeatureToggle: TimeOfDayReactionsFeatureToggle,
        act: MeeraAct,
        handleAction: (action: ActActions) -> Unit
    ) {
        this.act = act
        this.actionHandler = handleAction
        this.timeOfDayReactionsFeatureToggle = timeOfDayReactionsFeatureToggle
    }

    fun onEvent(event: ReactionEvent) {
        when (event) {
            is ReactionEvent.ShowAlert -> {
                NToast.with(act?.getRootView())
                    .typeAlert()
                    .text(event.message)
                    .show()
            }

            is ReactionEvent.Error -> {
                NToast.with(act?.getRootView())
                    .typeError()
                    .text(event.message)
                    .show()
            }

            is ReactionEvent.UnknownError -> {
                val message = act?.resources?.getString(R.string.reaction_unknown_error).toString()
                NToast.with(act?.getRootView())
                    .typeError()
                    .text(message)
                    .show()
            }
        }
    }

    fun forceAddReaction(
        reactionSource: MeeraReactionSource,
        commentReactionList: List<ReactionEntity>,
        selectedReactionType: ReactionType,
        reactionsParams: AmplitudeReactionsParams? = null
    ) = onReactionSelect(
        reactionSource = reactionSource,
        reactionList = commentReactionList,
        selectedReactionType = selectedReactionType,
        reactionsParams = reactionsParams,
        isForceAdd = true
    )

    fun showReactionBubble(
        reactionSource: MeeraReactionSource,
        showPoint: Point,
        viewsToHide: List<View>,
        reactionTip: TextView,
        currentReactionsList: List<ReactionEntity>,
        contentActionBarType: MeeraContentActionBar.ContentActionBarType,
        containerInfo: ContainerInfo,
        reactionsParams: AmplitudeReactionsParams? = null,
        isForceAdd: Boolean = false,
        isMoveUpAnimationEnabled: Boolean = true,
        showMorningEvening: Boolean = true,
        postedAt: Long? = null
    ) {
        val activity = act ?: return
        NavigationManager.getManager().topNavController.needAuthToNavigate(activity) {
            this.viewsToHide = viewsToHide
            this.reactionTip = reactionTip
            this.containerInfo = containerInfo

            onBubbleShow(viewsToHide = viewsToHide, reactionTip = reactionTip)
            actionHandler?.invoke(ActActions.OnShowReactionBubble)

            val isMorningEveningEnabled = if (postedAt != null) {
                ReactionType.isMorningEveningActual(postedAt) && showMorningEvening
            } else {
                showMorningEvening
            }

            onBubbleShow(viewsToHide = viewsToHide, reactionTip = reactionTip)
            currentReactionBubble = MeeraReactionBubble.show(
                position = showPoint,
                container = containerInfo.container,
                contentActionBarType = contentActionBarType,
                isMoveUpAnimationEnabled = isMoveUpAnimationEnabled,
                showMorningEvening = isMorningEveningEnabled && timeOfDayReactionsFeatureToggle.isEnabled,
                selectListener = { reactionType ->
                    onReactionSelect(
                        reactionSource = reactionSource,
                        reactionList = currentReactionsList,
                        selectedReactionType = reactionType,
                        reactionsParams = reactionsParams,
                        isForceAdd = isForceAdd
                    )
                    onBubbleDismiss(
                        viewsToHide = viewsToHide,
                        reactionTip = reactionTip
                    )
                },
                hideListener = {
                    onBubbleDismiss(
                        viewsToHide = viewsToHide,
                        reactionTip = reactionTip
                    )
                }
            )
            onReactionBubbleShow.invoke()

            blockTouchesInRecyclerView()
        }
    }

    fun hideReactionBubble() {
        val bubbleViewsToHide = viewsToHide ?: return
        val bubbleReactionTip = reactionTip ?: return
        onBubbleDismiss(bubbleViewsToHide, bubbleReactionTip, false)
        viewsToHide = null
        reactionTip = null
        containerInfo = null
    }

    fun onSelectDefaultReaction(
        reactionSource: MeeraReactionSource,
        currentReactionsList: List<ReactionEntity>,
        reactionsParams: AmplitudeReactionsParams? = null,
        forceDefault: Boolean = false,
        isShouldVibrate: Boolean = true,
        isNeedToCheckAuthInside: Boolean = true
    ) {
        if (!isNeedToCheckAuthInside) {
            onSelectDefaultReactionActions(reactionSource, currentReactionsList, reactionsParams, forceDefault, isShouldVibrate)
            return
        }
        val activity = act ?: return
        NavigationManager.getManager().topNavController.needAuthToNavigate(activity) {
            onSelectDefaultReactionActions(reactionSource, currentReactionsList, reactionsParams, forceDefault, isShouldVibrate)
        }
    }

    private fun onSelectDefaultReactionActions(
        reactionSource: MeeraReactionSource,
        currentReactionsList: List<ReactionEntity>,
        reactionsParams: AmplitudeReactionsParams? = null,
        forceDefault: Boolean = false,
        isShouldVibrate: Boolean = true
    ) {
        val defaultReaction = ReactionType.GreenLight
        val userReaction = currentReactionsList.find { it.isMine() }
            ?.let { ReactionType.getByString(it.reactionType) }
        if (userReaction != null) {
            if (forceDefault) {
                actionHandler?.invoke(
                    ActActions.AddReactionMeera(
                        reactionSource = reactionSource,
                        currentReactionList = currentReactionsList,
                        reaction = defaultReaction,
                        reactionsParams = reactionsParams
                    )
                )
                if (isShouldVibrate) act?.vibrate()
            } else {
                actionHandler?.invoke(
                    ActActions.RemoveReactionMeera(
                        reactionSource = reactionSource,
                        currentReactionList = currentReactionsList,
                        reactionToRemove = userReaction,
                        reactionsParams = reactionsParams
                    )
                )
            }
        } else {
            actionHandler?.invoke(
                ActActions.AddReactionMeera(
                    reactionSource = reactionSource,
                    currentReactionList = currentReactionsList,
                    reaction = defaultReaction,
                    reactionsParams = reactionsParams
                )
            )
            if (isShouldVibrate) act?.vibrate()
        }
    }

    private fun onReactionSelect(
        reactionSource: MeeraReactionSource,
        reactionList: List<ReactionEntity>,
        selectedReactionType: ReactionType,
        reactionsParams: AmplitudeReactionsParams?,
        isForceAdd: Boolean = false
    ) {
        val userAlreadyHaveReaction = reactionList.find { it.isMine() }
            ?.let { ReactionType.getByString(it.reactionType) }
        when {
            userAlreadyHaveReaction != selectedReactionType -> actionHandler?.invoke(
                ActActions.AddReactionMeera(
                    reactionSource = reactionSource,
                    currentReactionList = reactionList,
                    reaction = selectedReactionType,
                    reactionsParams = reactionsParams,
                    isFromBubble = true
                )
            )

            isForceAdd -> actionHandler?.invoke(
                ActActions.RemoveReactionMeera(
                    reactionSource = reactionSource,
                    currentReactionList = reactionList,
                    reactionToRemove = selectedReactionType,
                    reactionsParams = reactionsParams
                )
            )
        }
    }

    private fun onBubbleShow(
        viewsToHide: List<View>,
        reactionTip: View,
        useDelay: Boolean = true
    ) {
        hideCurrentReactionBubble(useDelay)
        hideCommentViews(viewsToHide)
        showReactionTip(reactionTip)
    }

    private fun onBubbleDismiss(
        viewsToHide: List<View>,
        reactionTip: View,
        useDelay: Boolean = true
    ) {
        showCommentViews(viewsToHide)
        hideReactionTip(reactionTip)
        hideCurrentReactionBubble(useDelay)
        unblockTouchesInRecyclerView()
        onReactionBubbleHide.invoke()
    }

    private fun blockTouchesInRecyclerView() {
        val bypassLayouts = containerInfo?.bypassLayouts ?: emptyList()
        bypassLayouts.forEach { interceptTouchLayout ->
            interceptTouchLayout.setReactionTouchEventPasser()
        }
    }

    private fun InterceptTouchLayout.setReactionTouchEventPasser() {
        bypassTouches(dispatchEventPasser = {
            currentReactionBubble?.dispatchTouchEvent(it) ?: false
        })
    }

    private fun InterceptTouchLayout.removeReactionTouchEventPasser() {
        bypassTouches(dispatchEventPasser = null)
    }

    private fun unblockTouchesInRecyclerView() {
        val bypassLayouts = containerInfo?.bypassLayouts ?: emptyList()
        bypassLayouts.forEach { blockTouchComponent ->
            blockTouchComponent.removeReactionTouchEventPasser()
        }
    }

    private fun hideCurrentReactionBubble(useDelay: Boolean) {
        currentReactionBubble?.hide(useDelay)
        currentReactionBubble = null
    }

    private fun showCommentViews(viewsToHide: List<View>) {
        viewsToHide.forEach { view ->
            view.visible()
        }
    }

    private fun hideCommentViews(viewsToHide: List<View>) {
        viewsToHide.forEach { view ->
            view.invisible()
        }
    }

    private fun showReactionTip(tipView: View) {
        tipView.visible()
    }

    private fun hideReactionTip(tipView: View) {
        tipView.invisible()
    }

    /**
     * С помощью этого класса можно задать кастомный контейнер для плашки реакций
     * (например это нужно для отображения плашки реакций в диалоговом-окне, так как диалог отображается выше дефолтного контейнера для плашки с реакциями)
     *
     * @param container – контейнер на который будет аттачится плашка
     * @param bypassLayouts – контейнеры которые будет передавать все свои touch-события в плашку реакций
     */
    data class ContainerInfo(
        val container: ViewGroup,
        val bypassLayouts: List<InterceptTouchLayout>,
    )
}

fun MeeraAct.getDefaultReactionContainer(): MeeraReactionBubbleViewController.ContainerInfo {
    val interceptLayout = findViewById<InterceptTouchFrameLayout>(R.id.root_intercept_layout_activity)
    return MeeraReactionBubbleViewController.ContainerInfo(
        container = interceptLayout,
        bypassLayouts = listOf(interceptLayout)
    )
}
