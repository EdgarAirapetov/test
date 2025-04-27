package com.numplates.nomera3.data.network;

import io.reactivex.Flowable;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * created by abelov
 */
public interface ApiCars {

//    this.api = new ApiService().createCarsApiService("https://www.carqueryapi.com/");
    //https://www.carqueryapi.com/api/0.3/?callback=?&cmd=getMakes&year=%222018%22

    @POST("/api/0.3/?callback=?&cmd=getYears")
    Flowable<CarsYears> getYears();


    @POST("/api/0.3/?callback=?&cmd=getMakes")
    Flowable<CarsMakes> getMakes(
            @Query("year") String year);

    @POST("/api/0.3/?callback=?&cmd=getModels")
    Flowable<CarsModels> getModels(
            @Query("year") String year,
            @Query("make") String make);




}
