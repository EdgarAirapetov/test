package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.CarsModels;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import io.reactivex.Flowable;

/**
 * Created by abelov.
 */
public class CarModels extends UseCase<ResponseWrapper<CarsModels>> {

    private final ApiHiWay api;
    private String query;
    private String make;

    public CarModels(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    public void setParams(String make, String query) {
        this.query = query;
        this.make = make;
    }

    @Override
    protected Flowable<ResponseWrapper<CarsModels>> buildUseCaseObservable() {
        return api.carModelsList(make, query);
    }

}
