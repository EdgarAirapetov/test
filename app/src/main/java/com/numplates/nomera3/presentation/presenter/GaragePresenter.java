package com.numplates.nomera3.presentation.presenter;

import com.numplates.nomera3.App;
import com.numplates.nomera3.data.network.ApiHiWay;
import com.numplates.nomera3.data.network.CarsMakes;
import com.numplates.nomera3.data.network.CarsModels;
import com.numplates.nomera3.data.network.CarsYears;
import com.numplates.nomera3.data.network.Countries;
import com.numplates.nomera3.data.network.EmptyModel;
import com.numplates.nomera3.data.network.Vehicle;
import com.numplates.nomera3.data.network.VehicleResponce;
import com.numplates.nomera3.data.network.VehicleResponse;
import com.numplates.nomera3.data.network.VehicleTypes;
import com.numplates.nomera3.data.network.Vehicles;
import com.numplates.nomera3.data.network.core.ResponseWrapper;
import com.numplates.nomera3.domain.interactor.AddVehicle;
import com.numplates.nomera3.domain.interactor.CarMakes;
import com.numplates.nomera3.domain.interactor.CarModels;
import com.numplates.nomera3.domain.interactor.CountryList;
import com.numplates.nomera3.domain.interactor.DeleteVehicle;
import com.numplates.nomera3.domain.interactor.GetVehicle;
import com.numplates.nomera3.domain.interactor.MainVehicle;
import com.numplates.nomera3.domain.interactor.UpdateVehicle;
import com.numplates.nomera3.domain.interactor.VehicleList;
import com.numplates.nomera3.domain.interactor.VehicleTypeList;
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase;
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeRepository;
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyVehicleType;
import com.numplates.nomera3.modules.vehicle.VehicleRepository;
import com.numplates.nomera3.presentation.view.view.IGarageView;


import static com.numplates.nomera3.presentation.router.INetView.UNEXPECTED_SERVER_RESPONSE;

import javax.inject.Inject;

/**
 * Created by abelov
 */
public class GaragePresenter implements Presenter {

    private final CountryList countryList;
    private final CarModels carModels;
    private final CarMakes carMakes;
    private final AddVehicle addVehicle;
    private final UpdateVehicle updateVehicle;
    private final GetVehicle getVehicle;
    private final DeleteVehicle deleteVehicle;
    private final MainVehicle mainVehicle;
    private final VehicleList vehicleList;
    private final VehicleTypeList vehicleTypeList;
    private IGarageView view;

    @Inject
    public AmplitudeRepository tracker;

    @Inject
    public VehicleRepository vehicleRepository;

    @Inject
    public GetUserUidUseCase getUserUidUseCase;

    @Inject
    public ApiHiWay apiHiWay;


    public GaragePresenter() {
        App.component.inject(this);
        countryList = new CountryList(apiHiWay);
        carModels = new CarModels(apiHiWay);
        carMakes = new CarMakes(apiHiWay);
        addVehicle = new AddVehicle(apiHiWay);
        updateVehicle = new UpdateVehicle(apiHiWay);
        getVehicle = new GetVehicle(apiHiWay);
        deleteVehicle = new DeleteVehicle(apiHiWay);
        vehicleList = new VehicleList(apiHiWay);
        mainVehicle = new MainVehicle(apiHiWay);
        vehicleTypeList = new VehicleTypeList(apiHiWay);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        countryList.unsubscribe();
        carMakes.unsubscribe();
        carModels.unsubscribe();
        addVehicle.unsubscribe();
        updateVehicle.unsubscribe();
        getVehicle.unsubscribe();
        deleteVehicle.unsubscribe();
        vehicleList.unsubscribe();
        mainVehicle.unsubscribe();
        vehicleTypeList.unsubscribe();
    }

    public void setView(IGarageView view){
        this.view = view;
    }

    public Long getUserUid(){
        return getUserUidUseCase.invoke();
    }

    private void fail(Throwable err) {
        if(view!=null) {
            view.onFail(err.toString());
        }
    }

    private void onCountryList(ResponseWrapper<Countries> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res != null) {
            view.onCountryList(res.getData().getCountries());
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }

    public void countryList( ){
        countryList.execute(this::onCountryList, this::fail);
    }

    private void onYears(CarsYears res) {
        if (res != null) {
            view.onYears(res);
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }

    public void carMakes (String query){
        carMakes.unsubscribe();
        carMakes.setParams(query);
        carMakes.execute(this::onMakes, this::fail);
    }

    private void onMakes(ResponseWrapper<CarsMakes> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res != null && res.getErr() == null ) {
            view.onMakes(res.getData().getMakes());
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }

    public void carModels (String make, String query){
        carMakes.unsubscribe();
        carModels.setParams(make, query);
        carModels.execute(this::onModels, this::fail);
    }

    private void onModels(ResponseWrapper<CarsModels> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res != null) {
            view.onModels(res.getData().getModels());
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }


    public void addVehicle(Vehicle vehicle, String image) {
        addVehicle.setParams(vehicle, image);
        addVehicle.execute(this::onAddVehicle, this::fail);
    }

    public static AmplitudePropertyVehicleType getAmplitudeVehicleNameByType(String typeId) {
        switch (typeId) {
            case "1": return AmplitudePropertyVehicleType.CAR;
            case "2": return AmplitudePropertyVehicleType.MOTOBIKE;
            case "3": return AmplitudePropertyVehicleType.BICYCLE;
            case "4": return AmplitudePropertyVehicleType.SCOOTER;
            case "5": return AmplitudePropertyVehicleType.SKATE;
            case "6": return AmplitudePropertyVehicleType.ROLLERS;
            case "7": return AmplitudePropertyVehicleType.SNOWMOBILE;
            case "8": return AmplitudePropertyVehicleType.JET_SKI;
            case "9": return AmplitudePropertyVehicleType.AIRPLANE;
            case "10": return AmplitudePropertyVehicleType.BOAT;
        }
        return AmplitudePropertyVehicleType.NONE;
    }

    private void onAddVehicle(ResponseWrapper<VehicleResponse> res) {
        if(res.getErr() != null) {
            view.onShowErrorMessage(res.getErr().getMessage());
        } else if (res != null) {
            try {
                tracker.logTransportAdd(
                        getAmplitudeVehicleNameByType(res.getData().getVehicle().getType().getTypeId())
                );
            } catch (Exception e) {
                tracker.logTransportAdd(AmplitudePropertyVehicleType.NONE);
            }
            view.onAddVehicle();
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }

    public void updateVehicle(Vehicle vehicle, String image) {
        updateVehicle.setParams(vehicle, image);
        updateVehicle.execute(res -> {
            if(res.getErr() != null) {
                view.onFail(res.getErr().getUserMessage());
            } else if (res != null) {
                onUpdateVehicleSuccess(vehicle.getVehicleId().toString());
            } else {
                view.onFail(UNEXPECTED_SERVER_RESPONSE);
            }
        }, this::fail);
    }


    private void onUpdateVehicleSuccess(String vehicleId) {
        getVehicle.setParams(vehicleId);
        getVehicle.execute(res -> {
            if (res.getData() != null) {
                vehicleRepository.getVehicleUpdateSubject().onNext(res.getData().getVehicle());
            }
        }, this::fail);
        view.onUpdateVehicle();
    }

    public void getVehicleList(long userId) {

        vehicleList.setParams(userId);
        vehicleList.execute(this::onVehicleList, this::fail);
    }

    private void onVehicleList(ResponseWrapper<Vehicles> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res.getData() != null) {
            view.onVehicleList(res.getData());
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }

    public void getVehicle(String vehicleId) {

        getVehicle.setParams(vehicleId);
        getVehicle.execute(this::onVehicle, this::fail);
    }

    private void onVehicle(ResponseWrapper<VehicleResponce> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res != null) {
            view.onVehicle(res.getData().getVehicle());
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }

    public void deleteVehicle(String vehicleId) {

        deleteVehicle.setParams(vehicleId);
        deleteVehicle.execute(this::onDeleteVehicle, this::fail);
    }

    private void onDeleteVehicle(ResponseWrapper<EmptyModel> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res != null) {
            view.onDeleteVehicle();
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }

    public void mainVehicle(String vehicleId, int main) {

        mainVehicle.setParams(vehicleId, main);
        mainVehicle.execute(this::onMainVehicle, this::fail);
    }

    private void onMainVehicle(ResponseWrapper<EmptyModel> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res != null) {
            view.onMainVehicle();
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);

        }
    }

    public void vehicleTypeList() {
        vehicleTypeList.execute(this::onVehicleTypes, this::fail);
    }

    private void onVehicleTypes(ResponseWrapper<VehicleTypes> res) {
        if(res.getErr() != null) {
            view.onFail(res.getErr().getUserMessage());
        } else if (res != null) {
            view.onTypes(res.getData());
        } else {
            view.onFail(UNEXPECTED_SERVER_RESPONSE);
        }
    }


    public void deleteVehicleImage(String pathImage) {

    }


}
