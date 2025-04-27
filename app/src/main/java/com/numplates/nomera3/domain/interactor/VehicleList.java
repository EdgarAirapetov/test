package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.Vehicles;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import io.reactivex.Flowable;

/**
 * Created by abelov.
 */
public class VehicleList extends UseCase<ResponseWrapper<Vehicles>> {

    private final ApiHiWay api;
    private long userId;

    public VehicleList(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    public void setParams(long userId) {
        this.userId = userId;
    }

    @Override
    protected Flowable<ResponseWrapper<Vehicles>> buildUseCaseObservable() {
        return api.vehicleList(userId);
    }

}
