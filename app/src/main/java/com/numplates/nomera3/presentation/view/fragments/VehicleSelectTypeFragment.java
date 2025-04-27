package com.numplates.nomera3.presentation.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.meera.core.extensions.ContextKt;
import com.numplates.nomera3.Act;
import com.numplates.nomera3.R;
import com.numplates.nomera3.data.network.CarsMakes;
import com.numplates.nomera3.data.network.CarsModels;
import com.numplates.nomera3.data.network.CarsYears;
import com.numplates.nomera3.data.network.Country;
import com.numplates.nomera3.data.network.Vehicle;
import com.numplates.nomera3.data.network.VehicleType;
import com.numplates.nomera3.data.network.VehicleTypes;
import com.numplates.nomera3.data.network.Vehicles;
import com.numplates.nomera3.databinding.FragmentVehivleSelectTypeBinding;
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel;
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType;
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFragment;
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFragmentKt;
import com.numplates.nomera3.presentation.presenter.GaragePresenter;
import com.numplates.nomera3.presentation.router.Arg;
import com.numplates.nomera3.presentation.router.BaseFragmentNew;
import com.numplates.nomera3.presentation.router.IArgContainer;
import com.numplates.nomera3.presentation.view.adapter.ProfileListAdapter;
import com.numplates.nomera3.presentation.view.adapter.VehicleTypeAdapter;
import com.numplates.nomera3.presentation.view.callback.VehickeTypeCallback;
import com.numplates.nomera3.presentation.view.view.IGarageView;
import com.numplates.nomera3.presentation.view.view.ProfileListItem;
import com.numplates.nomera3.presentation.view.widgets.BannerLayoutManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.functions.Function3;
import timber.log.Timber;

public class VehicleSelectTypeFragment extends BaseFragmentNew<FragmentVehivleSelectTypeBinding> implements IGarageView {

    GaragePresenter presenter;

    private Dialog countryDialog;
    private Vehicle vehicle;

    public void onPublish() {
        VehicleType vehicleType = vehicle.getType();
        int hasNumber = vehicle.getType().getHasNumber();
        Integer vehicleCountryId = vehicle.getCountry().getCountryId();

        if (vehicleCountryId != null || (vehicleType != null && hasNumber == 0)) {
            add(new VehicleEditFragment(), Act.LIGHT_STATUSBAR, new Arg(IArgContainer.ARG_CAR_MODEL, vehicle));
        }
    }

    VehicleTypeAdapter adapter;
    ProfileListAdapter countryAdapter;

    @NotNull
    @Override
    public Function3<LayoutInflater, ViewGroup, Boolean, FragmentVehivleSelectTypeBinding> getBindingInflater() {
        return FragmentVehivleSelectTypeBinding::inflate;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vehicle = new Vehicle(0,
                new VehicleType(),
                null,
                null,
                0,
                new Country(),
                null,
                null,
                new CarsMakes.Make(),
                new CarsModels.Model(),
                "",
                null
        );
        getParentFragmentManager().setFragmentResultListener(RegistrationCountryFragmentKt.KEY_COUNTRY, this, (requestKey, bundle) -> {
            if (bundle.containsKey(RegistrationCountryFragmentKt.KEY_COUNTRY)) {
                RegistrationCountryModel countryUiModel = bundle.getParcelable(RegistrationCountryFragmentKt.KEY_COUNTRY);

                FragmentVehivleSelectTypeBinding binding = getBinding();
                if (binding != null) {
                    binding.tvSend.setEnabled(true);
                    Country country = new Country();
                    country.setName(countryUiModel.getName());
                    country.setFlag(countryUiModel.getFlag());
                    country.setCountryId(countryUiModel.getId());
                    vehicle.setCountry(country);
                    Glide.with(binding.ivPicture)
                        .load(country.getFlag())
                        .apply(RequestOptions
                            .circleCropTransform()
                            .placeholder(R.drawable.gray_circle_transparent_shape)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .into(binding.ivPicture);
                    binding.ivPicture.setVisibility(View.VISIBLE);
                    binding.etCountry.setText(country.getName());
                }
            }
        });
    }

    @Override
    public void onReturnTransitionFragment() {
        ContextKt.hideKeyboard(requireContext(), requireView());

        FragmentVehivleSelectTypeBinding binding = getBinding();
        if (binding != null) {
            binding.rvVehicleType.requestLayout(); // hide if from common edit text keyboard
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentVehivleSelectTypeBinding binding = getBinding();
        if (binding != null) {
            View statusBarVehicleSelect = binding.statusBatVehicleSelect;
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) statusBarVehicleSelect.getLayoutParams();
            params.height = ContextKt.getStatusBarHeight(getContext());
            statusBarVehicleSelect.setLayoutParams(params);

            binding.toolbar.setNavigationIcon(R.drawable.arrowback);
            binding.toolbar.setNavigationOnClickListener(v -> getAct().onBackPressed());

            presenter = new GaragePresenter();
            presenter.setView(this);
            binding.tvSend.setEnabled(false);
            presenter.vehicleTypeList();

            LinearLayoutManager layoutManager = new BannerLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
            binding.rvVehicleType.setLayoutManager(layoutManager);
            binding.rvVehicleType.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    binding.rvVehicleType.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    adapter = new VehicleTypeAdapter.Builder(getAct())
                            .visibleAmount(3)
                            .callback(new VehickeTypeCallback() {
                                @Override
                                public void onClick(RecyclerView.ViewHolder holder) {
                                    super.onClick(holder);

                                    layoutManager.scrollToPositionWithOffset(holder.getAdapterPosition(), binding.rvVehicleType.getWidth() / 3);
                                    binding.rvVehicleType.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.setSelected(holder.getAdapterPosition());
                                        }
                                    }, 100);
                                }

                                @Override
                                public void onSelected(int position) {
                                    if (position > 0 && position <= 2) {
                                        binding.flCountry.setVisibility(View.VISIBLE);
                                        binding.tvSend.setEnabled(vehicle.getCountry().getCountryId() != null && !vehicle.getCountry().getCountryId().toString().isEmpty());
                                    } else {
                                        binding.flCountry.setVisibility(View.INVISIBLE);
                                        binding.tvSend.setEnabled(true);
                                    }

                                    vehicle.setType(adapter.getItem(position));
                                }
                            })
                            .data(new ArrayList<VehicleType>())
                            .itemWidth(binding.rvVehicleType.getWidth() / 3)
                            .build();

                    binding.rvVehicleType.setAdapter(adapter);

                }
            });
            SnapHelper snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(binding.rvVehicleType);
            binding.rvVehicleType.addOnScrollListener(new RecyclerView.OnScrollListener() {
                int oldCenter;

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        int totalVisibleItems = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();
                        int centeredItemPosition = layoutManager.findFirstVisibleItemPosition() + 1;
                        if (oldCenter != centeredItemPosition) {
                            oldCenter = centeredItemPosition;
                            adapter.setSelected(centeredItemPosition);

                        }
                    }
                }
            });

            binding.etCountry.setOnClickListener(v -> {
                Timber.d("bazaleev etCountry clicked");
                binding.etCountry.setEnabled(false);
                onSelectCountry();
                Handler h = new Handler();
                h.postDelayed(() -> {
                    binding.etCountry.setEnabled(true);
                }, 500);
            });

            binding.tvSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPublish();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public void onCountryList(List<Country> countries) {
        List<ProfileListItem> list = new ArrayList<>();
        list.addAll(countries);
        countryAdapter.setData(list);
        countryDialog.show();
    }

    public void onSelectCountry() {
        RegistrationCountryFragment fragment =
            RegistrationCountryFragment.Companion.newInstance(RegistrationCountryFromScreenType.Transport);
        fragment.show(getParentFragmentManager(), RegistrationCountryFragmentKt.KEY_COUNTRY);
    }

    @Override
    public void onYears(CarsYears years) {

    }

    @Override
    public void onMakes(List<CarsMakes.Make> makes) {

    }

    @Override
    public void onModels(List<CarsModels.Model> models) {

    }

    @Override
    public void onAddVehicle() {

    }

    @Override
    public void onUpdateVehicle() {

    }

    @Override
    public void onVehicleList(Vehicles vehicles) {

    }

    @Override
    public void onVehicle(Vehicle vehicle) {

    }

    @Override
    public void onDeleteVehicle() {

    }

    @Override
    public void onMainVehicle() {

    }

    @Override
    public void onShowErrorMessage(String message) {

    }

    @Override
    public void onTypes(VehicleTypes res) {
        List<VehicleType> types = new ArrayList<>();
        types.add(null);
        types.addAll(res.getList());
        types.add(null);
        adapter.addItems(types);
        adapter.setSelected(1);
    }

    @Override
    public void onFail(String msg) {

    }

}


