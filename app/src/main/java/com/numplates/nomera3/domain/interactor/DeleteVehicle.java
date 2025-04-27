package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.EmptyModel;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import io.reactivex.Flowable;

/**
 * Created by abelov.
 */
public class DeleteVehicle extends UseCase<ResponseWrapper<EmptyModel>> {

    private final ApiHiWay api;
    private String vehicleId;

    public DeleteVehicle(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    public void setParams(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    protected Flowable<ResponseWrapper<EmptyModel>> buildUseCaseObservable() {
        return api.deleteVehicle(vehicleId);
    }

}
