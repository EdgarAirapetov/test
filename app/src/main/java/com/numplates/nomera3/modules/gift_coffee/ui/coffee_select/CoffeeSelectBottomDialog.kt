package com.numplates.nomera3.modules.gift_coffee.ui.coffee_select

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.GiftEntity
import com.numplates.nomera3.databinding.BottomSheetCoffeeGiftBinding
import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType
import com.numplates.nomera3.modules.gift_coffee.data.entity.PromoCodeEntity
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.invisible
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.graphics.DrawableAlwaysCrossFadeFactory

private const val ILLEGAL_STATE_BINDING_NOT_CREATED = "BottomSheetCoffeeGiftBinding wasn't created"
private const val ILLEGAL_STATE_PARENT_FRAGMENT_UNDEFINED =
    "Parent Fragment should implement CoffeeGiftBottomDialogCallback"
private const val COFFEE_DISABLE_BUTTON_ALPHA = 0.3f
private const val COFFEE_ENABLE_BUTTON_ALPHA = 1.0f
private const val GIFT = "coffee_gift"

class CoffeeGiftBottomDialog : BottomSheetDialogFragment() {

    private lateinit var gift: GiftEntity

    private var binding: BottomSheetCoffeeGiftBinding? = null

    private val viewModel by viewModels<CoffeeSelectViewModel>()

    private fun getBinding(): BottomSheetCoffeeGiftBinding =
        binding ?: error(ILLEGAL_STATE_BINDING_NOT_CREATED)

    override fun getTheme(): Int =
        R.style.BottomSheetDialogTransparentTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme).apply {
            setOnShowListener {
                forceFullOpen()
            }

            closeOnCollapsed()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        binding = BottomSheetCoffeeGiftBinding.inflate(inflater, container, false)

        gift = arguments?.getSerializable(GIFT) as GiftEntity

        return getBinding().root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(this) { state ->
            render(state)
        }

        getBinding().coffeeSelectGetCodeButton.setOnClickListener {
            getBinding().coffeeSelectGetCodeButton.clickAnimate()
            viewModel.applySelect(gift)
        }

        initState()
    }

    private fun initCoffeeItemsListeners() {
        with(getBinding()) {
            coffeeSelectCappuccino.selectListener { viewModel.selectCoffee(CoffeeType.CAPPUCCINO) }
            coffeeSelectLatte.selectListener { viewModel.selectCoffee(CoffeeType.LATTE) }
            coffeeSelectRaf.selectListener { viewModel.selectCoffee(CoffeeType.RAF) }
        }
    }

    private fun removeCoffeeItemsListeners() {
        with(getBinding()) {
            coffeeSelectCappuccino.removeListener()
            coffeeSelectLatte.removeListener()
            coffeeSelectRaf.removeListener()
        }
    }

    private fun CoffeeButtonCustomView.removeListener() {
        setOnClickListener(null)
    }

    private fun CoffeeButtonCustomView.selectListener(callback: (CoffeeButtonCustomView) -> Unit) {
        setOnClickListener { currentCoffeeButton ->
            deselectAllCoffee()
            (currentCoffeeButton as CoffeeButtonCustomView).select()
            callback.invoke(currentCoffeeButton)
        }
    }

    private fun deselectAllCoffee() {
        with(getBinding()) {
            coffeeSelectCappuccino.regular()
            coffeeSelectLatte.regular()
            coffeeSelectRaf.regular()
        }
    }

    private fun render(state: CoffeeSelectState) {
        when (state) {
            is CoffeeSelectState.Loading -> loadingState()
            is CoffeeSelectState.Init -> initState()
            is CoffeeSelectState.Selected -> selectedState(state.type)
            is CoffeeSelectState.Success -> successState(state.coffeeType, state.promocode)
            is CoffeeSelectState.Error -> errorState(state.exception)
        }
    }

    private fun loadingState() {
        removeCoffeeItemsListeners()

        with(getBinding()) {
            coffeeSelectItemsContainer.isEnabled = false
            coffeeSelectErrorLabel.invisible()
            coffeeSelectGetCodeButton.alpha = COFFEE_ENABLE_BUTTON_ALPHA
            coffeeSelectGetCodeButton.isEnabled = false
            coffeeSelectLoader.visible()
        }
    }

    private fun initState() {
        initCoffeeItemsListeners()

        with(getBinding()) {
            coffeeSelectItemsContainer.isEnabled = false
            coffeeSelectErrorLabel.invisible()
            coffeeSelectGetCodeButton.alpha = COFFEE_DISABLE_BUTTON_ALPHA
            coffeeSelectGetCodeButton.isEnabled = false
            coffeeSelectLoader.gone()
        }
    }

    private fun selectedState(coffeeType: CoffeeType) {
        initCoffeeItemsListeners()

        with(getBinding()) {
            changeCoffeeBigImage(coffeeType.bigResource)
            coffeeSelectItemsContainer.isEnabled = false
            coffeeSelectErrorLabel.invisible()
            coffeeSelectGetCodeButton.alpha = COFFEE_ENABLE_BUTTON_ALPHA
            coffeeSelectGetCodeButton.isEnabled = true
            coffeeSelectLoader.gone()
        }
    }

    private fun successState(coffeeType: CoffeeType, promocode: PromoCodeEntity) {
        (parentFragment as? CoffeeGiftBottomDialogCallback)?.onCoffeeSelect(
            gift,
            promocode,
            coffeeType
        )
            ?: error(ILLEGAL_STATE_PARENT_FRAGMENT_UNDEFINED)

        dismiss()
    }

    private fun errorState(exception: Throwable) {
        initState()
        getBinding().coffeeSelectErrorLabel.visible()

        FirebaseCrashlytics.getInstance().recordException(exception)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }

    private fun changeCoffeeBigImage(@DrawableRes coffeeDrawable: Int) {
        Glide.with(requireContext())
            .load(coffeeDrawable)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .priority(Priority.IMMEDIATE)
            .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
            .into(getBinding().coffeeSelectImage)
    }

    private fun BottomSheetDialog.closeOnCollapsed() {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) dismiss()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
    }

    private fun BottomSheetDialog.forceFullOpen() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    companion object {
        private const val TAG = "CoffeeGiftBottomDialog"

        fun show(fragmentManager: FragmentManager, gift: GiftEntity) {
            val dialog = CoffeeGiftBottomDialog()
            dialog.arguments = bundleOf(GIFT to gift)
            dialog.show(fragmentManager, TAG)
        }
    }

    interface CoffeeGiftBottomDialogCallback {
        fun onCoffeeSelect(gift: GiftEntity, promocode: PromoCodeEntity, type: CoffeeType)
    }
}
