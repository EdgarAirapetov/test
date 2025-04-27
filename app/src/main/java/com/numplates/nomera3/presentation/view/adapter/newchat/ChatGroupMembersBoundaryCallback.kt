package com.numplates.nomera3.presentation.view.adapter.newchat

import androidx.paging.PagedList
import com.google.gson.Gson
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.data.newmessenger.response.GroupChatMembers
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.meera.db.DataStore
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.presentation.view.adapter.newpostlist.PagingRequestHelper
import com.meera.core.extensions.fromJson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.phoenixframework.Message
import timber.log.Timber
import java.util.concurrent.Executors

class ChatGroupMembersBoundaryCallback(private val roomId: Long?,
                                       private val webSocketMainChannel: WebSocketMainChannel,
                                       private val dataStore: DataStore,
                                       private val gson: Gson) : PagedList.BoundaryCallback<ChatMember>() {


    private val disposables = CompositeDisposable()

    private val helper = PagingRequestHelper(Executors.newSingleThreadExecutor())

    private var startPosition: Int = 0


    override fun onZeroItemsLoaded() {
        // Timber.d("On ZERO items loaded() useCase")

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            getChatMembersFromNetwork(roomId, startPosition, 20,
                    {
                        helperCallback.recordSuccess()
                    },
                    { error ->
                        helperCallback.recordFailure(error)
                    })
        }

    }

    override fun onItemAtEndLoaded(itemAtEnd: ChatMember) {
        // Timber.d("On item END loaded() ${itemAtEnd.userId}")

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
            getChatMembersFromNetwork(roomId, startPosition, 20,
                    {
                        helperCallback.recordSuccess()
                    },
                    { error ->
                        helperCallback.recordFailure(error)
                    })

        }
    }


    private fun getChatMembersFromNetwork(roomId: Long?,
                                          offset: Int,
                                          limit: Int,
                                          actionSuccess: () -> Any,
                                          actionFailure: (throwable: Throwable) -> Any) {

        roomId?.let { id ->
            val payload = hashMapOf(
                    "room_id" to id,
                    "offset" to offset,
                    "limit" to limit,
                    "user_type" to "UserChat"
            )
            disposables.add(
                    webSocketMainChannel.pushGetMembers(payload)
                            .map { parseResponse(it) }
                            .subscribeOn(Schedulers.io())
                            .subscribe({ response ->
                                Timber.d("Success save to Db BoundaryCallback")
                                actionSuccess.invoke()
                            }, {
                                Timber.e(it)
                                actionFailure.invoke(it)
                            })
            )
        }
    }

    /**
     * Parse response and save to Db
     */
    private fun parseResponse(response: Message) : MutableList<ChatMember> {
        val members = mutableListOf<ChatMember>()
        val responseObj = gson.fromJson<ResponseWrapperWebSock<GroupChatMembers>>(
                gson.toJson(response.payload))

        // Start position paginate
        responseObj.response?.members?.size?.let {
            startPosition = it
        }

        responseObj.response?.members?.forEach { member ->
            member.userId = member.user.userId
            member.roomId = roomId

            // Timber.d("Member: $member")
            // Add user to Db
            dataStore.daoChatMembers().insert(member)
        }
        return members
    }


}
