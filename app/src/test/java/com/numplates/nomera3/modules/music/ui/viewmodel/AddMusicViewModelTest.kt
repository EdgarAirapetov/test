package com.numplates.nomera3.modules.music.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.numplates.nomera3.TestCoroutineRule
import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity
import com.numplates.nomera3.modules.music.domain.usecase.GetTopMusicUseCase
import com.numplates.nomera3.modules.music.domain.usecase.SearchMusicUseCase
import com.numplates.nomera3.modules.music.ui.adapter.MusicAdapterType
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.modules.music.ui.entity.state.MusicSearchScreenState
import com.numplates.nomera3.modules.music.ui.entity.state.Status
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class AddMusicViewModelTest {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Rule
    @JvmField
    var testCoroutineRule = TestCoroutineRule()

    private val viewStateObserver: Observer<MusicSearchScreenState> = mock()
    private val searchMusicUseCaseTest: SearchMusicUseCase = mock()
    private val topMusicUSeCase: GetTopMusicUseCase = mock()

    private val testQuery = "test query"

    private lateinit var viewModel: AddMusicViewModel

    @Before
    fun setupViewModel() {
        viewModel = AddMusicViewModel(searchMusicUseCaseTest, topMusicUSeCase)
        viewModel.liveState.observeForever(viewStateObserver)
    }

    // TODO https://nomera.atlassian.net/browse/BR-28535
//    @ExperimentalCoroutinesApi
//    @Test
//    fun testNormalQuery() = runTest {
//        //given
//        val listEntity = getListEntity()
//        val searchResultState = getSearchStateResult(listEntity)
//
//        whenever(
//            searchMusicUseCaseTest.invoke(
//                20,
//                0,
//                testQuery
//            )
//        ).thenReturn(getMusicResponse())
//
//        //when
//        viewModel.searchMusic(testQuery)
//        runCurrent()
//
//        //then
//        verify(viewStateObserver, times(2)).onChanged(MusicSearchScreenState.MusicSearchLoading)
//        verify(viewStateObserver).onChanged(searchResultState)
//    }

    // TODO https://nomera.atlassian.net/browse/BR-28535
//    @ExperimentalCoroutinesApi
//    @Test
//    fun testQueryWithException() = runTest {
//        //given
//        whenever(
//            searchMusicUseCaseTest.invoke(
//                20,
//                0,
//                testQuery
//            )
//        ).thenThrow(RuntimeException())
//
//        //when
//        viewModel.searchMusic(testQuery)
//        runCurrent()
//
//        //then
//        verify(viewStateObserver, times(2)).onChanged(MusicSearchScreenState.MusicSearchLoading)
//        verify(viewStateObserver).onChanged(getSearchResultWithException())
//    }

    private fun getSearchResultWithException(): MusicSearchScreenState.SearchResultState {
        return MusicSearchScreenState.SearchResultState(
            searchList = listOf(MusicCellUIEntity(type = MusicAdapterType.ITEM_TYPE_HEADER_SEARCH)),
            status = Status.STATUS_EMPTY_LIST,
            needToScrollUp = false
        )
    }

    private fun getSearchStateResult(listEntity: List<MusicCellUIEntity>): MusicSearchScreenState.SearchResultState {
        val result = listEntity.toMutableList().apply {
            add(0, MusicCellUIEntity(type = MusicAdapterType.ITEM_TYPE_HEADER_SEARCH))
            add(MusicCellUIEntity(type = MusicAdapterType.ITEM_TYPE_PROGRESS))
        }
        return MusicSearchScreenState.SearchResultState(
            searchList = result,
            status = Status.STATUS_OK,
            needToScrollUp = true
        )
    }

    private fun getListEntity() = listOf(
        MusicCellUIEntity(
            MediaEntity(
                album = testQuery,
                artist = testQuery
            ),
            needToShowSeparator = false
        )
    )

    private fun getMusicResponse(): List<MusicSearchEntity> =
        mutableListOf<MusicSearchEntity>().apply {
            add(
                MusicSearchEntity(
                    testQuery,
                    "",
                    testQuery,
                    "",
                    "",
                    "",
                    "",
                    ""
                )
            )
        }
}
