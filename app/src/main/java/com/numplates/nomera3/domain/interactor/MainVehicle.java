package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.EmptyModel;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import io.reactivex.Flowable;

/**
 * Created by abelov.
 */
public class MainVehicle extends UseCase<ResponseWrapper<EmptyModel>> {

    private final ApiHiWay api;
    private String vehicleId;
    private int main;

    public MainVehicle(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    public void setParams(String vehicleId, int main) {
        this.vehicleId = vehicleId;
        this.main = main;
    }

    @Override
    protected Flowable<ResponseWrapper<EmptyModel>> buildUseCaseObservable() {
        return api.setMainVehicle(vehicleId, main);
    }

}
