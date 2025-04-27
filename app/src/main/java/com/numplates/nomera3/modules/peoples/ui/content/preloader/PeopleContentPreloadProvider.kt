package com.numplates.nomera3.modules.peoples.ui.content.preloader

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.meera.core.extensions.getScreenWidth
import com.numplates.nomera3.modules.peoples.ui.content.adapter.BloggerMediaContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraBloggerMediaContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraPeoplesContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraRecommendedPeopleAdapter
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.adapter.RecommendedPeopleAdapter
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.holder.BloggerMediaContentListHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraBloggerMediaContentListHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraRecommendedPeopleListHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.RecommendedPeopleListHolder
import com.numplates.nomera3.modules.services.ui.adapter.MeeraServicesRecommendedPeopleAdapter
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesRecommendedUsersListViewHolder

private const val MAX_PRELOAD = 3
private const val MAX_PEOPLE_PRELOAD = 3

internal fun BloggerMediaContentListHolder.getBloggerImagePreload(
    adapter: BloggerMediaContentAdapter
): RecyclerViewPreloader<BloggerMediaContentUiEntity> {
    val preloadSizeProvider = BloggerPreloadSizeProvider(getScreenWidth())
    val modelProvider = BloggerPreloadModelProvider(
        provider = object : BloggerItemsImagesProvider {
            override fun provide(position: Int): List<BloggerMediaContentUiEntity> {
                val item = adapter.getItemByPosition(position) ?: return emptyList()
                return listOf(item)
            }
        },
        context = getContext()
    )
    return RecyclerViewPreloader(
        Glide.with(getContext()),
        modelProvider,
        preloadSizeProvider,
        MAX_PRELOAD
    )
}

internal fun MeeraBloggerMediaContentListHolder.getBloggerImagePreload(
    adapter: MeeraBloggerMediaContentAdapter
): RecyclerViewPreloader<BloggerMediaContentUiEntity> {
    val preloadSizeProvider = BloggerPreloadSizeProvider(getScreenWidth())
    val modelProvider = BloggerPreloadModelProvider(
        provider = object : BloggerItemsImagesProvider {
            override fun provide(position: Int): List<BloggerMediaContentUiEntity> {
                val item = adapter.getItemByPosition(position) ?: return emptyList()
                return listOf(item)
            }
        },
        context = getContext()
    )
    return RecyclerViewPreloader(
        Glide.with(getContext()),
        modelProvider,
        preloadSizeProvider,
        MAX_PRELOAD
    )
}

internal fun RecommendedPeopleListHolder.getRelatedUsersPreload(
    adapter: RecommendedPeopleAdapter
): RecyclerViewPreloader<RecommendedPeopleUiEntity> {
    val preloadSizeProvider = RelatedPreloadSizeProvider(getScreenWidth())
    val modelProvider = RelatedPreloadModelProvider(
        provider = object : RelatedPreloadImagesProvider {
            override fun provide(position: Int): List<RecommendedPeopleUiEntity> {
                val item = adapter.getItemByPosition(position) ?: return emptyList()
                return listOf(item)
            }
        },
        context = getContext()
    )
    return RecyclerViewPreloader(
        Glide.with(getContext()),
        modelProvider,
        preloadSizeProvider,
        MAX_PRELOAD
    )
}

internal fun MeeraRecommendedPeopleListHolder.getRelatedUsersPreload(
    adapter: MeeraRecommendedPeopleAdapter
): RecyclerViewPreloader<RecommendedPeopleUiEntity> {
    val preloadSizeProvider = RelatedPreloadSizeProvider(getScreenWidth())
    val modelProvider = RelatedPreloadModelProvider(
        provider = object : RelatedPreloadImagesProvider {
            override fun provide(position: Int): List<RecommendedPeopleUiEntity> {
                val item = adapter.getItemByPosition(position) ?: return emptyList()
                return listOf(item)
            }
        },
        context = getContext()
    )
    return RecyclerViewPreloader(
        Glide.with(getContext()),
        modelProvider,
        preloadSizeProvider,
        MAX_PRELOAD
    )
}

internal fun MeeraServicesRecommendedUsersListViewHolder.getRelatedUsersPreload(
    adapter: MeeraServicesRecommendedPeopleAdapter
): RecyclerViewPreloader<RecommendedPeopleUiEntity> {
    val preloadSizeProvider = RelatedPreloadSizeProvider(getScreenWidth())
    val modelProvider = RelatedPreloadModelProvider(
        provider = object : RelatedPreloadImagesProvider {
            override fun provide(position: Int): List<RecommendedPeopleUiEntity> {
                val item = adapter.getItemByPosition(position) ?: return emptyList()
                return listOf(item)
            }
        },
        context = getContext()
    )
    return RecyclerViewPreloader(
        Glide.with(getContext()),
        modelProvider,
        preloadSizeProvider,
        MAX_PRELOAD
    )
}

internal fun Fragment.getPeopleMainContentPreload(
    adapter: PeoplesContentAdapter
) : RecyclerViewPreloader<PeoplesContentUiEntity> {
    val preloadSizeProvider = PeopleContentSizeProvider(getScreenWidth())
    val modelProvider = PeopleContentModelProvider(
        provider = object : PeopleContentImagesProvider {
            override fun provide(position: Int): List<PeoplesContentUiEntity> {
                val item = adapter.getItemByPosition(position) ?: return emptyList()
                return listOf(item)
            }
        },
        context = requireContext()
    )
    return RecyclerViewPreloader(
        Glide.with(requireContext()),
        modelProvider,
        preloadSizeProvider,
        MAX_PEOPLE_PRELOAD
    )
}

internal fun Fragment.getPeopleMainContentPreload(
    adapter: MeeraPeoplesContentAdapter
) : RecyclerViewPreloader<PeoplesContentUiEntity> {
    val preloadSizeProvider = PeopleContentSizeProvider(getScreenWidth())
    val modelProvider = PeopleContentModelProvider(
        provider = object : PeopleContentImagesProvider {
            override fun provide(position: Int): List<PeoplesContentUiEntity> {
                val item = adapter.getItemByPosition(position) ?: return emptyList()
                return listOf(item)
            }
        },
        context = requireContext()
    )
    return RecyclerViewPreloader(
        Glide.with(requireContext()),
        modelProvider,
        preloadSizeProvider,
        MAX_PEOPLE_PRELOAD
    )
}
