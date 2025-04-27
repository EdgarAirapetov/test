package com.numplates.nomera3.domain.interactor;

import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.Vehicle;
import com.numplates.nomera3.data.network.VehicleRequest;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import java.io.File;

import io.reactivex.Flowable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by abelov.
 */
public class UpdateVehicle extends UseCase<ResponseWrapper<Vehicle>> {

    private final ApiHiWay api;
    private Vehicle vehicle;
    private String image;

    public UpdateVehicle(ApiHiWay aboutRepository) {
        this.api = aboutRepository;
    }

    public void setParams(Vehicle vehicle, String image) {
        this.vehicle = vehicle;
        this.image = image;
    }

    @Override
    protected Flowable<ResponseWrapper<Vehicle>> buildUseCaseObservable() {
        if (vehicle.getVehicleId() == null) return Flowable.empty();

        MultipartBody.Part filePart;

        VehicleRequest vehicleRequest = new VehicleRequest(vehicle);

        if(image != null && !image.isEmpty()) {
            File file = new File(image);
            filePart = MultipartBody.Part.createFormData(
                    "image",
                    file.getName(),
                    RequestBody.create(MediaType.parse("image/*"),
                            file));
            return api.updateVehicle(vehicle.getVehicleId(), vehicleRequest.toRequestMap(), filePart);
        }
        return api.updateVehicle(vehicle.getVehicleId(), vehicleRequest);
    }

}
