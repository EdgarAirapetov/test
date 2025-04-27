package com.numplates.nomera3.presentation.view.utils.tedbottompicker;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.meera.core.extensions.CommonKt.dpToPx;
import static com.meera.core.utils.files.FileUtilsImpl.MEDIA_TYPE_VIDEO;
import static com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter.VIEW_TYPE_IMAGE;
import static com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING;
import static com.numplates.nomera3.modules.tags.data.SuggestionsMenuType.ROAD;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.gun0912.tedonactivityresult.TedOnActivityResult;
import com.meera.core.extensions.CommonKt;
import com.meera.core.utils.KeyboardHeightProvider;
import com.meera.core.utils.camera.CameraLensFacing;
import com.meera.core.utils.files.FileManager;
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils;
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel;
import com.meera.core.utils.listeners.OrientationScreenListener;
import com.meera.core.utils.tedbottompicker.GridSpacingItemDecoration;
import com.meera.core.views.EditTextExtended;
import com.meera.media_controller_api.model.MediaControllerCallback;
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse;
import com.meera.media_controller_common.MediaControllerOpenPlace;
import com.meera.media_controller_common.MediaEditorResult;
import com.noomeera.nmrmediatools.NMRPhotoAmplitude;
import com.noomeera.nmrmediatools.NMRVideoAmplitude;
import com.numplates.nomera3.Act;
import com.numplates.nomera3.App;
import com.numplates.nomera3.R;
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType;
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu;
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity;
import com.numplates.nomera3.presentation.model.enums.MediaViewerCameraTypeEnum;
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData;
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer;
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnImageChangeListener;
import com.numplates.nomera3.presentation.view.utils.camera.CameraOrientation;
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider;
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.adapter.GalleryAdapter;
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile;
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.util.RealPathUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import timber.log.Timber;

/**
 * TODO https://nomera.atlassian.net/browse/BR-29727
 */

public class TedBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String EXTRA_CAMERA_IMAGE_URI = "camera_image_uri";
    private static final String EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri";

    public BaseBuilder builder;

    private GalleryAdapter imageGalleryAdapter;

    private KeyboardHeightProvider keyboardHeightProvider;

    private List<Uri> selectedUriList;
    private List<Uri> tempUriList;
    private Uri cameraImageUri;

    private FrameLayout selectedPhotosContainerFrame;
    private LinearLayout selectedPhotosContainer;
    private ImageButton btnDone;
    private ImageButton btnDone1;
    private RecyclerView rcGallery;
    private TextView selectedPhotosEmpty;
    private TextView tvTitle;
    private View viewTitleContainer;
    private View appBarLayout;
    private LinearLayout container;
    private ConstraintLayout draggingPanel;
    // private LinearLayout draggingPanel;
    private LinearLayout.LayoutParams lp;
    private int margin = dpToPx(8);
    private int draggingHeight = dpToPx(3);
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageButton backBtn;
    private ConstraintLayout clCountContainer;
    private LinearLayout recentBtn;
    private TextView tvSelectedImageCount;
    private CardView cvDrag;
    private TextView tvSelectedImageCountBottom;
    private boolean isOpenToolbar = false;
    private float currentSlideOffset = 0f;
    private FrameLayout flBottomContainer;
    private EditTextExtended etInput;
    private AppCompatImageView btnSend;
    private View mainBehavierView;
    private boolean isInputShown = false;
    private int keyboardHeight = 0;
    private Act act;


    public IVideoDurationRequest duration;

    @Inject
    CameraProvider.Builder cameraProviderBuilder;

    @Inject
    FileManager fileManager;

    private final Runnable expandRunnable = () -> {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    };


    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View view, int i) {
            if (BottomSheetBehavior.STATE_EXPANDED == i) {
                container.setBackgroundColor(Color.WHITE);
                draggingPanel.setVisibility(View.INVISIBLE);
                backBtn.setClickable(true);
                recentBtn.setClickable(true);
            }
            if (BottomSheetBehavior.STATE_COLLAPSED == i) {
                container.setBackgroundColor(Color.TRANSPARENT);
                lp.height = draggingHeight;
                draggingPanel.setLayoutParams(lp);
            }
            if (BottomSheetBehavior.STATE_HIDDEN == i) {

                container.setBackgroundColor(Color.TRANSPARENT);
                lp.height = draggingHeight;
                draggingPanel.setLayoutParams(lp);
                dismissAllowingStateLoss();
            } else if (i == BottomSheetBehavior.STATE_DRAGGING) {

                backBtn.setClickable(false);
                recentBtn.setClickable(false);
                container.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            boolean isGoingDown = currentSlideOffset > slideOffset;
            currentSlideOffset = slideOffset;

            if (slideOffset > 0.75) appBarLayout.setAlpha(slideOffset);

            if (slideOffset <= 0.75 && isGoingDown) {
                float alpha = slideOffset - 0.59f;
                if (alpha < 0) alpha = 0;
                appBarLayout.setAlpha(alpha);
            }

            if (lp != null && slideOffset >= 0) {
                lp.topMargin = (int) (margin * (1 - slideOffset + slideOffset * 0.099));
                lp.bottomMargin = (int) (margin * (1 - slideOffset + slideOffset * 0.099));
                lp.height = (int) (draggingHeight * (1 - slideOffset + slideOffset * 0.099));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSavedInstanceState(savedInstanceState);
        App.component.inject(this);
        act = (Act) getActivity();
        if (act != null) act.setDialogColorStatusBar();
    }

    private void setupSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            cameraImageUri = builder.selectedUri;
            tempUriList = builder.selectedUriList;
        } else {
            cameraImageUri = savedInstanceState.getParcelable(EXTRA_CAMERA_IMAGE_URI);
            tempUriList = savedInstanceState.getParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri);
        if (selectedUriList != null) outState.putParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI, new ArrayList<>(selectedUriList));
        super.onSaveInstanceState(outState);
    }

    public void show(FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, getTag());
        ft.commitAllowingStateLoss();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NotNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
                layoutParams.height = MATCH_PARENT;
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                if (builder != null && builder.peekHeight > 0) {
                    behavior.setPeekHeight(builder.peekHeight);
                }
            }
        });

        if (builder == null) {
            dismissAllowingStateLoss();
            return;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.tedbottompicker_content_view, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        setTitle();
        setRecyclerView();
        setSelectionView();

        selectedUriList = new ArrayList<>();

        if (builder.onImageSelectedListener != null && cameraImageUri != null) {
            addUri(cameraImageUri);
        } else if (builder.onMultiImageSelectedListener != null && tempUriList != null) {
            for (Uri uri : tempUriList) {
                addUri(uri);
            }
        }

        setDoneButton();
        checkMultiMode();
        setupDurationListener();
    }

    private void setupDurationListener() {
        duration = (uri, listener) -> {
            final MutableLiveData<Long> liveData = new MutableLiveData<>();
            Thread t = new Thread(() -> {
                Long durationMils = fileManager.getVideoDurationMils(uri);
                // Timber.d("Bazaleev: DurationListener Duration is " + durationMils);
                liveData.postValue(durationMils);
            });
            Activity activity = getActivity();
            if (activity != null) {
                liveData.observe((LifecycleOwner) activity, listener::onResult);
                t.start();
            }
        };

        imageGalleryAdapter.setDurationListener(duration);
    }

    private void setSelectionView() {
        if (builder.emptySelectionText != null) {
            selectedPhotosEmpty.setText(builder.emptySelectionText);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.e("Bazaleev: tedBottomPickerOnSTOP");
        if (builder.onDialogDismissListener != null) {
            builder.onDialogDismissListener.onDialogDismiss();
        }
        if (mainBehavierView != null) {
            mainBehavierView.removeCallbacks(expandRunnable);
        }
    }

    private void setDoneButton() {
        btnDone.setOnClickListener(view -> onMultiSelectComplete());
        btnDone1.setOnClickListener(view -> onMultiSelectComplete());
    }

    private void onMultiSelectComplete() {
        if (selectedUriList.size() < builder.selectMinCount) {
            String message;
            if (builder.selectMinCountErrorText != null) {
                message = builder.selectMinCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_min_count), builder.selectMinCount);
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (builder.onImageWithTextReady != null) {
            if (etInput == null) onReadyChat("");
            else onReadyChat(etInput.getText().toString());
        } else
            builder.onMultiImageSelectedListener.onImagesSelected(selectedUriList);

        dismissAllowingStateLoss();
    }

    private void checkMultiMode() {
        if (!isMultiSelect()) {
            btnDone.setVisibility(View.INVISIBLE);
            selectedPhotosContainerFrame.setVisibility(View.GONE);
        }
    }

    private void initView(View contentView) {
        mainBehavierView = contentView;
        viewTitleContainer = contentView.findViewById(R.id.view_title_container);
        rcGallery = contentView.findViewById(R.id.rc_gallery);
        tvTitle = contentView.findViewById(R.id.tv_title);
        btnDone = contentView.findViewById(R.id.btn_done);
        appBarLayout = contentView.findViewById(R.id.appBarLayout);
        container = contentView.findViewById(R.id.ll_container_ted_bottom);
        draggingPanel = contentView.findViewById(R.id.cl_dragging_panel);
        clCountContainer = contentView.findViewById(R.id.cl_counter_container);
        tvSelectedImageCount = contentView.findViewById(R.id.tv_img_count);
        cvDrag = contentView.findViewById(R.id.cardView);
        tvSelectedImageCountBottom = contentView.findViewById(R.id.tv_media_counter);
        btnDone1 = contentView.findViewById(R.id.btn_done_1);

        try {
            lp = (LinearLayout.LayoutParams) draggingPanel.getLayoutParams();
            margin = lp.topMargin;
        } catch (Exception e) {
            Timber.e(e);
        }
        selectedPhotosContainerFrame = contentView.findViewById(R.id.selected_photos_container_frame);
        selectedPhotosContainer = contentView.findViewById(R.id.selected_photos_container);
        selectedPhotosEmpty = contentView.findViewById(R.id.selected_photos_empty);


        backBtn = contentView.findViewById(R.id.cancelBtn);
        recentBtn = contentView.findViewById(R.id.ll_recent);

        backBtn.setOnClickListener(v -> dismissAllowingStateLoss());
        recentBtn.setOnClickListener(v -> startGalleryIntent());

        if (builder.type == MediaControllerOpenPlace.Chat.INSTANCE) {
            btnDone.setVisibility(View.INVISIBLE);
            btnDone1.setVisibility(View.GONE);
            contentView.post(() -> {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                initBottomInput();

                if (builder.isSuggestionMenuActive) {
                    initSuggestionsMenu();
                }

                initKeyboardHeightProvider();
            });

        }

        mainBehavierView.post(expandRunnable);
    }

    private void setRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rcGallery.setLayoutManager(gridLayoutManager);
        rcGallery.setItemAnimator(null);
        rcGallery.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), builder.spacing, builder.includeEdgeSpacing));
        updateAdapter();
    }

    private void updateAdapter() {
        initImageGalleryAdapter();
        //imageGalleryAdapter.setHasStableIds(true); // need to disable image blinking
        imageGalleryAdapter.isMultiModeEnabled = builder.onMultiImageSelectedListener != null;
        rcGallery.setAdapter(imageGalleryAdapter);
        RecyclerView.ItemAnimator animator = rcGallery.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        imageGalleryAdapter.setOnItemClickListener((view, position) -> {

            PickerTile pickerTile = imageGalleryAdapter.getItem(position);
            switch (pickerTile.getTileType()) {
                case PickerTile.CAMERA:
                    startCameraIntent();
                    break;
                case PickerTile.GALLERY:
                    startGalleryIntent();
                    break;
                case PickerTile.IMAGE: {
                    handleResult(pickerTile, position);
                    break;
                }
                case PickerTile.VIDEO:
                    handleResult(pickerTile, position);
                default:
                    errorMessage();
            }
        });

        imageGalleryAdapter.setOnOpenImagePreview((view, position) -> {
            showPhotoMediaView(imageGalleryAdapter.getCollection(), position - 1);
        });
    }

    private void initImageGalleryAdapter(){
        CameraOrientation cameraOrientation = builder.cameraType == MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_BACK ? CameraOrientation.BACK : CameraOrientation.FRONT;

        CameraProvider cameraProvider = new CameraProvider.Builder(requireContext())
            .cameraOrientation(cameraOrientation)
            .build();

        imageGalleryAdapter = new GalleryAdapter(
            getContext(),
            builder,
            cameraProvider,
            getViewLifecycleOwner(),
            fileManager
        );
    }

    private void handleResult(PickerTile pickerTile, int position) {
        if (isMultiSelect()) {
            if (pickerTile.getImageUri() != null) {
                complete(pickerTile.getImageUri());
            }
            return;
        }
        if (builder.type == MediaControllerOpenPlace.Common.INSTANCE) {
            if (pickerTile.getImageUri() != null) {
                complete(pickerTile.getImageUri());
            }
        } else if (builder.type == MediaControllerOpenPlace.Gallery.INSTANCE) {
            showPhotoMediaView(imageGalleryAdapter.getCollection(), position - 1);
        } else if (builder.type == MediaControllerOpenPlace.Chat.INSTANCE) {
            showPhotoMediaView(imageGalleryAdapter.getCollection(), position - 1);
        } else {
            if (pickerTile.getImageUri() != null) {
                complete(pickerTile.getImageUri());
            }
        }
    }


    private class OrientationListener extends OrientationScreenListener {
        @Override
        public void onOrientationChanged(int orientation) {
            getOrientationChangedListener().invoke(orientation);
        }
    }

    private OrientationScreenListener screenListener = new OrientationListener();

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        screenListener.onOrientationChanged(newConfig.orientation);
    }

    private void showPhotoMediaView(List<PickerTile> list, int currentPosition) {
        if (list == null)
            return;
        if (list.size() == 0)
            return;
        list = list.subList(1, list.size());
        ArrayList<ImageViewerData> imageList = new ArrayList<>(list.size() + 1);
        for (PickerTile i : list) {
            int type;
            if (i.getTileType() == PickerTile.VIDEO)
                type = VIEW_TYPE_VIDEO_NOT_PLAYING;
            else type = VIEW_TYPE_IMAGE;
            ImageViewerData data = new ImageViewerData(i.getImageUri().toString(), "", -1L, type);
            if (imageGalleryAdapter.selectedPickerTiles.contains(i)) {
                data.setSelected(true);
                data.setCnt(i.getCounter());
            }
            imageList.add(data);
        }
        if (imageList.size() == 0)
            return;

        Act act = null;
        try {
            act = (Act) builder.fragmentActivity;
        } catch (Exception e) {
            Timber.e(e);
        }

        MediaViewer.Builder mediaBuilder = MediaViewer.Companion.with(getContext())
                .setImageList(imageList)
                .startPosition(currentPosition)
                .setType(builder.type)
                .setLifeCycle(getLifecycle())
                .setSelectedCount(imageGalleryAdapter.selectedPickerTiles.size())
                .setOrientationChangedListener(screenListener)
                .setUniqueNameSuggestionsMenu(new SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD, true))
                .onDismissListener(() -> {
                })
                .setAct(act) // act
                .onImageReady((img) -> {
                    complete(Uri.parse(img));
                    dismissAllowingStateLoss();
                    return Unit.INSTANCE;
                })
                .onChangeListener(new OnImageChangeListener() {
                    @Override
                    public void onImageChange(int position) {
                    }

                    @Override
                    public void onImageAdded(ImageViewerData image) {
                        Timber.d("Bazaleev onImageAdded with: " + image);
                        if (image.getViewType() == VIEW_TYPE_IMAGE) {
                            imageGalleryAdapter.addItemNotSelected(
                                    Uri.parse(image.getImageUrl()),
                                    VIEW_TYPE_IMAGE
                            );
                        } else if (image.getViewType() == VIEW_TYPE_VIDEO_NOT_PLAYING) {
                            onVideoReadyUri(Uri.parse(image.getImageUrl()));
                        }
                    }

                    @Override
                    public void onImageChecked(ImageViewerData image, boolean isChecked) {
                        Uri img = Uri.parse(image.getImageUrl());
                        if (isChecked) selectedUriList.add(img);
                        else selectedUriList.remove(img);
                        imageGalleryAdapter.setSelected(img, isChecked);
                        checkUriList();
                    }
                })
                .onImageEdit((img) -> {
                    if (builder.onImageEditListener != null) {
                        builder.onImageEditListener.onImageEdit(Uri.parse(img));
                        dismissAllowingStateLoss();
                    }
                    return Unit.INSTANCE;
                })
                .onImageReadyWithText((image, text) -> {
                    if (selectedUriList.isEmpty())
                        selectedUriList.addAll(image);
                    onReadyChat(text);
                    return Unit.INSTANCE;
                });
        if (builder.type == MediaControllerOpenPlace.Chat.INSTANCE) {
            mediaBuilder.setMessage(etInput.getText().toString());
            mediaBuilder.addTextWatcher((message) -> {
                etInput.setText(message);
                return Unit.INSTANCE;
            });
        }
        mediaBuilder.show();

    }


    private void complete(final Uri uri) {
        if (isMultiSelect()) {
            if (selectedUriList.contains(uri)) {
                removeImage(uri);
            } else {
                addUri(uri);
            }

        } else {
            builder.onImageSelectedListener.onImageSelected(uri);
            dismissAllowingStateLoss();
        }
    }

    private void animateHeigh(View v, int newHeight, int duration) {
        v.setVisibility(View.VISIBLE);
        v.measure(MATCH_PARENT, WRAP_CONTENT);
        com.meera.core.extensions.ViewKt.animateHeight(
                v,
                newHeight,
                duration, () -> Unit.INSTANCE
        );
    }

    private void checkUriList() {
        if (selectedUriList.size() > 0) {
            clCountContainer.setVisibility(View.VISIBLE);
            recentBtn.setVisibility(View.GONE);
            cvDrag.setVisibility(View.GONE);
            tvSelectedImageCountBottom.setVisibility(View.VISIBLE);
            if (builder.type != MediaControllerOpenPlace.Chat.INSTANCE) // if multi selected and chat
                btnDone1.setVisibility(View.VISIBLE);
            draggingHeight = dpToPx(24);
        } else {
            clCountContainer.setVisibility(View.INVISIBLE);
            recentBtn.setVisibility(View.VISIBLE);
            cvDrag.setVisibility(View.VISIBLE);
            tvSelectedImageCountBottom.setVisibility(View.GONE);
            btnDone1.setVisibility(View.GONE);
            draggingHeight = dpToPx(3);
        }
        if (selectedUriList.size() == 1
                && fileManager.getMediaType(selectedUriList.get(0)) == MEDIA_TYPE_VIDEO) {
            tvSelectedImageCountBottom.setText(R.string.one_video_selected);
            tvSelectedImageCount.setText(R.string.one_video_selected);
        } else if (selectedUriList.size() == 1) {
            tvSelectedImageCountBottom.setText(R.string.add_one_photo);
            tvSelectedImageCount.setText(R.string.add_one_photo);
        } else if (selectedUriList.size() > 1 && selectedUriList.size() <= 4) {
            tvSelectedImageCountBottom.setText(getString(R.string.add_few_photo, selectedUriList.size()));
            tvSelectedImageCount.setText(getString(R.string.add_few_photo, selectedUriList.size()));
        } else if (selectedUriList.size() == 5) {
            tvSelectedImageCountBottom.setText(getString(R.string.add_many_photo, selectedUriList.size()));
            tvSelectedImageCount.setText(getString(R.string.add_many_photo, selectedUriList.size()));
        }

        if (selectedUriList.size() > 0 && builder.type == MediaControllerOpenPlace.Chat.INSTANCE && !isInputShown) {
            showBottomInput();
        } else if (selectedUriList.size() == 0 && builder.type == MediaControllerOpenPlace.Chat.INSTANCE)
            hideBottomInput();
    }


    private void addUri(final Uri uri) {
        if (selectedUriList.size() == builder.selectMaxCount) {
            String message;
            if (builder.selectMaxCountErrorText != null) {
                message = builder.selectMaxCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_max_count), builder.selectMaxCount);
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }

        selectedUriList.add(uri);
        checkUriList();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri);

        if(act != null && fileManager.getMediaType(uri) == MEDIA_TYPE_VIDEO) {
            MediaControllerNeedEditResponse response = act.getMediaControllerFeature().needEditMedia(uri, MediaControllerOpenPlace.Common.INSTANCE);
            if(response instanceof MediaControllerNeedEditResponse.VideoTooLong) {
                act.getMediaControllerFeature().showVideoTooLongDialog(MediaControllerOpenPlace.Common.INSTANCE, (MediaControllerNeedEditResponse.VideoTooLong) response, true, new Function0() {
                    @Override
                    public Object invoke() {
                        openEditor(uri);
                        return null;
                    }
                });
            }
        }
    }

    private void removeImage(Uri uri) {
        selectedUriList.remove(uri);
        checkUriList();
        imageGalleryAdapter.setSelectedUriList(selectedUriList, uri);
    }

    private void enableAll() {
        selectedUriList.clear();
        imageGalleryAdapter.unSelectAll();
    }

    private void startCameraIntent() {
        switch (builder.mediaType) {
            case BaseBuilder.MediaType.IMAGE:
            case BaseBuilder.MediaType.IMAGE_AND_VIDEO:
                if (getActivity() != null) {
                    ImageCaptureUtils.getImageFromCamera(getActivity(), new ImageCaptureUtils.Listener() {
                        @Override
                        public void onResult(@NonNull ImageCaptureResultModel imageCaptureResultModel) {
                            cameraImageUri = imageCaptureResultModel.getFileUri();
                            complete(cameraImageUri);
                            addNewSelectedItemToAdapter(cameraImageUri);
                        }
                        @Override
                        public void onFailed() {
                            rcGallery.postDelayed(() -> {
                                updateAdapter();
                            }, 200);
                        }}, builder.cameraLensFacing
                    );
                }
                break;
            case BaseBuilder.MediaType.VIDEO:
            default:
                break;
        }
    }

    private void errorMessage(String message) {
        String errorMessage = message == null ? "Something wrong." : message;

        if (builder.onErrorListener == null) {
            //Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            builder.onErrorListener.onError(errorMessage);
        }
    }

    private void startGalleryIntent() {
        Intent galleryIntent;
        if (builder.mediaType == BaseBuilder.MediaType.IMAGE) {
            galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
        } else {
            galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("video/*");
        }

        if (galleryIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Phone do not have Gallery Application");
            return;
        }

        TedOnActivityResult.with(getActivity())
                .setIntent(galleryIntent)
                .setListener((resultCode, data) -> {
                    if (resultCode == Activity.RESULT_OK) {
                        onActivityResultGallery(data);
                    }
                })
                .startActivityForResult();
    }

    private void errorMessage() {
        errorMessage(null);
    }

    private void setTitle() {
        if (!builder.showTitle) {
            tvTitle.setVisibility(View.GONE);

            if (!isMultiSelect()) {
                viewTitleContainer.setVisibility(View.GONE);
            }

            return;
        }

        if (!TextUtils.isEmpty(builder.title)) {
            tvTitle.setText(builder.title);
        }

        if (builder.titleBackgroundResId > 0) {
            tvTitle.setBackgroundResource(builder.titleBackgroundResId);
        }
    }

    private boolean isMultiSelect() {
        return builder.onMultiImageSelectedListener != null;
    }

    private void addNewSelectedItemToAdapter(Uri uri) {
        imageGalleryAdapter.addItemSelected(uri);
    }

    private void onActivityResultGallery(Intent data) {
        Uri temp = data.getData();

        if (temp == null) {
            errorMessage();
        }

        String realPath = RealPathUtil.getRealPath(getActivity(), temp);

        Uri selectedImageUri;
        try {
            selectedImageUri = Uri.fromFile(new File(realPath));
        } catch (Exception ex) {
            if (realPath == null) return;
            selectedImageUri = Uri.parse(realPath);
        }

        if (builder.onMultiImageSelectedListener != null) {
            imageGalleryAdapter.setSelected(selectedImageUri, true);
        }

        complete(selectedImageUri);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Act act = (Act) getActivity();
        if (act != null) act.setStatusBar();
        if (builder.type == MediaControllerOpenPlace.Chat.INSTANCE) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            keyboardHeightProvider.release();
        }
        if (builder.onDialogDismissListener != null) {
            builder.onDialogDismissListener.onDialogDismiss();
        }
    }

    private void initKeyboardHeightProvider() {
        keyboardHeightProvider = new KeyboardHeightProvider(getDialog().getWindow().getDecorView());
        keyboardHeightProvider.setObserver(integer -> {
            adjustRootView(integer);
            return Unit.INSTANCE;
        });
    }

    private void adjustRootView(Integer keyboardHeight) {
        this.keyboardHeight = keyboardHeight;
        if (keyboardHeight > 0) {
            flBottomContainer.animate()
                    .translationY(-keyboardHeight)
                    .setDuration(150)
                    .start();

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            setSuggestionsMenuExtraPeekHeight(keyboardHeight + flBottomContainer.getHeight(), true);
        } else {
            if(isInputShown) {
                flBottomContainer.animate()
                        .translationY(0)
                        .setDuration(150)
                        .start();
            }

            setSuggestionsMenuExtraPeekHeight(flBottomContainer.getHeight(), true);
        }
    }

    private void setSuggestionsMenuExtraPeekHeight(int extraPeekHeight, boolean isAnimate) {
        if (suggestionsMenu != null && flBottomContainer != null) {
            suggestionsMenu.setExtraPeekHeight(extraPeekHeight, true);
        }
    }

    private void initBottomInput() {
        flBottomContainer = getDialog().findViewById(R.id.fl_bottom_container);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_input_layout_picker, null, false);
        etInput = v.findViewById(R.id.et_share_input);
        etInput.setHint(getContext().getString(R.string.add_message));
        etInput.setText(builder.message);
        btnSend = v.findViewById(R.id.btn_share_send);
        flBottomContainer.measure(MATCH_PARENT, WRAP_CONTENT);
        flBottomContainer.setTranslationY(CommonKt.getDp(-100));
        flBottomContainer.addView(v);
        btnSend.setOnClickListener(v1 -> onReadyChat(etInput.getText().toString()));
        flBottomContainer.post(this::hideBottomInput);
    }

    private View tagListView = null;
    private String lastSearchUniqueName = null;
    private RecyclerView tagListRecyclerView = null;
    private BottomSheetBehavior<View> tagListViewBehaviour = null;
    private com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu suggestionsMenu = null;

    private void initSuggestionsMenu() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            tagListView = dialog.findViewById(R.id.tags_list);
            if (tagListView != null) {
                tagListView.post(() -> {
                    tagListView.setVisibility(View.VISIBLE);
                    Act act = (Act) builder.fragmentActivity;
                    suggestionsMenu = new SuggestionsMenu(act.getCurrentFragment(), ROAD, true);
                    tagListViewBehaviour = BottomSheetBehavior.from(tagListView);
                    tagListRecyclerView = tagListView.findViewById(R.id.recycler_tags);
                    if (etInput != null && tagListRecyclerView != null && tagListViewBehaviour != null) {
                        suggestionsMenu.init(tagListRecyclerView, etInput, tagListViewBehaviour);
                        suggestionsMenu.setOnSuggestedUniqueNameClicked(uiTagEntity -> {
                            suggestionsMenu.forceCloseMenu();
                            replaceUniqueNameBySuggestion(uiTagEntity);
                            return Unit.INSTANCE;
                        });

                        initEditTextCallbacks();
                        tagListView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    private void initEditTextCallbacks() {
        etInput.setOnNewUniqueNameAfterTextChangedListener(this::searchUsersByUniqueName);

        etInput.setOnUniqueNameNotFoundListener(() -> {
            if (suggestionsMenu != null) {
                suggestionsMenu.forceCloseMenu();
            }
        });
    }

    private void searchUsersByUniqueName(String uniqueName) {
        if (suggestionsMenu != null) {
            lastSearchUniqueName = uniqueName;
            String replaceUniqueName = uniqueName.replaceAll("@", "");
            suggestionsMenu.searchUsersByUniqueName(replaceUniqueName);
        }
    }

    private void replaceUniqueNameBySuggestion(UITagEntity userData) {
        if (suggestionsMenu != null) {
            if (userData != null) {
                String newUniqueName = userData.getUniqueName();
                if (newUniqueName != null && lastSearchUniqueName != null && etInput != null) {
                    etInput.replaceUniqueNameBySuggestion(lastSearchUniqueName, newUniqueName);
                }
            }
        }
    }

    private void showBottomInput() {
        flBottomContainer.animate()
                .translationY(-keyboardHeight)
                .setDuration(100)
                .start();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rcGallery.getLayoutParams();
        if (params != null) {
            params.bottomMargin = flBottomContainer.getMeasuredHeight();
        }
        isInputShown = true;
    }

    private void hideBottomInput() {
        flBottomContainer.measure(MATCH_PARENT, WRAP_CONTENT);
        flBottomContainer.animate()
                .translationY(flBottomContainer.getMeasuredHeight())
                .setDuration(100)
                .start();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rcGallery.getLayoutParams();
        if (params != null) {
            params.bottomMargin = 0;
        }
        isInputShown = false;
    }

    private void openEditor(Uri uri) {
        try {
            Act act = (Act) builder.fragmentActivity;
            act.getMediaControllerFeature().open(
                    uri,
                MediaControllerOpenPlace.Chat.INSTANCE,
                new MediaControllerCallback() {
                        @Override
                        public void onPhotoReady(@NotNull Uri resultUri, NMRPhotoAmplitude nmrAmplitude) {}

                        @Override
                        public void onVideoReady(@NotNull Uri resultUri, NMRVideoAmplitude nmrAmplitude) {
                            onVideoReadyUri(resultUri);
                        }

                        @Override
                        public void onMediaListReady(@NonNull List<MediaEditorResult> results) {}

                        @Override
                        public void onError() {
                            enableAll();
                        }

                        @Override
                        public void onCanceled() {
                            enableAll();
                        }
                    },
                false
            );
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onVideoReadyUri(Uri resultUri) {
        imageGalleryAdapter.unSelectAll();
        imageGalleryAdapter.addAndSelectVideo(
                new PickerTile(resultUri,
                        PickerTile.VIDEO
                )
        );

        if(act != null && fileManager.getMediaType(resultUri) == MEDIA_TYPE_VIDEO) {
            MediaControllerNeedEditResponse response = act.getMediaControllerFeature().needEditMedia(resultUri, MediaControllerOpenPlace.Common.INSTANCE);
            if(response instanceof MediaControllerNeedEditResponse.VideoTooLong) {
                act.getMediaControllerFeature().showVideoTooLongDialog(MediaControllerOpenPlace.Common.INSTANCE, (MediaControllerNeedEditResponse.VideoTooLong) response, true, new Function0() {
                    @Override
                    public Object invoke() {
                        openEditor(resultUri);
                        return null;
                    }
                });
            }
        }
    }

    private void onReadyChat(String txt) {
        if (builder.onImageWithTextReady == null) return;
        builder.onImageWithTextReady.onImageWithText(selectedUriList, txt);
        dismissAllowingStateLoss();
    }

    public interface OnMultiImageSelectedListener {
        void onImagesSelected(List<Uri> uriList);
    }

    public interface OnImageSelectedListener {
        void onImageSelected(Uri uri);
    }

    public interface OnDialogDismissListener {
        void onDialogDismiss();
    }

    public interface OnRequestChangeBottomSheetStateListener {
        void onRequestChangeBottomSheetState(int state);
    }

    public interface OnImageEditListener {
        void onImageEdit(Uri uri);
    }

    public interface OnErrorListener {
        void onError(String message);
    }

    public interface ImageProvider {
        void onProvideImage(ImageView imageView, Uri imageUri);
    }

    public interface IOnImageWithTextReady {
        void onImageWithText(List<? extends Uri> image, String text);
    }

    public abstract static class BaseBuilder<T extends BaseBuilder> {
        public int previewMaxCount = 25;
        public Drawable cameraTileDrawable;
        public Drawable galleryTileDrawable;
        public Drawable selectedForegroundDrawable;
        public ImageProvider imageProvider;
        public boolean showCamera = true;
        public boolean showGallery = true;
        public int cameraTileBackgroundResId = R.color.transparent;
        public int galleryTileBackgroundResId = R.color.tedbottompicker_gallery;
        @MediaType
        public int mediaType = MediaType.IMAGE;
        public OnImageEditListener onImageEditListener;
        public Uri selectedUri;
        public MediaControllerOpenPlace type = MediaControllerOpenPlace.Common.INSTANCE;
        public MediaViewerCameraTypeEnum cameraType = MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_BACK;
        public IOnImageWithTextReady onImageWithTextReady;
        protected FragmentActivity fragmentActivity;
        public OnImageSelectedListener onImageSelectedListener;
        public OnMultiImageSelectedListener onMultiImageSelectedListener;
        OnErrorListener onErrorListener;
        OnDialogDismissListener onDialogDismissListener;
        public OnRequestChangeBottomSheetStateListener onRequestChangeBottomSheetStateListener;
        private String title;
        private boolean showTitle = true;
        public List<Uri> selectedUriList;
        private Drawable deSelectIconDrawable;
        private int spacing = 1;
        private boolean includeEdgeSpacing = false;
        private int peekHeight = -1;
        private int titleBackgroundResId;
        private int selectMaxCount = Integer.MAX_VALUE;
        private int selectMinCount = 0;
        private String completeButtonText;
        private String emptySelectionText;
        private String selectMaxCountErrorText;
        private String selectMinCountErrorText;
        private String message = "";
        private Boolean isSuggestionMenuActive = false;
        private @CameraLensFacing int cameraLensFacing;

        public BaseBuilder(@NonNull FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
            setCameraTile(com.meera.core.R.drawable.ic_outlined_cam_m);
            setGalleryTile(R.drawable.ic_gallery);
            setSpacingResId(R.dimen.material1);
        }

        public BaseBuilder<T> setMessage(String message) {
            this.message = message;
            return this;
        }

        public BaseBuilder<T> setWithPreview(MediaControllerOpenPlace type) {
            this.type = type;
            return this;
        }

        public BaseBuilder<T> setCameraTypePreview(MediaViewerCameraTypeEnum cameraType) {
            this.cameraType = cameraType;
            return this;
        }

        public T setOnImageReadyWithText(IOnImageWithTextReady onImageReadyWithText) {
            this.onImageWithTextReady = onImageReadyWithText;
            return (T) this;
        }

        public T setOnChangeBottomState(IOnImageWithTextReady onImageReadyWithText) {
            this.onImageWithTextReady = onImageReadyWithText;
            return (T) this;
        }

        public T setOnImageEditListener(OnImageEditListener listener) {
            this.onImageEditListener = listener;
            return (T) this;
        }

        public T setCameraTile(@DrawableRes int cameraTileResId) {
            setCameraTile(ContextCompat.getDrawable(fragmentActivity, cameraTileResId));
            return (T) this;
        }

        public BaseBuilder<T> setGalleryTile(@DrawableRes int galleryTileResId) {
            setGalleryTile(ContextCompat.getDrawable(fragmentActivity, galleryTileResId));
            return this;
        }

        public T setSpacingResId(@DimenRes int dimenResId) {
            this.spacing = fragmentActivity.getResources().getDimensionPixelSize(dimenResId);
            return (T) this;
        }

        public T setCameraTile(Drawable cameraTileDrawable) {
            this.cameraTileDrawable = cameraTileDrawable;
            return (T) this;
        }

        public T setGalleryTile(Drawable galleryTileDrawable) {
            this.galleryTileDrawable = galleryTileDrawable;
            return (T) this;
        }

        public T setDeSelectIcon(@DrawableRes int deSelectIconResId) {
            setDeSelectIcon(ContextCompat.getDrawable(fragmentActivity, deSelectIconResId));
            return (T) this;
        }

        public T setDeSelectIcon(Drawable deSelectIconDrawable) {
            this.deSelectIconDrawable = deSelectIconDrawable;
            return (T) this;
        }

        public T setSelectedForeground(@DrawableRes int selectedForegroundResId) {
            setSelectedForeground(ContextCompat.getDrawable(fragmentActivity, selectedForegroundResId));
            return (T) this;
        }

        public T setSelectedForeground(Drawable selectedForegroundDrawable) {
            this.selectedForegroundDrawable = selectedForegroundDrawable;
            return (T) this;
        }

        public T setPreviewMaxCount(int previewMaxCount) {
            this.previewMaxCount = previewMaxCount;
            return (T) this;
        }

        public T setSelectMaxCount(int selectMaxCount) {
            this.selectMaxCount = selectMaxCount;
            return (T) this;
        }

        public T setSelectMinCount(int selectMinCount) {
            this.selectMinCount = selectMinCount;
            return (T) this;
        }

        public T setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
            return (T) this;
        }

        public T setOnMultiImageSelectedListener(OnMultiImageSelectedListener onMultiImageSelectedListener) {
            this.onMultiImageSelectedListener = onMultiImageSelectedListener;
            return (T) this;
        }

        public T setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
            return (T) this;
        }

        public T showCameraTile(boolean showCamera) {
            this.showCamera = showCamera;
            return (T) this;
        }

        public T showGalleryTile(boolean showGallery) {
            this.showGallery = showGallery;
            return (T) this;
        }

        public T setSpacing(int spacing) {
            this.spacing = spacing;
            return (T) this;
        }

        public T setIncludeEdgeSpacing(boolean includeEdgeSpacing) {
            this.includeEdgeSpacing = includeEdgeSpacing;
            return (T) this;
        }

        public T setPeekHeight(int peekHeight) {
            this.peekHeight = peekHeight;
            return (T) this;
        }

        public T setPeekHeightResId(@DimenRes int dimenResId) {
            this.peekHeight = fragmentActivity.getResources().getDimensionPixelSize(dimenResId);
            return (T) this;
        }

        public T setCameraTileBackgroundResId(@ColorRes int colorResId) {
            this.cameraTileBackgroundResId = colorResId;
            return (T) this;
        }

        public T setGalleryTileBackgroundResId(@ColorRes int colorResId) {
            this.galleryTileBackgroundResId = colorResId;
            return (T) this;
        }

        public T setTitle(String title) {
            this.title = title;
            return (T) this;
        }

        public T setTitle(@StringRes int stringResId) {
            this.title = fragmentActivity.getResources().getString(stringResId);
            return (T) this;
        }

        public T showTitle(boolean showTitle) {
            this.showTitle = showTitle;
            return (T) this;
        }

        public T setDialogDismissListener(OnDialogDismissListener dialogDismissListener) {
            this.onDialogDismissListener = dialogDismissListener;
            return (T) this;
        }

        public T setOnRequestChangeBottomSheetState(OnRequestChangeBottomSheetStateListener requestChangeBottomSheetStateListener) {
            this.onRequestChangeBottomSheetStateListener = requestChangeBottomSheetStateListener;
            return (T) this;
        }

        public T setCompleteButtonText(String completeButtonText) {
            this.completeButtonText = completeButtonText;
            return (T) this;
        }

        public T setCompleteButtonText(@StringRes int completeButtonResId) {
            this.completeButtonText = fragmentActivity.getResources().getString(completeButtonResId);
            return (T) this;
        }

        public T setEmptySelectionText(String emptySelectionText) {
            this.emptySelectionText = emptySelectionText;
            return (T) this;
        }

        public T setEmptySelectionText(@StringRes int emptySelectionResId) {
            this.emptySelectionText = fragmentActivity.getResources().getString(emptySelectionResId);
            return (T) this;
        }

        public T setSelectMaxCountErrorText(String selectMaxCountErrorText) {
            this.selectMaxCountErrorText = selectMaxCountErrorText;
            return (T) this;
        }

        public T setSelectMaxCountErrorText(@StringRes int selectMaxCountErrorResId) {
            this.selectMaxCountErrorText = fragmentActivity.getResources().getString(selectMaxCountErrorResId);
            return (T) this;
        }

        public T setSelectMinCountErrorText(String selectMinCountErrorText) {
            this.selectMinCountErrorText = selectMinCountErrorText;
            return (T) this;
        }

        public T setSelectMinCountErrorText(@StringRes int selectMinCountErrorResId) {
            this.selectMinCountErrorText = fragmentActivity.getResources().getString(selectMinCountErrorResId);
            return (T) this;
        }

        public T setTitleBackgroundResId(@ColorRes int colorResId) {
            this.titleBackgroundResId = colorResId;
            return (T) this;
        }

        public T setImageProvider(ImageProvider imageProvider) {
            this.imageProvider = imageProvider;
            return (T) this;
        }

        public T setSelectedUriList(List<Uri> selectedUriList) {
            this.selectedUriList = selectedUriList;
            return (T) this;
        }

        public T setSelectedUri(Uri selectedUri) {
            this.selectedUri = selectedUri;
            return (T) this;
        }

        public T showVideoMedia() {
            this.mediaType = MediaType.VIDEO;
            return (T) this;
        }

        public T showImageAndVideoMedia() {
            this.mediaType = MediaType.IMAGE_AND_VIDEO;
            return (T) this;
        }

        public T setSuggestionMenuActive(Boolean isActive) {
            this.isSuggestionMenuActive = isActive;
            return (T) this;
        }

        public T setCameraLensFacing(int cameraLensFacing) {
            this.cameraLensFacing = cameraLensFacing;
            return (T) this;
        }

        public TedBottomSheetDialogFragment create() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && ContextCompat.checkSelfPermission(fragmentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?");
            }

            if (onImageSelectedListener == null && onMultiImageSelectedListener == null) {
                throw new RuntimeException("You have to use setOnImageSelectedListener() or setOnMultiImageSelectedListener() for receive selected Uri");
            }

            TedBottomSheetDialogFragment customBottomSheetDialogFragment = new TedBottomSheetDialogFragment();
            customBottomSheetDialogFragment.builder = this;
            return customBottomSheetDialogFragment;
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({MediaType.IMAGE, MediaType.VIDEO, MediaType.IMAGE_AND_VIDEO})
        public @interface MediaType {
            int IMAGE = 1;
            int VIDEO = 2;
            int IMAGE_AND_VIDEO = 3;
        }
    }

    public interface IVideoDurationRequest {
        void requestVideoDuration(Uri uri, IOnDurationReady listener);
    }

    public interface IOnDurationReady {
        void onResult(Long duration);
    }
}
