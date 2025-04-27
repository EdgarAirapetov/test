package com.numplates.nomera3.presentation.birthday.ui

import android.animation.Animator
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.RenderMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentBirthdayBottomDialogBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import timber.log.Timber

/**
 * Данный диалог будет показывать поздравление с Днем Рождения
 */
class BirthdayBottomDialogFragment :
    BaseBottomSheetDialogFragment<FragmentBirthdayBottomDialogBinding>() {

    private var dismissListener: (() -> Unit)? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    /**
     * Анимация будет показывать фейерверк, когда юзер получит диалог прздравления
     * Не могу создать объект в xml. Т.к анимация покажется на уровне диалога
     */
    private var lottieFireworkAnimation: LottieAnimationView? = null

    override val bindingInflater: (
        LayoutInflater, ViewGroup?, Boolean
    ) -> FragmentBirthdayBottomDialogBinding
        get() = FragmentBirthdayBottomDialogBinding::inflate

    /**
     * Создаем LayoutParams, устанавливаем показ анимации фейерверка в decorView.
     * Кейс в том, что не могу использовать анимацию, которая хранится в
     * [com.numplates.nomera3.Act] т.к анимация будет показываться под диалогом.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = FrameLayout(requireContext())
        lottieFireworkAnimation = createFireworkLottieView()
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val dialog = BottomSheetDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val decorViewLp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.window?.addContentView(lottieFireworkAnimation, decorViewLp)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createListeners()
        createBehavior()
        getActionType()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

    override fun onStart() {
        super.onStart()
        showDelayedFireworkAnim()
    }

    fun setOnDismissListener(listener: () -> Unit) {
        this.dismissListener = listener
    }

    fun show(manager: FragmentManager) {
        if (manager.findFragmentByTag(TAG) == null) {
            super.showNow(manager, TAG)
        }
    }

    private fun createListeners() {
        binding?.tvBtnOk?.setOnClickListener {
            dismiss()
        }
        binding?.ivClose?.setOnClickListener {
            dismiss()
        }

        binding?.lvBirthdayIcon?.addAnimatorListener(lottieAnimatorListener)
    }

    private val lottieAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator) {
            disableDismissDialog()
        }

        override fun onAnimationEnd(p0: Animator) {
            enableDismissDialog()
        }

        override fun onAnimationCancel(p0: Animator) = Unit
        override fun onAnimationRepeat(p0: Animator) = Unit
    }

    private fun disableDismissDialog() {
        isCancelable = false
        binding?.ivClose?.isEnabled = false
        binding?.tvBtnOk?.isEnabled = false
    }

    private fun enableDismissDialog() {
        isCancelable = true
        binding?.ivClose?.isEnabled = true
        binding?.tvBtnOk?.isEnabled = true
    }

    private fun createBehavior() {
        binding?.vgBirthdayRoot?.let {
            val dialog = dialog as BottomSheetDialog
            val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            mainContainer?.let { frameLayout ->
                bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun getActionType() {
        arguments?.let { args ->
            val typeAction = args.getString(KEY_BIRTHDAY_TYPE)
            Timber.d("Birthday action type: $typeAction")
            typeAction?.let { action ->
                setDialogActionText(action)
            }
        }
    }

    private fun setDialogActionText(typeAction: String) {
        when (typeAction) {
            ACTION_TODAY_IS_BIRTHDAY -> {
                val stringsArr = context?.resources?.getStringArray(R.array.birthdays)
                stringsArr?.let { array ->
                    binding?.tvBirthdayTitle?.text =
                        context?.getString(R.string.happy_birthday_dialog)
                    binding?.tvDesc?.text = array.random()
                }
            }
            ACTION_YESTERDAY_IS_BIRTHDAY -> {
                binding?.tvBirthdayTitle?.text = context?.getString(R.string.past_birthday)
            }
        }
    }

    private fun showFireworksAnim(actionAnimationEnd: (() -> Unit)? = null) {
        val animationView = lottieFireworkAnimation ?: return
        if (!animationView.isAnimating) {
            animationView.visible()
            animationView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    animationView.gone()
                    actionAnimationEnd?.invoke()
                }
            })
            animationView.playAnimation()
            requireContext().vibrate()
        }
    }

    private fun showDelayedFireworkAnim() {
        doDelayed(SHOW_FIREWORK_ANIM_DELAY) {
            showFireworksAnim()
        }
    }

    /**
     * Создаем программно LottieView, которая будет показывать фейерверк
     */
    private fun createFireworkLottieView(): LottieAnimationView {
        val animationView = LottieAnimationView(context)
        animationView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        animationView.id = ViewCompat.generateViewId()
        animationView.setAnimation(R.raw.fireworks)
        animationView.scaleType = ImageView.ScaleType.CENTER_CROP
        animationView.setRenderMode(RenderMode.HARDWARE)
        return animationView
    }

    companion object {
        const val ACTION_TODAY_IS_BIRTHDAY = "ACTION_TODAY_IS_BIRTHDAY"
        const val ACTION_YESTERDAY_IS_BIRTHDAY = "ACTION_YESTERDAY_IS_BIRTHDAY"
        const val ACTION_DEFAULT = "ACTION_DEFAULT"
        private const val TAG = "BirthdayBottomDialogFragment"
        private const val KEY_BIRTHDAY_TYPE = "KEY_BIRTHDAY_TYPE"
        private const val SHOW_FIREWORK_ANIM_DELAY = 100L

        @JvmStatic
        fun create(actionType: String = ACTION_DEFAULT): BirthdayBottomDialogFragment {
            val args = bundleOf(KEY_BIRTHDAY_TYPE to actionType)
            val instance = BirthdayBottomDialogFragment()
            instance.arguments = args

            return instance
        }
    }
}
