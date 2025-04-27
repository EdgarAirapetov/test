package com.numplates.nomera3.presentation.view.view;

import com.numplates.nomera3.data.network.CarsMakes;
import com.numplates.nomera3.data.network.CarsModels;
import com.numplates.nomera3.data.network.CarsYears;
import com.numplates.nomera3.data.network.Country;
import com.numplates.nomera3.data.network.Vehicle;
import com.numplates.nomera3.data.network.VehicleTypes;
import com.numplates.nomera3.data.network.Vehicles;
import com.numplates.nomera3.presentation.router.INetView;

import java.util.List;

/**
 * created by abelov
 */
public interface IGarageView extends INetView {

    void onCountryList(List<Country> res);

    void onYears(CarsYears years);
    void onMakes(List<CarsMakes.Make> makes);
    void onModels(List<CarsModels.Model> models);

    void onAddVehicle();
    void onUpdateVehicle();
    void onVehicleList(Vehicles vehicles);
    void onVehicle(Vehicle vehicle);
    void onDeleteVehicle();
    void onMainVehicle();
    void onTypes(VehicleTypes types);
    void onFail(String msg);
    void onShowErrorMessage(String message);

}
