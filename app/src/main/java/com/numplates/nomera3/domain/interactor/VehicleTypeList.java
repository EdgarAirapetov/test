package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.VehicleTypes;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import io.reactivex.Flowable;

/**
 * Created by abelov.
 */
public class VehicleTypeList extends UseCase<ResponseWrapper<VehicleTypes>> {

    private final ApiHiWay api;

    public VehicleTypeList(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    @Override
    protected Flowable<ResponseWrapper<VehicleTypes>> buildUseCaseObservable() {
        return api.getVehicleTypes();
    }
}
