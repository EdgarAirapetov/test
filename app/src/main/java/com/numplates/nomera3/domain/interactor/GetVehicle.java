package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.VehicleResponce;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import io.reactivex.Flowable;

/**
 * Created by abelov.
 */
public class GetVehicle extends UseCase<ResponseWrapper<VehicleResponce>> {

    private final ApiHiWay api;
    private String vehicleId;

    public GetVehicle(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    public void setParams(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    protected Flowable<ResponseWrapper<VehicleResponce>> buildUseCaseObservable() {
        return api.getVehicle(vehicleId);
    }

}
