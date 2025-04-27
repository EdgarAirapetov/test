package com.numplates.nomera3.data.network;

import com.numplates.nomera3.data.network.core.ResponseWrapper;
import com.numplates.nomera3.domain.interactor.UseCase;

import java.util.List;

import io.reactivex.Flowable;

public class FriendListHiWay extends UseCase<ResponseWrapper<List<UserModel>>> {
    public static final int FRIENDS = 0;
    public static final int BLACKLIST = 1;
    public static final int OUTCOMING = 2;
    public static final int INCOMING = 3;

    private final ApiHiWay apiHiWay;
    private long userId;
    private int startIndex;
    private int listType;

    public FriendListHiWay(ApiHiWay apiHiWay){
        this.apiHiWay = apiHiWay;
    }

    public void setParams(long userId, int startIndex, int listType) {
        this.userId = userId;
        this.startIndex = startIndex;
        this.listType = listType;
    }

    public int getListType(){
        return listType;
    }

    @Override
    protected Flowable<ResponseWrapper<List<UserModel>>> buildUseCaseObservable() {
        if (apiHiWay == null) return Flowable.empty();

        switch (listType){
            case FRIENDS:
                return apiHiWay.getFriendList(userId, startIndex);
            case INCOMING:
                return apiHiWay.getFriendIncomingList(userId, startIndex);
            case BLACKLIST:
                return apiHiWay.getFriendBlockedList(userId);
            case OUTCOMING:
                return apiHiWay.getFriendOutcomingList(userId, startIndex);
            default: return Flowable.empty();
        }
    }
}
