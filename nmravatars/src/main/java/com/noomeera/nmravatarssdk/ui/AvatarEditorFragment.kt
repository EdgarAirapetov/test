package com.noomeera.nmravatarssdk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.noomeera.nmravatarssdk.*
import com.noomeera.nmravatarssdk.data.AssetsConfig
import com.noomeera.nmravatarssdk.data.AvatarState
import com.noomeera.nmravatarssdk.data.AvatarStateAsset
import com.noomeera.nmravatarssdk.databinding.FragmentEditorMainBinding
import com.noomeera.nmravatarssdk.extensions.SvgCache
import com.noomeera.nmravatarssdk.ui.adapters.*
import com.noomeera.nmravatarssdk.ui.view.CustomSwitch
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

const val COLUMNS_COUNT = 4


class AvatarEditorFragment(override val layout: Int = R.layout.fragment_editor_main) :
    BaseFragment() {

    private lateinit var binding: FragmentEditorMainBinding

    private val builder: AlertDialog.Builder by lazy {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.ALERT_DIALOG_EXIT_TITLE)
            setMessage(R.string.ALERT_DIALOG_EXIT_MESSAGE)
            negativeButton(getString(R.string.ALERT_DIALOG_EXIT_CANCEL)) { _, _ -> }
            positiveButton(getString(R.string.ALERT_DIALOG_EXIT_CONFIRM)) { backToRoot() }
        }
    }

    private val dialog: AlertDialog by lazy {
        builder.create()
    }

    private lateinit var assetsConfig: AssetsConfig

    private fun generateCategoriesItems(woman: Boolean): List<CategoriesItem> {
        val assetCategories = if (woman) assetsConfig.woman else assetsConfig.man
        return listOfNotNull(
            CategoriesItem(
                R.string.PART_SELECTOR_HEAD,
                R.drawable.ic_head,
                R.drawable.ic_head_selected,
                assetCategories.head
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_HAIR,
                R.drawable.ic_hair,
                R.drawable.ic_hair_selected,
                assetCategories.hair
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_EYES,
                R.drawable.ic_eyes,
                R.drawable.ic_eyes_selected,
                assetCategories.eyes
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_EYEBROWS,
                R.drawable.ic_eyebrows,
                R.drawable.ic_eyebrows_selected,
                assetCategories.eyebrows
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_NOSE,
                R.drawable.ic_nose,
                R.drawable.ic_nose_selected,
                assetCategories.nose
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_MOUTH,
                R.drawable.ic_mouth,
                R.drawable.ic_mouth_selected,
                assetCategories.mouth
            ),
            if (woman) null else
                CategoriesItem(
                    R.string.PART_SELECTOR_BEARD,
                    R.drawable.ic_beard,
                    R.drawable.ic_beard_selected,
                    assetCategories.beard
                ),
            CategoriesItem(
                R.string.PART_SELECTOR_SHIRT,
                R.drawable.ic_shirt,
                R.drawable.ic_shirt_selected,
                assetCategories.shirt
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_HAT,
                R.drawable.ic_hat,
                R.drawable.ic_hat_selected,
                assetCategories.hat
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_ACCSESSORIES,
                R.drawable.ic_accessoires,
                R.drawable.ic_accessoires_selected,
                assetCategories.accessories
            ),
            CategoriesItem(
                R.string.PART_SELECTOR_BACKGROUND,
                R.drawable.ic_color,
                R.drawable.ic_color_selected,
                assetCategories.background
            )
        )
    }

    private lateinit var manCategoriesItem: List<CategoriesItem>
    private lateinit var womanCategoriesItem: List<CategoriesItem>

    private val manStates: MutableList<AvatarState> = mutableListOf()
    private val manUndoedStates: MutableList<AvatarState> = mutableListOf()
    private val manInitStates: MutableList<AvatarState> = mutableListOf()
    private val womanStates: MutableList<AvatarState> = mutableListOf()
    private val womanUndoedStates: MutableList<AvatarState> = mutableListOf()
    private val womanInitStates: MutableList<AvatarState> = mutableListOf()

    private fun states() = if (man()) manStates else womanStates
    private fun initStates() = if (man()) manInitStates else womanInitStates
    private fun undoedStates() = if (man()) manUndoedStates else womanUndoedStates

    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var itemsAdapter: ItemsAdapter
    private lateinit var colorsAdapter: ColorsAdapter

    private val updatesCount = AtomicInteger(0)

    private fun assetForCategory(selectedCategoryType: Int) = when (selectedCategoryType) {
        R.string.PART_SELECTOR_HEAD -> states().last().assets.head
        R.string.PART_SELECTOR_HAIR -> states().last().assets.hair
        R.string.PART_SELECTOR_EYES -> states().last().assets.eyes
        R.string.PART_SELECTOR_EYEBROWS -> states().last().assets.eyebrows
        R.string.PART_SELECTOR_NOSE -> states().last().assets.nose
        R.string.PART_SELECTOR_MOUTH -> states().last().assets.mouth
        R.string.PART_SELECTOR_BEARD -> states().last().assets.beard
        R.string.PART_SELECTOR_SHIRT -> states().last().assets.shirt
        R.string.PART_SELECTOR_BACKGROUND -> states().last().assets.background
        R.string.PART_SELECTOR_ACCSESSORIES -> states().last().assets.accessories
        R.string.PART_SELECTOR_HAT -> states().last().assets.hat
        else -> throw RuntimeException()
    }

    private fun copyLastStateWithAsset(
        typeResource: Int,
        avatarStateAsset: AvatarStateAsset
    ): AvatarState {
        val lastState = states().last()
        return when (typeResource) {
            R.string.PART_SELECTOR_HEAD -> lastState.copy(
                assets = lastState.assets.copy(
                    head = avatarStateAsset
                )
            )
            R.string.PART_SELECTOR_HAIR -> lastState.copy(
                assets = lastState.assets.copy(
                    hair = avatarStateAsset
                )
            )
            R.string.PART_SELECTOR_EYES -> lastState.copy(
                assets = lastState.assets.copy(
                    eyes = avatarStateAsset
                )
            )
            R.string.PART_SELECTOR_EYEBROWS -> lastState.copy(
                assets = lastState.assets
                    .copy(eyebrows = avatarStateAsset)
            )
            R.string.PART_SELECTOR_NOSE -> lastState.copy(
                assets = lastState.assets.copy(
                    nose = avatarStateAsset
                )
            )
            R.string.PART_SELECTOR_MOUTH -> lastState.copy(
                assets = lastState.assets.copy(
                    mouth = avatarStateAsset
                )
            )
            R.string.PART_SELECTOR_BEARD -> lastState.copy(
                assets = lastState.assets.copy(
                    beard = avatarStateAsset
                )
            )
            R.string.PART_SELECTOR_SHIRT -> lastState.copy(
                assets = lastState.assets.copy(
                    shirt = avatarStateAsset
                )
            )
            R.string.PART_SELECTOR_BACKGROUND -> lastState.copy(
                assets = lastState.assets
                    .copy(background = avatarStateAsset)
            )
            R.string.PART_SELECTOR_ACCSESSORIES -> lastState.copy(
                assets = lastState.assets
                    .copy(accessories = avatarStateAsset)
            )
            R.string.PART_SELECTOR_HAT -> lastState.copy(
                assets = lastState.assets
                    .copy(hat = avatarStateAsset)
            )
            else -> lastState.copy(assets = lastState.assets.copy())
        }
    }

    private fun update() {
        try {

            val categories = if (man()) manCategoriesItem else womanCategoriesItem
            val categoriesItem = categories.find {
                it.typeResource == categoriesAdapter.selectedCategoryTypeResource
            } ?: categories.first()
            val selectedCategoryType = categoriesItem.typeResource

            binding.vPanelTitle.setText(selectedCategoryType)
            categoriesAdapter.submitList(categories)
            categoriesAdapter.setSelectedCategoryTypeResource(selectedCategoryType)

            if (!(categoriesItem.assetSets.containsAll(itemsAdapter.currentList) && itemsAdapter.currentList.containsAll(
                    categoriesItem.assetSets
                ))
            )
                itemsAdapter.submitList(categoriesItem.assetSets)

            if (states().isEmpty()) {
                return
            }

            val selectedAvatarStateAsset = assetForCategory(selectedCategoryType)

            assetsConfig.allAssetSets().find { it.id == selectedAvatarStateAsset.id }
                ?.let { assetSet ->
                    assetSet.variants?.let { coloredAssets ->
                        colorsAdapter.submitList(coloredAssets.map {
                            ColorsItem(
                                it.color
                            )
                        }.distinct())
                    }
                    if (assetSet.variants == null)
                        colorsAdapter.submitList(listOf())
                    selectedAvatarStateAsset.color?.apply {
                        colorsAdapter.selectedColorId = this
                    }
                    binding.vColorSelector.visibility =
                        if (assetSet.variants == null) View.GONE else View.VISIBLE
                }

            val lastState = states().last()
            itemsAdapter.setState(lastState)

            computationThreadPool.execute {
                val oldUpdatesCount = updatesCount.getAndIncrement() + 1
                val assets = lastState.allAvatarStateAssets().map {
                    assetsConfig.layeredSvgsFromStateAsset(requireContext(), it)
                }.flatten()
                binding.vAvatarView.post {
                    if (oldUpdatesCount == updatesCount.get())
                        binding.vAvatarView.setAssets(assets)
                }
            }
        } catch (e: Error) {
            e.printStackTrace()
        }
    }

    private fun selectCategory(categoriesItem: CategoriesItem) {
        categoriesAdapter.setSelectedCategoryTypeResource(categoriesItem.typeResource)
        update()
    }

    private fun moveCategorySelector(forward: Boolean) {
        val categories = if (man()) manCategoriesItem else womanCategoriesItem
        val categoriesItem = categories.find {
            it.typeResource == categoriesAdapter.selectedCategoryTypeResource
        } ?: categories.first()

        val selectedCategoryTypeIndex = categories.indexOf(categoriesItem)

        var newIndex = selectedCategoryTypeIndex + if (forward) 1 else -1
        if (newIndex > categories.size - 1)
            newIndex = categories.size - 1
        if (newIndex < 0)
            newIndex = 0
        categoriesAdapter.setSelectedCategoryTypeResource(categories[newIndex].typeResource)
        binding.vCategorySelector.layoutManager?.scrollToPosition(newIndex)
        update()
    }

    private fun randomAvatar() {
        states().add(assetsConfig.randomAvatarState(!man()))
        undoedStates().clear()
        update()
    }


    private fun cacheColorAssets() {
        assetsConfig.allColors().forEach {
            SvgCache.cache("${NMRAvatarsSDK.getResourcesDirPath(requireContext())}/${it.path}")
        }
    }

    private fun init() {
        manCategoriesItem = generateCategoriesItems(false)
        womanCategoriesItem = generateCategoriesItems(true)

        with(binding.fragmentEditorOverlay) {
            vGenderSwitch.checkedChangeListener = object :
                CustomSwitch.CustomSwitchCheckedChangeListener {
                override fun isMale(isMale: Boolean) {
                    update()
                }

            }
        }

        // To disable recycling on colors
        binding.vColorSelector.recycledViewPool.setMaxRecycledViews(0, 0)
//        vPartSelector.recycledViewPool.setMaxRecycledViews(0, 0)

        binding.vColorSelector.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        colorsAdapter = ColorsAdapter(requireContext()) {
            if (states().isEmpty()) {
                return@ColorsAdapter
            }

            val selectedAvatarStateAsset =
                assetForCategory(categoriesAdapter.selectedCategoryTypeResource)

            val avatarStateAsset = AvatarStateAsset(
                selectedAvatarStateAsset.id,
                it.color.id
            )
            states().add(
                copyLastStateWithAsset(
                    categoriesAdapter.selectedCategoryTypeResource,
                    avatarStateAsset
                )
            )
            undoedStates().clear()
            update()
        }
        binding.vColorSelector.adapter = colorsAdapter


        binding.vCategorySelector.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        categoriesAdapter =
            CategoriesAdapter { partTypeSelectorItem ->
                selectCategory(partTypeSelectorItem)
            }.apply {
                binding.vCategorySelector.adapter = this
            }

        binding.fragmentEditorOverlay.vUndoButton.setOnClickListener {
            if (states().size >= 2) {
                undoedStates().add(states().removeAt(states().size - 1))
                update()
            }
        }

        binding.fragmentEditorOverlay.vRedoButton.setOnClickListener {
            if (undoedStates().isNotEmpty()) {
                states().add(undoedStates().removeAt(undoedStates().size - 1))
                update()
            }
        }

        binding.vNextButton.setOnClickListener {
            moveCategorySelector(forward = true)
        }

        binding.vPrevButton.setOnClickListener {
            moveCategorySelector(forward = false)
        }

        binding.fragmentEditorOverlay.vRandomButton.setOnClickListener {
            randomAvatar()
        }

        binding.fragmentEditorOverlay.topPanel.vCheckButton.setOnClickListener {
            try {
                navigateToResult(states().last().toJson())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.fragmentEditorOverlay.topPanel.vBackButton.setOnClickListener {
            if (isAllChangedSaved()) {
                backToRoot()
                return@setOnClickListener
            }
            if (dialog.isShowing.not()) {
                dialog.show()
            }
        }

        binding.vPartSelector.layoutManager = GridLayoutManager(requireContext(), COLUMNS_COUNT)
        itemsAdapter = ItemsAdapter(requireContext()) {
            if (states().isEmpty()) {
                return@ItemsAdapter
            }

            val avatarStateAsset = AvatarStateAsset(
                it.id,
                assetsConfig.allAssetSets()
                    .find { assetSet -> assetSet.id == it.id }?.variants?.let { variants ->
                        val selected =
                            variants.find { variant -> variant.color.id == colorsAdapter.selectedColorId }?.color?.id
                        return@let selected ?: variants.first().color.id
                    }
            )

            states().add(
                copyLastStateWithAsset(
                    categoriesAdapter.selectedCategoryTypeResource,
                    avatarStateAsset
                )
            )

            undoedStates().clear()
            update()
        }
        binding.vPartSelector.adapter = itemsAdapter

        val avatarQuality = arguments?.getFloat(Constants.QUALITY_AVATAR_DATA_KEY, 0.5f) ?: 0.5f
        binding.vAvatarView.quality = avatarQuality

        val avatarState = arguments?.getString(Constants.STATE_AVATAR_DATA_KEY)?.let {
            try {
                Gson().fromJson(it, AvatarState::class.java)
            } catch (e: Exception) {
                null
            }
        }

        with(binding.fragmentEditorOverlay) {
            if (avatarState != null) {
                val male = avatarState.gender == "male"
                manStates.add(if (male) avatarState else assetsConfig.randomAvatarState(false))
                womanStates.add(if (male.not()) avatarState else assetsConfig.randomAvatarState(true))

                if (male) vGenderSwitch.setMale() else vGenderSwitch.setFemale()
            } else {
                manStates.add(assetsConfig.randomAvatarState(false))
                womanStates.add(assetsConfig.randomAvatarState(true))

                // default
                vGenderSwitch.setMale()
            }
            manInitStates.clear()
            womanInitStates.clear()
            manInitStates.addAll(manStates)
            womanInitStates.addAll(womanStates)
        }

        binding.vAvatarView.startParallaxEffect()
        binding.vAvatarView.setDrawBackgroundGradient(true)

        cacheColorAssets()

        update()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            binding = FragmentEditorMainBinding.bind(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!NMRAvatarsSDK.isSdkReady(requireContext())) {
            return
        }

        val configJson =
            File("${NMRAvatarsSDK.getResourcesDirPath(requireContext())}/${Constants.CONFIG_JSON_PATH}")
                .inputStream()
                .use {
                    return@use it.bufferedReader().readText()
                }

        try {
            assetsConfig = Gson().fromJson(configJson, AssetsConfig::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        init()
    }

    override fun onBackPressed(): Boolean {
        if (isAllChangedSaved().not()) {
            if (dialog.isShowing.not()) {
                dialog.show()
            }
            return true
        }
        return false
    }

    private fun isAllChangedSaved(): Boolean {
        return states().containsAll(initStates()) && initStates().containsAll(states())
    }

    fun onHandleBackPress(){
        if (isAllChangedSaved()) {
            backToRoot()
            return
        }
        if (dialog.isShowing.not()) {
            dialog.show()
        }
    }

    private fun man() = binding.fragmentEditorOverlay.vGenderSwitch.isMan()
}
