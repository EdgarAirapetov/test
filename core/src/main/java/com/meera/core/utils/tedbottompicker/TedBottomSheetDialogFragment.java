package com.meera.core.utils.tedbottompicker;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE;
import static com.meera.core.extensions.CommonKt.dpToPx;
import static com.meera.core.extensions.PermissionExtensionKt.returnReadExternalStoragePermissionAfter33;
import static com.meera.core.utils.files.FileUtilsImpl.MEDIA_TYPE_VIDEO;
import static com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment.BaseBuilder.NOT_SET;

import android.Manifest;
import android.animation.ValueAnimator;
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
import android.view.MotionEvent;
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
import com.meera.core.R;
import com.meera.core.base.enums.PermissionState;
import com.meera.core.bottomsheets.SuggestionsMenuContract;
import com.meera.core.dialogs.ConfirmDialogBuilder;
import com.meera.core.dialogs.MeeraConfirmDialogBuilder;
import com.meera.core.extensions.CommonKt;
import com.meera.core.extensions.ContextKt;
import com.meera.core.extensions.PermissionExtensionKt;
import com.meera.core.extensions.ViewKt;
import com.meera.core.utils.KeyboardHeightProvider;
import com.meera.core.utils.NSnackbar;
import com.meera.core.utils.NTimeKt;
import com.meera.core.utils.camera.CameraLensFacing;
import com.meera.core.utils.camera.CameraPreviewHelper;
import com.meera.core.utils.files.FileManager;
import com.meera.core.utils.files.FileUtilsImpl;
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils;
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel;
import com.meera.core.utils.layouts.FrameTouchEventInterceptorLayout;
import com.meera.core.utils.listeners.OrientationScreenListener;
import com.meera.core.utils.mediaviewer.ImageViewerData;
import com.meera.core.utils.mediaviewer.MediaViewer;
import com.meera.core.utils.mediaviewer.MediaViewerPhotoEditorCallback;
import com.meera.core.utils.mediaviewer.MediaViewerViewCallback;
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter;
import com.meera.core.utils.mediaviewer.listeners.OnImageChangeListener;
import com.meera.core.utils.tedbottompicker.adapter.GalleryAdapter;
import com.meera.core.utils.tedbottompicker.models.MediaUriModel;
import com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum;
import com.meera.core.utils.tedbottompicker.models.MediaViewerPreviewModeParams;
import com.meera.core.utils.tedbottompicker.util.RealPathUtil;
import com.meera.core.views.EditTextExtended;
import com.meera.core.views.MeeraSetupPermissionsView;
import com.meera.media_controller_common.MediaControllerOpenPlace;
import com.meera.uikit.widgets.buttons.ButtonType;
import com.meera.uikit.widgets.buttons.UiKitButton;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Unit;

public class TedBottomSheetDialogFragment extends BottomSheetDialogFragment {
    public static final int DEFAULT_CHAT_VIDEO_MAX_DURATION = 5 * 60;
    public static final int DEFAULT_VIDEO_MAX_DURATION = 1 * 60;
    private static final int DURATION_VIDEO_MS = 60 * 1000;
    private static final float SLIDE_OFFSET_INITIAL_VALUE = 0f;
    private static final float SLIDE_OFFSET_MAX_VALUE = 1f;
    private static final int PADDING_BOTTOM_RECYCLER = 120;
    private static final int RECYCLER_PADDING_BOTTOM_ANIMATOR_DURATION = 200;
    private static final int MAX_MEDIA_SNACK_MARGIN_BOTTOM = 116;
    private static final String EXTRA_CAMERA_IMAGE_URI = "camera_image_uri";
    private static final String EXTRA_CAMERA_SELECTED_IMAGE_URI = "camera_selected_image_uri";

    public BaseBuilder builder;


    private FileManager filesManager;

    private GalleryAdapter imageGalleryAdapter;

    private KeyboardHeightProvider keyboardHeightProvider;

    private List<MediaUriModel> selectedUriList;
    private List<MediaUriModel> tempUriList;
    private MediaUriModel cameraImageUri;

    private FrameLayout selectedPhotosContainerFrame;
    private LinearLayout selectedPhotosContainer;
    private ImageButton btnDone;
    private ImageButton btnDone1;
    private RecyclerView rcGallery;
    private FrameTouchEventInterceptorLayout fteilGalleryTouchContainer;
    private TextView selectedPhotosEmpty;
    private TextView tvTitle;
    private View viewTitleContainer;
    private View appBarLayout;
    private View divider;
    private UiKitButton cancelBtn;
    private UiKitButton doneBtn;
    private LinearLayout container;
    private ConstraintLayout draggingPanel;
    private LinearLayout.LayoutParams lp;
    private int margin = dpToPx(8);
    private int draggingHeight = dpToPx(3);
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageButton backBtn;
    private LinearLayout recentBtn;
    private TextView tvSelectedImageCount;
    private CardView cvDrag;
    private TextView tvSelectedImageCountBottom;
    private boolean isOpenToolbar = false;
    private float currentSlideOffset = 0f;
    private FrameLayout flBottomContainer;
    private EditTextExtended etInput;
    private AppCompatImageView btnSend;
    private MeeraSetupPermissionsView permissionsView;
    private LinearLayout contentLayout;
    private TextView toolbarName;
    private ImageView toolbarArrow;
    private View mainBehaviorView;
    private boolean isInputShown = false;
    private int keyboardHeight = 0;
    private int preSelectedCount = 0;
    ValueAnimator recyclerPaddingBottomAnimator;

    private boolean isInitialStateChanged = true;

    private TextView tvChangePermissionReadMediaVisual;
    private View vgPermissionMediaRequest;

    public IVideoDurationRequest duration;
    private GridLayoutManager galleryGridLayoutManager;

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
                setRecentBtnClickable();
            }
            if (BottomSheetBehavior.STATE_HALF_EXPANDED == i) {
                container.setBackgroundColor(Color.WHITE);
                draggingPanel.setVisibility(View.INVISIBLE);
                backBtn.setClickable(true);
                setRecentBtnClickable();
            }
            if (BottomSheetBehavior.STATE_COLLAPSED == i) {
                container.setBackgroundColor(Color.TRANSPARENT);
                lp.height = draggingHeight;
                draggingPanel.setLayoutParams(lp);
                backBtn.setClickable(true);
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

    public void updateAlreadySelectedMedia(List<MediaUriModel> mediaViewerEditedAttachmentInfo) {
        if (mediaViewerEditedAttachmentInfo != null) {
            builder.alreadySelectedMedia = mediaViewerEditedAttachmentInfo;
        }
        setupPeekHeight();
        setupBehaviorState();
        updateAdapter();
    }

    public void updatePreviewCollapsedHeight(int height) {
        builder.previewModeParams.setCollapsedHeight(height);
        setupPeekHeight();
    }

    public void updateGalleryPermissionState(PermissionState permissionState) {
        builder.permissionState = permissionState;
        if (permissionState == PermissionState.GRANTED) {
            setupViews(mainBehaviorView);
        } else {
            setupPermissionView(permissionState);
        }
    }

    public void openCamera() {
        imageGalleryAdapter.reloadCameraTile();
        startCameraIntent();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSavedInstanceState(savedInstanceState);
        if (builder.previewModeParams.isPreviewModeEnabled()) {
            setStyle(STYLE_NORMAL, R.style.TedBottomSheetPreviewModeDialogTheme);
        } else {
            builder.tedBottomSheetCallback.onSetDialogColorStatusBar();
        }
    }

    private void setupSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (builder.selectedUri != null) {
                cameraImageUri = MediaUriModel.Companion.initial(builder.selectedUri);
            }
            tempUriList = builder.selectedUriList;
        } else {
            cameraImageUri = savedInstanceState.getParcelable(EXTRA_CAMERA_IMAGE_URI);
            tempUriList = savedInstanceState.getParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_CAMERA_IMAGE_URI, cameraImageUri);
        if (selectedUriList != null)
            outState.putParcelableArrayList(EXTRA_CAMERA_SELECTED_IMAGE_URI, new ArrayList<>(selectedUriList));
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
        if (builder.previewModeParams.isPreviewModeEnabled() && getDialog() != null) {
            getDialog().getWindow().setDimAmount(0.0f);
            ((BottomSheetDialog) getDialog()).getBehavior().setPeekHeight(0);
        }
        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
                layoutParams.height = MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);
                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                setupPermissionViewHeight();
                setupPeekHeight();
                setupBehaviorState();
                setupOutsideTouch(bottomSheetDialog);
            }
        });

        if (builder == null) {
            dismissAllowingStateLoss();
        }
    }

    private void setupPermissionViewHeight() {
        if (permissionsView == null) return;

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) permissionsView.getLayoutParams();
        if (builder.previewModeParams.isPreviewModeEnabled()) {
            lp.height = WRAP_CONTENT;
        } else {
            lp.height = MATCH_PARENT;
        }
        permissionsView.setLayoutParams(lp);
    }

    private void setupPeekHeight() {
        if (builder.previewModeParams.isPreviewModeEnabled()) {
            if ((!isMultiPicking() && builder.alreadySelectedMedia.isEmpty())
                || (isMultiPicking() && (selectedUriList == null || selectedUriList.isEmpty()))) {
                bottomSheetBehavior.setPeekHeight(builder.previewModeParams.getHalfExtendedHeight(), true);
            } else {
                bottomSheetBehavior.setPeekHeight(builder.previewModeParams.getCollapsedHeight(), true);
            }
            setupRecyclerPadding(bottomSheetBehavior.getState());
        } else if (builder.peekHeight > 0) {
            bottomSheetBehavior.setPeekHeight(builder.peekHeight);
        }
    }

    private void setupBehaviorState() {
        int startState;
        if (!builder.previewModeParams.isPreviewModeEnabled()) {
            bottomSheetBehavior.setSkipCollapsed(true);
            startState = BottomSheetBehavior.STATE_EXPANDED;
        } else {
            startState = BottomSheetBehavior.STATE_COLLAPSED;
            bottomSheetBehavior.setSkipCollapsed(false);
            bottomSheetBehavior.addBottomSheetCallback(previewModeBottomSheetCallback);
        }
        bottomSheetBehavior.setState(startState);
        previewModeInitialStateChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupOutsideTouch(BottomSheetDialog bottomSheetDialog) {
        if (builder.previewModeParams.isPreviewModeEnabled()) {
            bottomSheetDialog.setCanceledOnTouchOutside(false);

            View touchOutsideView = bottomSheetDialog.getWindow()
                .getDecorView()
                .findViewById(R.id.touch_outside);

            touchOutsideView.setOnTouchListener((View v, MotionEvent event) -> {
                event.setLocation(event.getRawX(), event.getRawY());
                if (getActivity() != null) {
                    getActivity().getWindow().getDecorView().dispatchTouchEvent(event);
                }
                return false;
            });
        }
    }

    private final BottomSheetBehavior.BottomSheetCallback previewModeBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            setupRecyclerPadding(newState);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            previewModeBottomSheetSlide(slideOffset);
        }
    };

    private void previewModeBottomSheetSlide(Float slideOffset) {
        if (permissionsView == null) return;

        int screenHeight = ViewKt.getScreenHeight();
        float peekHeightRatio = (float) bottomSheetBehavior.getPeekHeight() / screenHeight;
        if (slideOffset == null) slideOffset = SLIDE_OFFSET_INITIAL_VALUE;
        float slideOffsetFinal = SLIDE_OFFSET_MAX_VALUE;

        if (slideOffset > SLIDE_OFFSET_INITIAL_VALUE) {
            slideOffsetFinal = peekHeightRatio + slideOffset;
        } else if (slideOffset <= SLIDE_OFFSET_INITIAL_VALUE) {
            slideOffsetFinal = peekHeightRatio;
        }

        slideOffsetFinal = Math.min(slideOffsetFinal, SLIDE_OFFSET_MAX_VALUE);

        int viewPositionWithCollapsedState = (int) (((screenHeight - permissionsView.getHeight()) / 2)
            * peekHeightRatio - appBarLayout.getHeight() - ContextKt.getStatusBarHeight(requireContext()));
        int viewPositionCenter = ((screenHeight - permissionsView.getHeight()) / 2) - appBarLayout.getHeight();

        int offset = viewPositionWithCollapsedState;
        int viewPositionWithExpandedState =
            (int) (viewPositionCenter - ((viewPositionCenter - viewPositionWithCollapsedState) / (SLIDE_OFFSET_MAX_VALUE - peekHeightRatio))
                * (SLIDE_OFFSET_MAX_VALUE - slideOffsetFinal));

        if (slideOffsetFinal > peekHeightRatio) offset = viewPositionWithExpandedState;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) permissionsView.getLayoutParams();
        lp.setMargins(lp.leftMargin, offset, lp.rightMargin, lp.bottomMargin);
        permissionsView.setLayoutParams(lp);
    }

    public void setupRecyclerPadding(int bottomSheetState) {
        cancelRecyclerPaddingAnimation();
        if (rcGallery == null) return;

        if (bottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            recyclerPaddingBottomAnimator = ValueAnimator.ofInt(rcGallery.getPaddingBottom(), 0);
            recyclerPaddingBottomAnimator.addUpdateListener(valueAnimator ->
                rcGallery.setPadding(rcGallery.getPaddingLeft(), rcGallery.getPaddingTop(), rcGallery.getPaddingRight(), (int) valueAnimator.getAnimatedValue()));
            recyclerPaddingBottomAnimator.setDuration(RECYCLER_PADDING_BOTTOM_ANIMATOR_DURATION);
            recyclerPaddingBottomAnimator.start();
        } else if (bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
            int screenHeight = ViewKt.getScreenHeight();
            int peekHeight = bottomSheetBehavior.getPeekHeight();
            int bottomPadding = screenHeight - peekHeight;
            rcGallery.setClipToPadding(false);
            rcGallery.setPadding(rcGallery.getPaddingLeft(), rcGallery.getPaddingTop(), rcGallery.getPaddingRight(), bottomPadding);
        }
    }

    private void cancelRecyclerPaddingAnimation() {
        if (recyclerPaddingBottomAnimator != null) recyclerPaddingBottomAnimator.cancel();
    }

    private void previewModeInitialStateChanged() {
        if (!isInitialStateChanged || !builder.previewModeParams.isPreviewModeEnabled()) return;

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            isInitialStateChanged = false;
            previewModeBottomSheetSlide(null);
            setupRecyclerPadding(bottomSheetBehavior.getState());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.meera_tedbottompicker_content_view, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        if (builder.permissionState == PermissionState.GRANTED) {
            setupViews(view);
        } else {
            setupPermissionView(builder.permissionState);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void initParamViewRedesign(){
        container.setBackgroundResource(R.color.transparent);
        appBarLayout.setBackgroundResource(R.drawable.meera_bg_mediakeyboard_tabs);
        divider.setVisibility(View.GONE);
        rcGallery.setBackgroundResource(R.color.uiKitColorBackgroundSecondary);
        tvChangePermissionReadMediaVisual.setTextColor(R.color.map_friend_A6C90D);
        rcGallery.setClipToPadding(false);
        rcGallery.setPadding(0,0,0, PADDING_BOTTOM_RECYCLER);

        btnDone.setVisibility(View.GONE);
        draggingPanel.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);
        cancelBtn.setOnClickListener(v -> dismissAllowingStateLoss());
        doneBtn.setOnClickListener(view -> onMultiSelectComplete());
        tvChangePermissionReadMediaVisual.setTextColor(R.color.uiKitColorForegroundLightGreen);
    }

    private void setupViews(View view) {
        filesManager = new FileUtilsImpl(requireContext());

        contentLayout.setVisibility(View.VISIBLE);
        permissionsView.setVisibility(View.GONE);
        toolbarName.setText(requireContext().getResources().getString(R.string.recent));
        toolbarArrow.setVisibility(View.VISIBLE);

        setTitle();
        setRecyclerView();
        setSelectionView();

        selectedUriList = new ArrayList<>();

        if (builder.alreadySelectedMedia != null) {
            selectedUriList.addAll(builder.alreadySelectedMedia);
            setupSelectedMultipleMedia(builder.alreadySelectedMedia);
        }

        if (builder.onImageSelectedListener != null && cameraImageUri != null) {
            addUri(cameraImageUri, false);
        } else if (
            (builder.onMultiImageSelectedListener != null || builder.onMultiMediaSelectedListener != null) && tempUriList != null) {
            for (MediaUriModel uri : tempUriList) {
                addUri(uri, false);
            }
        }

        setDoneButton();
        checkMultiMode();
        setupDurationListener();
    }

    private void setupPermissionView(PermissionState permissionState) {
        recentBtn.setClickable(false);
        contentLayout.setVisibility(View.GONE);
        permissionsView.setVisibility(View.VISIBLE);

        toolbarName.setText(requireContext().getResources().getString(R.string.gallery_title));
        toolbarArrow.setVisibility(View.GONE);

        checkMultiMode();

        permissionsView.bind(
            permissionState,
            this::onRequestPermissions,
            this::onOpenSettings
        );
    }

    private Unit onRequestPermissions() {
        if (builder.permissionActionsListener != null) {
            builder.permissionActionsListener.onGalleryRequestPermissions();
        }
        return Unit.INSTANCE;
    }

    private Unit onOpenSettings() {
        if (builder.permissionActionsListener != null) {
            builder.permissionActionsListener.onGalleryOpenSettings();
        }
        return Unit.INSTANCE;
    }

    private void setupDurationListener() {
        duration = (uri, listener) -> {
            final MutableLiveData<Long> liveData = new MutableLiveData<>();
            Thread t = new Thread(() -> {
                Long durationMils = filesManager.getVideoDurationMils(uri);
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
    public void onResume() {
        super.onResume();
        checkGalleryVisibilityLimit();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (builder.onDialogDismissListener != null) {
            builder.onDialogDismissListener.onDialogDismiss();
        }
        if (mainBehaviorView != null) {
            mainBehaviorView.removeCallbacks(expandRunnable);
        }
    }

    @Override
    public void onDestroy() {
        rcGallery = null;
        imageGalleryAdapter = null;
        cancelRecyclerPaddingAnimation();
        super.onDestroy();
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
        } else {
            builder.onMultiImageSelectedListener.onImagesSelected(
                selectedUriList.stream()
                    .map(MediaUriModel::getInitialUri)
                    .collect(Collectors.toList()));
        }

        dismissAllowingStateLoss();
    }

    private void checkMultiMode() {
        if (!isMultiSelect()) {
            btnDone.setVisibility(View.INVISIBLE);
            selectedPhotosContainerFrame.setVisibility(View.GONE);
        }
    }

    private void initView(View contentView) {
        mainBehaviorView = contentView;
        viewTitleContainer = contentView.findViewById(R.id.view_title_container);
        rcGallery = contentView.findViewById(R.id.rc_gallery);
        fteilGalleryTouchContainer = contentView.findViewById(R.id.fteil_gallery_touch_container);
        tvTitle = contentView.findViewById(R.id.tv_title);
        btnDone = contentView.findViewById(R.id.btn_done);
        divider = contentView.findViewById(R.id.v_divider);
        cancelBtn = contentView.findViewById(R.id.v_cancel_btn);
        doneBtn = contentView.findViewById(R.id.v_done_btn);
        appBarLayout = contentView.findViewById(R.id.appBarLayout);
        container = contentView.findViewById(R.id.ll_container_ted_bottom);
        draggingPanel = contentView.findViewById(R.id.cl_dragging_panel);
        tvSelectedImageCount = contentView.findViewById(R.id.tv_img_count);
        cvDrag = contentView.findViewById(R.id.cardView);
        tvSelectedImageCountBottom = contentView.findViewById(R.id.tv_media_counter);
        btnDone1 = contentView.findViewById(R.id.btn_done_1);
        permissionsView = contentView.findViewById(R.id.spv_permission_view);
        contentLayout = contentView.findViewById(R.id.ll_content_layout);
        toolbarName = contentView.findViewById(R.id.tv_toolbar_name);
        toolbarArrow = contentView.findViewById(R.id.iv_toolbar_arrow);
        contentLayout = contentView.findViewById(R.id.ll_content_layout);
        try {
            lp = (LinearLayout.LayoutParams) draggingPanel.getLayoutParams();
            margin = lp.topMargin;
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectedPhotosContainerFrame = contentView.findViewById(R.id.selected_photos_container_frame);
        selectedPhotosContainer = contentView.findViewById(R.id.selected_photos_container);
        selectedPhotosEmpty = contentView.findViewById(R.id.selected_photos_empty);


        backBtn = contentView.findViewById(R.id.cancelBtn);
        recentBtn = contentView.findViewById(R.id.ll_recent);

        backBtn.setOnClickListener(v -> dismissAllowingStateLoss());

        if (checkConditionsGalleryVisibilityLimit()) {
            toolbarName.setTextColor(getResources().getColor(R.color.uiKitColorDisabledPrimary, null));
            recentBtn.setClickable(false);
        } else {
            initClickListenerRecent();
        }

        tvChangePermissionReadMediaVisual = contentView.findViewById(R.id.tv_change_permission_red_media_visual);
        vgPermissionMediaRequest = contentView.findViewById(R.id.vg_permission_media_request);

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

        if (builder.previewModeParams.isPreviewModeEnabled()) {
            Drawable background = ContextCompat.getDrawable(contentView.getContext(), R.drawable.top_rounded_bottom_sheet);

            contentView.setBackground(background);
            appBarLayout.setBackground(background);
            container.setNestedScrollingEnabled(false);
            rcGallery.setNestedScrollingEnabled(false);
        } else {
            mainBehaviorView.post(expandRunnable);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        galleryGridLayoutManager = gridLayoutManager;
        rcGallery.setLayoutManager(gridLayoutManager);
        rcGallery.setItemAnimator(null);

            initParamViewRedesign();
            rcGallery.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), dpToPx(2), false));

        updateAdapter();

        if (builder.previewModeParams.isPreviewModeEnabled()) {
            fteilGalleryTouchContainer.enableTouchEventIntercept();
            if (imageGalleryAdapter != null)
                imageGalleryAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

            rcGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    enableCameraItemPreview(calculateEnableCameraParameter());
                }
            });
        }
    }

    private boolean calculateEnableCameraParameter() {
        return (galleryGridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0)
            && (!isMultiPicking() || !isMaxMediaCountSelected());
    }

    private void checkGalleryVisibilityLimit() {
        if (checkConditionsGalleryVisibilityLimit()) {
            vgPermissionMediaRequest.setVisibility(View.VISIBLE);
            toolbarName.setTextColor(getResources().getColor(R.color.uiKitColorDisabledPrimary, null));
            recentBtn.setClickable(false);
            tvChangePermissionReadMediaVisual.setOnClickListener(v -> {
                onOpenSettings();
            });
        } else if (hasMediaPermissions()) {
            vgPermissionMediaRequest.setVisibility(View.GONE);
            updateGalleryPermissionState(PermissionState.GRANTED);
        } else {
            vgPermissionMediaRequest.setVisibility(View.GONE);
            initClickListenerRecent();
            if (imageGalleryAdapter != null && (!isMultiPicking() && !isMultiSelect())) {
                imageGalleryAdapter.setSelectedUriList(builder.alreadySelectedMedia);
            }
        }
    }

    private void initClickListenerRecent(){
        toolbarName.setTextColor(getResources().getColor(R.color.ui_black, null));
        recentBtn.setOnClickListener(v -> {
                recentBtn.setClickable(false);
                startGalleryIntent();
        });
        setRecentBtnClickable();
    }

    private void setRecentBtnClickable() {
        boolean isPermitted = builder.permissionState == PermissionState.GRANTED;
        recentBtn.setClickable(isPermitted);
    }

    private Boolean hasMediaPermissions() {
        return PermissionExtensionKt.isEnabledPermission(
            requireContext(),
            returnReadExternalStoragePermissionAfter33()
        );
    }

    private Boolean checkConditionsGalleryVisibilityLimit() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            PermissionExtensionKt.isEnabledPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) &&
            (
                !PermissionExtensionKt.isEnabledPermission(requireContext(), READ_MEDIA_IMAGES) ||
                    !PermissionExtensionKt.isEnabledPermission(requireContext(), READ_MEDIA_VIDEO)
            );
    }

    private void enableCameraItemPreview(boolean enable) {
        if (imageGalleryAdapter == null) return;

        imageGalleryAdapter.enableCameraPreview(enable);
    }

    private void updateAdapter() {
        if (imageGalleryAdapter == null) {
            if (getContext() != null) {
                CameraPreviewHelper cameraPreviewHelper = new CameraPreviewHelper(requireContext());
                imageGalleryAdapter = new GalleryAdapter(
                    getContext(),
                    builder,
                    cameraPreviewHelper,
                    getViewLifecycleOwner(),
                    filesManager,
                    builder.alreadySelectedMedia,
                    builder.cameraLensFacing,
                    builder.selectMaxCount
                );
                imageGalleryAdapter.isMultiModeEnabled = builder.onMultiImageSelectedListener != null;
                imageGalleryAdapter.isMultiPickingEnabled = builder.onMultiMediaSelectedListener != null;
                imageGalleryAdapter.isPreviewModeEnabled = builder.previewModeParams.isPreviewModeEnabled();
                if (duration != null) imageGalleryAdapter.setDurationListener(duration);
                rcGallery.setAdapter(imageGalleryAdapter);
                RecyclerView.ItemAnimator animator = rcGallery.getItemAnimator();
                if (animator instanceof SimpleItemAnimator) {
                    ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
                }
                imageGalleryAdapter.setOnItemClickListener((view, position) -> {
                    if (position < 0) return;

                    GalleryAdapter.PickerTile pickerTile = imageGalleryAdapter.getItem(position);
                    switch (pickerTile.getTileType()) {
                        case GalleryAdapter.PickerTile.CAMERA:
                            if (!isMultiPicking()) {
                                startCameraIntent();
                            } else {
                                if (!isMaxMediaCountSelected()) {
                                    startCameraIntent();
                                }
                            }
                            break;
                        case GalleryAdapter.PickerTile.GALLERY:
                            if (!checkConditionsGalleryVisibilityLimit()) startGalleryIntent();
                            break;
                        case GalleryAdapter.PickerTile.IMAGE: {
                            handleResult(pickerTile, position);
                            break;
                        }
                        case GalleryAdapter.PickerTile.VIDEO:
                            handleResult(pickerTile, position);
                        default:
                            errorMessage();
                    }
                });

                imageGalleryAdapter.setOnOpenImagePreview((view, position) -> {
                    showPhotoMediaView(imageGalleryAdapter.getCollection(), position - 1);
                });
            }
        } else {
            selectedUriList.clear();
            selectedUriList.addAll(builder.alreadySelectedMedia);
            imageGalleryAdapter.setSelectedUriList(builder.alreadySelectedMedia);
            setupSelectedMultipleMedia(builder.alreadySelectedMedia);
        }

    }

    private void handleResult(GalleryAdapter.PickerTile pickerTile, int position) {
        if (isMultiSelect() || isMultiPicking() || builder.previewModeParams.isPreviewModeEnabled()) {
            if (pickerTile.getImageUriModel() != null) {
                complete(pickerTile.getImageUriModel(), pickerTile.getTileType());
            }
            return;
        }
        if (builder.type == MediaControllerOpenPlace.Common.INSTANCE) {
            if (pickerTile.getImageUriModel() != null) {
                complete(pickerTile.getImageUriModel(), pickerTile.getTileType());
            }
        } else if (builder.type == MediaControllerOpenPlace.Gallery.INSTANCE) {
            showPhotoMediaView(imageGalleryAdapter.getCollection(), position - 1);
        } else if (builder.type == MediaControllerOpenPlace.Chat.INSTANCE) {
            showPhotoMediaView(imageGalleryAdapter.getCollection(), position - 1);
        } else {
            if (pickerTile.getImageUriModel() != null) {
                complete(pickerTile.getImageUriModel(), pickerTile.getTileType());
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

    private void showPhotoMediaView(List<GalleryAdapter.PickerTile> list, int currentPosition) {
        if (list == null)
            return;
        if (list.size() == 0)
            return;
        list = list.subList(1, list.size());
        ArrayList<ImageViewerData> imageList = new ArrayList<>(list.size() + 1);
        for (GalleryAdapter.PickerTile i : list) {
            int type;
            if (i.getTileType() == GalleryAdapter.PickerTile.VIDEO)
                type = RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING;
            else type = RecyclingPagerAdapter.VIEW_TYPE_IMAGE;
            MediaUriModel uriModel = i.getImageUriModel();
            if (uriModel != null) {
                ImageViewerData data = new ImageViewerData(
                    uriModel,
                    "", -1L, type);
                if (imageGalleryAdapter.selectedPickerTiles.contains(i)) {
                    data.setSelected(true);
                    data.setCnt(i.getCnt());
                }
                imageList.add(data);
            }
        }
        if (imageList.size() == 0)
            return;

        preSelectedCount = 0;

        for (MediaUriModel item : selectedUriList) {
            if (item.getNetworkId() != null) {
                preSelectedCount++;
            }
        }

        MediaViewer.Builder mediaBuilder = MediaViewer.Companion.with(getContext())
            .setPreSelectedCount(preSelectedCount)
            .setImageList(imageList)
            .startPosition(currentPosition)
            .setType(builder.type)
            .setLifeCycle(getLifecycle())
            .setSelectedCount(imageGalleryAdapter.selectedPickerTiles.size())
            .setOrientationChangedListener(screenListener)
            .setUniqueNameSuggestionsMenu(builder.suggestionsMenu)
            .addMediaViewerPhotoEditorCallback(builder.mediaViewerPhotoEditorCallback)
            .onDismissListener(() -> {
            })
            .setSupportFragmentManager(builder.supportFragmentManager)
            .addMediaViewerViewCallback(builder.mediaViewerViewCallback)
            .onImageReady((img) -> {
                complete(MediaUriModel.Companion.initial(Uri.parse(img)), null);
                dismissAllowingStateLoss();
                return Unit.INSTANCE;
            })
            .onChangeListener(new OnImageChangeListener() {
                @Override
                public void onImageChange(int position) {
                }

                @Override
                public void onImageAdded(ImageViewerData image) {
                    if (image.getViewType() == RecyclingPagerAdapter.VIEW_TYPE_IMAGE) {
                        imageGalleryAdapter.addItemNotSelected(
                            image.getMediaUriModel(),
                            RecyclingPagerAdapter.VIEW_TYPE_IMAGE
                        );
                    } else if (image.getViewType() == RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING) {
                        onVideoReadyUri(image.getMediaUriModel());
                    }
                }

                @Override
                public void onImageChecked(ImageViewerData image, boolean isChecked) {
                    MediaUriModel model = image.getMediaUriModel();
                    if (isChecked) {
                        selectedUriList.add(model);
                        imageGalleryAdapter.addItemInSelectedList(model);
                    } else {
                        selectedUriList.remove(model);
                        imageGalleryAdapter.removeItemFromSelectedList(model);
                    }
                    imageGalleryAdapter.setSelected(model, isChecked, preSelectedCount);
                    checkUriList();
                }

                @Override
                public void onImageEdited(ImageViewerData image) {
                    updateMedia(image);
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
                    selectedUriList.addAll(image.stream().map(
                        MediaUriModel.Companion::initial
                    ).collect(Collectors.toList()));
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
        if (builder.type == MediaControllerOpenPlace.CreatePost.INSTANCE) {
            mediaBuilder.setVideoMaxLength(getVideoMaxDuration());
            mediaBuilder.setMaxCount(builder.selectMaxCount);
        }
        mediaBuilder.show();

    }

    private void updateMedia(ImageViewerData image) {
        MediaUriModel media = image.getMediaUriModel();
        for (MediaUriModel item : selectedUriList) {
            if (media.getInitialUri() == item.getInitialUri()) {
                item.setEditedUri(media.getEditedUri());
            }
        }
        imageGalleryAdapter.loadTiles();
        builder.onMultiMediaSelectedListener.onMediaUrisListChanges(selectedUriList);
    }

    private void complete(final MediaUriModel media, Integer tyleTipe) {
        if (isMultiSelect() || isMultiPicking()) {
            handleMultiSelectCompleteActions(media);
        } else if (builder.previewModeParams.isPreviewModeEnabled()) {
            if (selectedUriList != null && !selectedUriList.isEmpty() && selectedUriList.get(0).isEdited()) {
                showEditedMediaRemoveWarning(() -> handlePreviewModeCompleteActions(media, tyleTipe));
            } else {
                handlePreviewModeCompleteActions(media, tyleTipe);
            }
        } else {
            builder.onImageSelectedListener.onImageSelected(media.getActualUri());
            dismissAllowingStateLoss();
        }
    }

    private void handleMultiSelectCompleteActions(MediaUriModel media) {
        if (selectedUriList.contains(media)) {
            if (media.isEdited()) {
                showEditedMediaRemoveWarning(() -> removeImage(media));
            } else {
                removeImage(media);
            }
        } else {
            addUri(media, false);
        }
    }

    private void showEditedMediaRemoveWarning(Runnable action) {
        new MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.post_reset_media_dialog_title))
            .setDescription(getString(R.string.post_reset_media_dialog_description))
            .setTopBtnText(getString(R.string.post_reset_media_dialog_action))
            .setTopBtnType(ButtonType.FILLED_ERROR)
            .setTopClickListener(()-> {
                action.run();
                return Unit.INSTANCE;
            })
            .setBottomBtnText(getString(R.string.cancel))
            .setCancelable(false)
            .show(getChildFragmentManager());
    }

    private void handlePreviewModeCompleteActions(MediaUriModel media, Integer tyleTipe) {
        if (selectedUriList!= null &&
            !selectedUriList.isEmpty()
            && (selectedUriList.get(0))
            .getInitialUri() == media.getInitialUri()) {
            builder.onImageSelectedListener.onImageUnselected();
            removeImage(media);
        } else {
            if (tyleTipe != null && tyleTipe == GalleryAdapter.PickerTile.VIDEO) {
                checkVideoDurationBeforeSelect(media);
            } else {
                builder.onImageSelectedListener.onImageSelected(media.getActualUri());
                changeBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    private void checkVideoDurationBeforeSelect(MediaUriModel media) {
        if (duration != null) {
            duration.requestVideoDuration(media.getActualUri(), duration -> {
                long secondsDuration = duration / 1000;
                int maxVideoDuration = getVideoMaxDuration();

                if (secondsDuration <= maxVideoDuration) {
                    selectItem(media);
                }
                builder.onImageSelectedListener.onImageSelected(media.getActualUri());
                changeBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
            });
        }
    }

    private void changeBottomSheetState(int state) {
        if (bottomSheetBehavior != null) bottomSheetBehavior.setState(state);
    }

    private int getVideoMaxDuration() {
        int maxVideoDuration;
        if (builder.type == MediaControllerOpenPlace.Chat.INSTANCE) {
            maxVideoDuration = DEFAULT_CHAT_VIDEO_MAX_DURATION;
        } else {
            if (builder.videoMaxDuration != NOT_SET) {
                maxVideoDuration = builder.videoMaxDuration;
            } else {
                maxVideoDuration = DEFAULT_VIDEO_MAX_DURATION;
            }
        }

        return maxVideoDuration;
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

        if (!selectedUriList.isEmpty()) {
            tvSelectedImageCount.setVisibility(View.VISIBLE);
            recentBtn.setVisibility(View.GONE);
            cvDrag.setVisibility(View.GONE);
            tvSelectedImageCountBottom.setVisibility(View.VISIBLE);
            if (builder.type != MediaControllerOpenPlace.Chat.INSTANCE) // if multi selected and chat
                btnDone1.setVisibility(View.VISIBLE);
            if (isMultiPicking()) {
                builder.onMultiMediaSelectedListener.onMediaUrisListChanges(selectedUriList);
            }
            draggingHeight = dpToPx(24);
        } else {
            tvSelectedImageCount.setVisibility(View.INVISIBLE);
            recentBtn.setVisibility(View.VISIBLE);
            cvDrag.setVisibility(View.VISIBLE);
            tvSelectedImageCountBottom.setVisibility(View.GONE);
            btnDone1.setVisibility(View.GONE);
            draggingHeight = dpToPx(3);
        }
        if (isMultiSelect()) {
            if (selectedUriList.size() == 1
                && filesManager.getMediaType(selectedUriList.get(0).getInitialUri()) == MEDIA_TYPE_VIDEO) {
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
        }
        if (isMultiPicking()) {
            builder.onMultiMediaSelectedListener.onMediaUrisListChanges(selectedUriList);
            setupPeekHeight();
            setupSelectedMultipleMedia(selectedUriList);
            enableCameraItemPreview(calculateEnableCameraParameter());
        }

        if (selectedUriList.size() > 0 && builder.type == MediaControllerOpenPlace.Chat.INSTANCE && !isInputShown) {
            showBottomInput();
        } else if (selectedUriList.size() == 0 && builder.type == MediaControllerOpenPlace.Chat.INSTANCE)
            hideBottomInput();

        if (selectedUriList.size() > 0 && !isMultiPicking()) {
            doneBtn.setVisibility(View.VISIBLE);
        } else {
            doneBtn.setVisibility(View.GONE);
        }
    }

    private void setupSelectedMultipleMedia(List<MediaUriModel> mediaUriList) {
        int selectedGalleryMediaCount = mediaUriList.size();
        if (selectedGalleryMediaCount > 0) {
            tvSelectedImageCount.setText(getString(R.string.media_picker_select_media_count, selectedGalleryMediaCount));
            tvSelectedImageCount.setVisibility(View.VISIBLE);
            recentBtn.setVisibility(View.GONE);
        } else {
            recentBtn.setVisibility(View.VISIBLE);
            tvSelectedImageCount.setVisibility(View.GONE);
        }
    }


    private void addUri(final MediaUriModel media, Boolean editVideo) {
        if (isMaxMediaCountSelected()) {
            if (isMultiPicking() || isMultiSelect()) {
                showMaxCountWarningMessage();
                return;
            }
            String message;
            if (builder.selectMaxCountErrorText != null) {
                message = builder.selectMaxCountErrorText;
            } else {
                message = String.format(getResources().getString(R.string.select_max_count), builder.selectMaxCount);
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }

        checkUriList();

        if (filesManager.getMediaType(media.getActualUri()) == MEDIA_TYPE_VIDEO) {
            duration.requestVideoDuration(media.getActualUri(), timeMils -> {
                int duration = DURATION_VIDEO_MS;

                if (builder.videoMaxDuration != NOT_SET) {
                    duration = (builder.videoMaxDuration) * 1000;
                }

                if (timeMils > (duration + 1000)) {
                    if (isMultiPicking()) {
                        openEditor(media);
                    } else {
                        showDialogToLongVideo(media);
                    }
                } else {
                    selectedUriList.add(media);
                    checkUriList();
                    imageGalleryAdapter.setSelectedUriList(selectedUriList, media);
                    if (editVideo) imageGalleryAdapter.addEditedPickerTile(media);
                }
            });
        } else {
            selectedUriList.add(media);
            checkUriList();
            imageGalleryAdapter.setSelectedUriList(selectedUriList, media);
        }
    }

    private void showMaxCountWarningMessage() {
        NSnackbar.Builder snackBuilder = new NSnackbar.Builder(getActivity(), null);
        snackBuilder.typeText();
        snackBuilder.inView(requireView().getRootView());
        snackBuilder.setAnimationMode(ANIMATION_MODE_FADE);
        snackBuilder.setIcon(R.drawable.ic_outlined_warning_m);
        snackBuilder.marginBottom(MAX_MEDIA_SNACK_MARGIN_BOTTOM);
        snackBuilder.text(String.format(getResources().getString(R.string.may_only_pick_n_media),
            builder.selectMaxCount));
        snackBuilder.show();
    }

    private void removeImage(MediaUriModel media) {
        selectedUriList.remove(media);
        checkUriList();
        imageGalleryAdapter.unselectPickerTile(media);
        imageGalleryAdapter.setSelectedUriList(selectedUriList, media);
    }

    private void selectItem(MediaUriModel media) {
        selectedUriList.clear();
        selectedUriList.add(media);
        imageGalleryAdapter.selectItem(media);
    }

    private void unselectItem() {
        selectedUriList.clear();
        updateAlreadySelectedMedia(new ArrayList<>());
    }

    private void enableAll() {
        selectedUriList.clear();
        imageGalleryAdapter.unSelectAll();
    }

    private void startCameraIntent() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            switch (builder.mediaType) {
                case BaseBuilder.MediaType.IMAGE:
                case BaseBuilder.MediaType.IMAGE_AND_VIDEO:
                    if (getActivity() != null) {
                        if (!isMultiPicking()) {
                            if (builder.alreadySelectedMedia != null &&
                                !builder.alreadySelectedMedia.isEmpty() &&
                                builder.alreadySelectedMedia.get(0) != null
                                && ((MediaUriModel) builder.alreadySelectedMedia.get(0)).getEditedUri() != null) {
                                builder.onImageSelectedListener.onRequestMediaReset();
                                return;
                            }
                        }
                        ImageCaptureUtils.getImageFromCamera(getActivity(), new ImageCaptureUtils.Listener() {
                                @Override
                                public void onResult(@NonNull ImageCaptureResultModel imageCaptureResultModel) {
                                    cameraImageUri = MediaUriModel.Companion.initial(imageCaptureResultModel.getFileUri());
                                    addNewSelectedItemToAdapter(cameraImageUri);
                                    complete(cameraImageUri, null);
                                }

                                @Override
                                public void onFailed() {
                                    if (rcGallery != null && !isMultiPicking()) {
                                        rcGallery.postDelayed(() -> {
                                            updateAdapter();
                                        }, 200);
                                    }
                                }
                            }, builder.cameraLensFacing
                        );
                    }
                    break;
                case BaseBuilder.MediaType.VIDEO:
                default:
                    break;
            }
        } else {
            builder.permissionActionsListener.onCameraRequestPermissions(true);
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
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        if (builder.mediaType == BaseBuilder.MediaType.IMAGE) {
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        } else {
            galleryIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        }

        if (galleryIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Phone do not have Gallery Application");
            return;
        }

        TedOnActivityResult.with(getActivity())
            .setIntent(galleryIntent)
            .setListener((resultCode, data) -> {
                setRecentBtnClickable();
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

    private boolean isMultiPicking() {
        return builder.onMultiMediaSelectedListener != null;
    }

    private void addNewSelectedItemToAdapter(MediaUriModel uri) {
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

        MediaUriModel mediaModel = MediaUriModel.Companion.initial(selectedImageUri);

        if ((isMultiSelect() || isMultiPicking()) && !isMaxMediaCountSelected()) {
            boolean isSelected = !selectedUriList.contains(mediaModel);
            imageGalleryAdapter.setSelected(mediaModel, isSelected, 0);
        }

        complete(mediaModel, null);
    }

    private boolean isMaxMediaCountSelected() {
        return selectedUriList.size() == builder.selectMaxCount;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        builder.tedBottomSheetCallback.onSetStatusBar();
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
            if (isInputShown) {
                flBottomContainer.animate()
                    .translationY(0)
                    .setDuration(150)
                    .start();
            }

            setSuggestionsMenuExtraPeekHeight(flBottomContainer.getHeight(), true);
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
    private SuggestionsMenuContract suggestionsMenu = null;

    private void initSuggestionsMenu() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            tagListView = dialog.findViewById(R.id.tags_list);
            if (tagListView != null) {
                tagListView.post(() -> {
                    tagListView.setVisibility(View.VISIBLE);
                    suggestionsMenu = builder.suggestionsMenu;
                    tagListViewBehaviour = BottomSheetBehavior.from(tagListView);
                    tagListRecyclerView = tagListView.findViewById(R.id.recycler_tags);
                    if (etInput != null && tagListRecyclerView != null && tagListViewBehaviour != null) {
                        suggestionsMenu.init(tagListRecyclerView, etInput, tagListViewBehaviour);
                        suggestionsMenu.setSuggestedUniqueNameClicked(uiTagEntity -> {
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

    private void setSuggestionsMenuExtraPeekHeight(int extraPeekHeight, boolean isAnimate) {
        if (suggestionsMenu != null && flBottomContainer != null) {
            suggestionsMenu.setExtraPeekHeight(extraPeekHeight, true);
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

    private void replaceUniqueNameBySuggestion(SuggestionsMenuContract.UITagEntity userData) {
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

    private void showDialogToLongVideo(MediaUriModel media) {
        int duration = DURATION_VIDEO_MS / 1000;

        if (builder.videoMaxDuration != NOT_SET) {
            duration = builder.videoMaxDuration;
        }

        String maxDuration = NTimeKt.getDurationFromSeconds(requireContext(), duration);
        new ConfirmDialogBuilder()
            .setHeader(getString(R.string.warning_video_duration_title))
            .setDescription(getString(R.string.you_cant_send_video_more_duration, maxDuration))
            .setLeftBtnText(getString(R.string.cancel_caps))
            .setRightBtnText(getString(R.string.open_editor_caps))
            .setCancelable(false)
            .setLeftClickListener(() -> {
                if (!isMultiPicking()) {
                    enableAll();
                }
                return Unit.INSTANCE;
            })
            .setRightClickListener(() -> {
                openEditor(media);
                return Unit.INSTANCE;
            }).show(getChildFragmentManager());
    }

    private void openEditor(MediaUriModel media) {
        try {
            builder.mediaViewerPhotoEditorCallback.onOpenPhotoEditor(
                media.getActualUri(),
                builder.type,
                true,
                new MediaViewerPhotoEditorCallback.MediaViewerPhotoEditorResultCallback() {
                    @Override
                    public void onCanceled() {
                        if (!isMultiPicking()) enableAll();
                    }

                    @Override
                    public void onPhotoReady(@NonNull Uri resultUri) {

                    }

                    @Override
                    public void onVideoReady(@NonNull Uri resultUri) {
                        if (isMultiPicking()) {
                            onVideoReadyUri(MediaUriModel.Companion.edited(media.getInitialUri(), resultUri));
                        } else {
                            onVideoReadyUri(MediaUriModel.Companion.initial(resultUri));
                        }
                    }

                    @Override
                    public void onError() {
                        if (!isMultiPicking()) enableAll();
                    }
                }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onVideoReadyUri(MediaUriModel resultMedia) {
        if (!isMultiPicking()) {
            imageGalleryAdapter.unSelectAll();
            imageGalleryAdapter.addAndSelectVideo(
                new GalleryAdapter.PickerTile(resultMedia,
                    GalleryAdapter.PickerTile.VIDEO
                )
            );
        }

        addVideoToLaterDelete(resultMedia.getEditedUri());

        if (isMultiPicking()) {
            addUri(resultMedia, true);
        } else if (filesManager.getMediaType(resultMedia.getActualUri()) == MEDIA_TYPE_VIDEO) {
            duration.requestVideoDuration(resultMedia.getActualUri(), timeMils -> {
                int duration = DURATION_VIDEO_MS;

                if (builder.videoMaxDuration != NOT_SET) {
                    duration = builder.videoMaxDuration * 1000;
                }

                if (timeMils > (duration + 1000)) {
                    showDialogToLongVideo(resultMedia);
                }
            });
        }
    }

    private void addVideoToLaterDelete(Uri uri) {
        if (uri == null) return;
        builder.mediaViewerPhotoEditorCallback.onAddHashSetVideoToDelete(uri.getPath());
    }

    private void onReadyChat(String txt) {
        if (builder.onImageWithTextReady == null) return;
        builder.onImageWithTextReady.onImageWithText(
            selectedUriList.stream()
                .map(MediaUriModel::getInitialUri)
                .collect(Collectors.toList()), txt);
        dismissAllowingStateLoss();
    }

    public interface OnMultiMediaSelectedListener {
        void onMediaUrisListChanges(List<MediaUriModel> uriList);
    }

    public interface OnMultiImageSelectedListener {
        void onImagesSelected(List<Uri> uriList);
    }

    public interface OnImageSelectedListener {
        void onImageSelected(Uri uri);

        void onImageUnselected();

        void onRequestMediaReset();
    }

    public interface OnDialogDismissListener {
        void onDialogDismiss();
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
        public static int NOT_SET = -1;
        public int videoMaxDuration = NOT_SET;
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
        public boolean showGifs = true;
        public OnImageEditListener onImageEditListener;
        public Uri selectedUri;
        public MediaControllerOpenPlace type = MediaControllerOpenPlace.Common.INSTANCE;
        public MediaViewerCameraTypeEnum cameraType = MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_BACK;
        public PermissionState permissionState = PermissionState.GRANTED;
        public TedBottomSheetPermissionActionsListener permissionActionsListener;
        public MediaViewerPreviewModeParams previewModeParams = new MediaViewerPreviewModeParams();
        public List<MediaUriModel> alreadySelectedMedia = new ArrayList<>();
        public IOnImageWithTextReady onImageWithTextReady;
        protected FragmentActivity fragmentActivity;
        OnImageSelectedListener onImageSelectedListener;
        OnMultiImageSelectedListener onMultiImageSelectedListener;
        OnMultiMediaSelectedListener onMultiMediaSelectedListener;
        OnErrorListener onErrorListener;
        OnDialogDismissListener onDialogDismissListener;
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
        private TedBottomSheetCallback tedBottomSheetCallback;
        private Boolean isSuggestionMenuActive = false;
        private FragmentManager supportFragmentManager;
        private SuggestionsMenuContract suggestionsMenu;
        private MediaViewerPhotoEditorCallback mediaViewerPhotoEditorCallback;
        private MediaViewerViewCallback mediaViewerViewCallback;
        private @CameraLensFacing int cameraLensFacing;

        public BaseBuilder(@NonNull FragmentActivity fragmentActivity) {

            this.fragmentActivity = fragmentActivity;
            setCameraTile(R.drawable.ic_outlined_cam_m);
            setGalleryTile(R.drawable.ic_gallery_new);
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

        public T setPermissionState(PermissionState permissionState) {
            this.permissionState = permissionState;
            return (T) this;
        }

        public T setPermissionsActionsListener(TedBottomSheetPermissionActionsListener permissionActionsListener) {
            this.permissionActionsListener = permissionActionsListener;
            return (T) this;
        }

        public T setPreviewModeParams(MediaViewerPreviewModeParams previewModeParams) {
            this.previewModeParams = previewModeParams;
            return (T) this;
        }

        public T setAlreadySelectedMedia(List<MediaUriModel> mediaViewerEditedAttachmentInfo) {
            if (mediaViewerEditedAttachmentInfo != null) {
                this.alreadySelectedMedia = mediaViewerEditedAttachmentInfo;
            }
            return (T) this;
        }

        public T setOnImageReadyWithText(IOnImageWithTextReady onImageReadyWithText) {
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

        public T setVideoMaxDuration(Integer videoMaxDuration) {
            if (videoMaxDuration != null) {
                this.videoMaxDuration = videoMaxDuration;
            }
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

        public T setPeekHeight(Integer peekHeight) {
            if (peekHeight != null) this.peekHeight = peekHeight;
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

        public T showGifs(boolean show) {
            this.showGifs = show;
            return (T) this;
        }

        public T setSupportFragmentManager(FragmentManager supportFragmentManager) {
            this.supportFragmentManager = supportFragmentManager;
            return (T) this;
        }

        public T setSuggestionMenuActive(Boolean isActive) {
            this.isSuggestionMenuActive = isActive;
            return (T) this;
        }

        public T setSuggestionMenu(SuggestionsMenuContract suggestionsMenu) {
            this.suggestionsMenu = suggestionsMenu;
            return (T) this;
        }

        public T setMediaViewerPhotoEditorCallback(MediaViewerPhotoEditorCallback mediaViewerPhotoEditorCallback) {
            this.mediaViewerPhotoEditorCallback = mediaViewerPhotoEditorCallback;
            return (T) this;
        }

        public T setMediaViewerViewCallback(MediaViewerViewCallback mediaViewerViewCallback) {
            this.mediaViewerViewCallback = mediaViewerViewCallback;
            return (T) this;
        }

        public T setTedBottomSheetCallback(TedBottomSheetCallback tedBottomSheetCallback) {
            this.tedBottomSheetCallback = tedBottomSheetCallback;
            return (T) this;
        }

        public T setCameraLensFacing(@CameraLensFacing int cameraLensFacing) {
            this.cameraLensFacing = cameraLensFacing;
            return (T) this;
        }

        public TedBottomSheetDialogFragment create() {
            if (onImageSelectedListener == null
                && onMultiImageSelectedListener == null
                && onMultiMediaSelectedListener == null) {
                throw new RuntimeException("You have to use setOnImageSelectedListener() or setOnMultiImageSelectedListener()" +
                    "or setOnMultiMediaSelectedListener() for receive selected Uri");
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

    public interface IOnDurationReadyInHolder {
        void onResult(Long duration, GalleryAdapter.GalleryViewHolder holder);
    }
}
