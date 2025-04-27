package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.doAsyncViewModel
import com.meera.core.utils.files.FileManager
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.CreateGroupUseCase
import com.numplates.nomera3.domain.interactornew.EditGroupUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.RemoveGroupUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorParams
import com.numplates.nomera3.modules.communities.data.entity.Community
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCaseParams
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.presentation.viewmodel.viewevents.GroupEditViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class GroupEditViewModel : BaseViewModel() {

    @Inject
    lateinit var communityInfoUseCase: GetCommunityInformationUseCase

    @Inject
    lateinit var createGroupUseCase: CreateGroupUseCase

    @Inject
    lateinit var editGroupUseCase: EditGroupUseCase

    @Inject
    lateinit var removeGroupUseCase: RemoveGroupUseCase

    @Inject
    lateinit var fbAnalytic: FireBaseAnalytics

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var analyticsInteractor: AnalyticsInteractor

    @Inject
    lateinit var communityRepository: CommunityRepository

    @Inject
    lateinit var amplitudeEditor: AmplitudeEditor

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    val liveGroupInfo = MutableLiveData<Community>()

    val liveViewEvent = MutableLiveData<GroupEditViewEvent>()

    var isCreatorAppearanceMode = true

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun injectDependencies() {
        App.component.inject(this)
    }

    fun getGroupInfo(groupId: Int?) {
        Timber.d("Get Group INFO: grId: $groupId")
        groupId?.let { id ->
            viewModelScope.launch {
                communityInfoUseCase.execute(
                    params = GetCommunityInformationUseCaseParams(id),
                    success = {
                        Timber.d("RESPONSE Get groups: ${Gson().toJson(it)}")
                        liveGroupInfo.value = it
                    },
                    fail = {
                        Timber.e("ERROR: get group info: ${it.localizedMessage}")
                    }
                )
            }
        }
    }


    fun createGroup(
        name: String,
        description: String,
        privateGroup: Int,
        royalty: Int,
        avatar: String
    ) {
        if (name.length < 3)
            liveViewEvent.value = GroupEditViewEvent.ErrorNameSizeMoreThenTree
        else {
            disposables.add(
                createGroupUseCase.createGroup(name, description, privateGroup, royalty, avatar)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d("Response Create group: $response")
                        val groupId = response.data?.groupId
                        when {
                            isExist(response) -> {
                                liveViewEvent.value = GroupEditViewEvent.FailureGroupCreateExist
                            }

                            groupId != null -> {
                                createCommunityEvent(groupId)
                                liveViewEvent.value = GroupEditViewEvent.SuccessGroupCreate(groupId.toInt())
                            }

                            else -> {
                                liveViewEvent.value = GroupEditViewEvent.FailureGroupCreate
                            }
                        }
                    }, { error ->
                        Timber.e("ERROR: Create group $error")
                        val errorEvent = getErrorEvent(error)
                        liveViewEvent.value = errorEvent
                    })
            )
        }

    }

    private fun createCommunityEvent(groupId: Long) {
        viewModelScope.launch {
            communityRepository.onCreateCommunity(groupId)
        }
    }

    fun editGroup(
        groupId: Int,
        name: String,
        description: String,
        privateGroup: Int,
        royalty: Int,
        avatar: String,
        isDeleteGroupAvatar: Boolean
    ) {

        if (name.length < 3)
            liveViewEvent.value = GroupEditViewEvent.ErrorNameSizeMoreThenTree
        else {
            disposables.add(
                editGroupUseCase.editGroup(
                    groupId = groupId,
                    name = name,
                    description = description,
                    privateGroup = privateGroup,
                    royalty = royalty,
                    image = avatar,
                    isDeleteGroupAvatar = isDeleteGroupAvatar
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d("Edit Group response")
                        when {
                            isExist(response) -> {
                                liveViewEvent.value = GroupEditViewEvent.FailureGroupEditExist
                            }

                            response.data != null -> {
                                liveViewEvent.value = GroupEditViewEvent.SuccessGroupEdit
                            }

                            else -> {
                                liveViewEvent.value = GroupEditViewEvent.FailureGroupEdit
                            }
                        }
                    }, { error ->
                        Timber.e("ERROR: edit group $error")
                        liveViewEvent.value = GroupEditViewEvent.FailureGroupEdit
                    })
            )
        }
    }

    fun removeGroup(groupId: Int) {
        disposables.add(
            removeGroupUseCase.removeGroup(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    Timber.d("Group successfully deleted")
                    if (response.data != null) {
                        liveViewEvent.value = GroupEditViewEvent.SuccessGroupDeleted
                    } else {
                        liveViewEvent.value = GroupEditViewEvent.FailureGroupDeleted
                    }
                }, { error ->
                    Timber.e("ERROR: Remove group $error")
                    liveViewEvent.value = GroupEditViewEvent.FailureGroupDeleted
                })
        )
    }

    fun logScreenForFragment(groupId: Int?) {
        fbAnalytic.logScreenForFragment(
            groupId?.let {
                "EditGroup"
            } ?: "CreateGroup"
        )
    }

    fun logPhotoEdits(nmrAmplitude: NMRPhotoAmplitude) =
        viewModelScope.launch {
            amplitudeEditor.photoEditorAction(
                editorParams = AmplitudeEditorParams(
                    where = AmplitudePropertyWhere.COMMUNITY,
                    automaticOpen = true
                ),
                nmrAmplitude = nmrAmplitude
            )
        }

    private fun <T> isExist(response: ResponseWrapper<T>): Boolean {
        val code400 = response.code == ResponseWrapper.CODE_400
        val messageExist = response.message == ERROR_COMMUNITY_ALREADY_EXIST
        return code400 && messageExist

    }

    fun deleteTempImageFile(filePath: String?) {
        filePath?.let {
            doAsyncViewModel({
                try {
                    val extension = filePath.substring(filePath.lastIndexOf("."))
                    Timber.d("Temp image file extension: $extension")
                    if (extension != ".gif")
                        return@doAsyncViewModel fileManager.deleteFile(it)
                    else
                        return@doAsyncViewModel false
                } catch (e: Exception) {
                    Timber.e(e)
                    return@doAsyncViewModel false
                }
            }, { isDeleted ->
                Timber.d("Temp image file isDeleted: $isDeleted")
            })
        }
    }

    private fun getErrorEvent(error: Throwable?): GroupEditViewEvent {
        return if (error is UnknownHostException) {
            GroupEditViewEvent.NoInternetConnection
        } else {
            GroupEditViewEvent.FailureGroupCreate
        }
    }

    companion object {
        private const val ERROR_COMMUNITY_ALREADY_EXIST = "already exists"
    }
}
