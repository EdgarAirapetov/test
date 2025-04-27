package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.CarsMakes;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import io.reactivex.Flowable;

/**
 * Created by abelov.
 */
public class CarMakes extends UseCase<ResponseWrapper<CarsMakes>> {

    private final ApiHiWay api;
    private String query;

    public CarMakes(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    public void setParams(String query) {
        this.query = query;

    }

    @Override
    protected Flowable<ResponseWrapper<CarsMakes>> buildUseCaseObservable() {
        return api.carMakersList(query);
    }

}
