package com.meera.core.utils.tedbottompicker.adapter;

import static com.meera.core.utils.CommonUtilsKt.IS_APP_REDESIGNED;
import static com.meera.core.utils.files.FileUtilsImpl.MEDIA_TYPE_VIDEO;
import static com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment.BaseBuilder.NOT_SET;
import static com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment.DEFAULT_CHAT_VIDEO_MAX_DURATION;
import static com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment.DEFAULT_VIDEO_MAX_DURATION;
import static java.util.Collections.emptyList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.meera.core.R;
import com.meera.core.extensions.ViewKt;
import com.meera.core.utils.CommonUtilsKt;
import com.meera.core.utils.camera.CameraLensFacing;
import com.meera.core.utils.camera.CameraPreviewHelper;
import com.meera.core.utils.files.FileManager;
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment;
import com.meera.core.utils.tedbottompicker.models.MediaUriModel;
import com.meera.core.utils.tedbottompicker.view.TedSquareImageView;
import com.meera.media_controller_common.MediaControllerOpenPlace;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    public ArrayList<PickerTile> pickerTiles;
    private Context context;
    private FileManager fileManager;
    private TedBottomSheetDialogFragment.BaseBuilder builder;
    private OnItemClickListener onItemClickListener;
    private List<MediaUriModel> selectedUriList = new ArrayList<>();
    public ArrayList<PickerTile> selectedPickerTiles;
    public boolean isMultiModeEnabled = false;
    public boolean isPreviewModeEnabled = false;
    public boolean isMultiPickingEnabled = false;
    private OnOpenImagePreview onOpenImagePreview;
    private final CameraPreviewHelper cameraPreviewHelper;
    private final LifecycleOwner lifecycleOwner;
    public String bucketId;
    private int SELECTED_VIEW_MARGIN = 2;
    private int SELECTED_VIEW_MARGIN_MULTI_MODE = 9;
    private int CAMERA_PREVIEW_DELAY = 1000;
    @CameraLensFacing private int cameraLensFacing;

    private boolean isCameraPreviewEnabled = true;
    private boolean isSelectionBlocked = false;

    private TedBottomSheetDialogFragment.IVideoDurationRequest duration;

    private int selectedCount = 0;
    private int selectMaxCount = 0;

    public GalleryAdapter(Context context,
                          TedBottomSheetDialogFragment.BaseBuilder builder,
                          CameraPreviewHelper cameraPreviewHelper,
                          LifecycleOwner lifecycleOwner,
                          FileManager fileManager,
                          List<MediaUriModel> alreadySelectedMedia,
                          @CameraLensFacing int cameraLensFacing,
                          int selectMaxCount) {

        this.context = context;
        this.builder = builder;
        this.cameraPreviewHelper = cameraPreviewHelper;
        this.lifecycleOwner = lifecycleOwner;
        this.fileManager = fileManager;
        List<MediaUriModel> existAlreadySelectedMedia = alreadySelectedMedia != null ? alreadySelectedMedia : emptyList();
        this.selectedUriList.addAll(existAlreadySelectedMedia);
        this.cameraLensFacing = cameraLensFacing;
        this.selectMaxCount = selectMaxCount;
        loadTiles();
    }

    public void setSelectedUriList(List<MediaUriModel> alreadySelectedMedia) {
        this.selectedUriList.clear();
        List<MediaUriModel> existAlreadySelectedMedia = alreadySelectedMedia != null ? alreadySelectedMedia : emptyList();
        this.selectedUriList.addAll(existAlreadySelectedMedia);
        loadTiles();
    }

    public void unSelectAll() {
        selectedPickerTiles.clear();
        selectedUriList.clear();
        for (PickerTile i : pickerTiles) {
            i.isSelected = false;
            i.needWhiteBg = false;
        }
        notifyDataSetChanged();
    }

    public void updateCounters(int cnt) {
        for (PickerTile i : selectedPickerTiles) {
            if (i.cnt > cnt)
                i.cnt--;
        }
    }

    public void setSelectedUriList(List<MediaUriModel> selectedUriList, @NonNull MediaUriModel media) {
        Log.d("TedGalleryAdapter", "Picker: selectedUri = " + selectedUriList + "\n selectedPickerTiles = " + selectedPickerTiles);

        HashSet<MediaUriModel> selectedSet = new HashSet<>();
        selectedSet.addAll(selectedUriList);
        selectedSet.addAll(this.selectedUriList);
        selectedSet.add(media);
        boolean isNeedToUpdateAll = false;
        if (selectedSet.size() == selectMaxCount)
            isNeedToUpdateAll = true;

        this.selectedUriList = selectedUriList;
        Log.d("TedGalleryAdapter", "setSelectedUriList called");
        if (isNeedToUpdateAll)
            notifyDataSetChanged();
        else {
            for (MediaUriModel i : selectedSet) {
                Integer pos = getPicketTilePositionForUri(i);
                if (pos != null) {
                    notifyItemChanged(pos);
                }
            }
            disableVideoContent();
        }
        if (selectedUriList.isEmpty()) {
            enableVideoContent();
        }
    }

    public void selectItem(MediaUriModel uri) {
        selectedUriList.clear();
        selectedUriList.add(uri);
        notifyDataSetChanged();
    }

    public void loadTiles() {
        pickerTiles = new ArrayList<>();
        selectedPickerTiles = new ArrayList<>();

        if (builder.showCamera) {
            pickerTiles.add(new PickerTile(PickerTile.CAMERA));
        }

        if (builder.showGallery) {
            pickerTiles.add(new PickerTile(PickerTile.GALLERY));
        }

        Cursor cursor = null;
        try {
            String[] columns;
            String orderBy;
            String selection = null;
            String[] selectionArgs = null;
            Uri uri;
            if (builder.mediaType == TedBottomSheetDialogFragment.BaseBuilder.MediaType.IMAGE) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_ID
                };
                orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
                if (!builder.showGifs) {
                    selection = MediaStore.Files.FileColumns.MIME_TYPE + "!= ?";
                    selectionArgs = new String[]{"image/gif"};
                }
            } else if (builder.mediaType == TedBottomSheetDialogFragment.BaseBuilder.MediaType.VIDEO) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{
                    MediaStore.Video.VideoColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_ID
                };
                orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC";
            } else {
                uri = MediaStore.Files.getContentUri("external");
                orderBy = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
                columns = new String[]{
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_ID
                };
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                if (!builder.showGifs) {
                    selection = selection + MediaStore.Files.FileColumns.MIME_TYPE + "!= ?";
                    selectionArgs = new String[]{"image/gif"};
                }
            }

            cursor = context.getApplicationContext()
                .getContentResolver()
                .query(uri, columns, selection, selectionArgs, orderBy);
            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext() && count < builder.previewMaxCount) {
                    String dataIndex;
                    if (builder.mediaType == TedBottomSheetDialogFragment.BaseBuilder.MediaType.IMAGE) {
                        dataIndex = MediaStore.Images.Media.DATA;
                    } else {
                        dataIndex = MediaStore.Video.VideoColumns.DATA;
                    }
                    String bucketIdIndex = MediaStore.Images.ImageColumns.BUCKET_ID;

                    @SuppressLint("Range") String imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex));
                    @SuppressLint("Range") String bucketId = cursor.getString(cursor.getColumnIndex(bucketIdIndex));
                    int type;
                    //long durations;
                    if (fileManager.getMediaType(Uri.parse(imageLocation)) == MEDIA_TYPE_VIDEO) {
                        type = PickerTile.VIDEO;
                    } else {
                        type = PickerTile.IMAGE;
                    }
                    File imageFile = new File(imageLocation);
                    Uri currentUri = Uri.fromFile(imageFile);
                    PickerTile pt = getTileForAlreadySelectedMedia(currentUri, type);
                    if (this.bucketId == null) {
                        pickerTiles.add(pt);
                    } else if (this.bucketId.equals(bucketId)) {
                        pickerTiles.add(pt);
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        notifyDataSetChanged();
    }

    public void enableCameraPreview(boolean enable) {
        if (isCameraPreviewEnabled == enable) return;

        isCameraPreviewEnabled = enable;
        if (!pickerTiles.isEmpty() && getItemCount() != 0) {
            notifyItemChanged(0);
        }
    }

    private PickerTile getTileForAlreadySelectedMedia(Uri uri, int tileType) {
        if (!selectedUriList.isEmpty()) {
            for (int i = 0; i < selectedUriList.size(); i++) {
                MediaUriModel media = selectedUriList.get(i);
                if (media.getInitialUri().getPath().equals(uri.getPath())) {
                    PickerTile pt = new PickerTile(media, tileType);
                    pt.isSelected = true;
                    selectedPickerTiles.add(pt);
                    pt.cnt = i + 1;
                    return pt;
                }
            }
        }

        return new PickerTile(MediaUriModel.Companion.initial(uri), tileType);
    }

    private void disableVideoContent() {
        for (int i = 1; i < pickerTiles.size(); i++) {
            if (pickerTiles.get(i).tileType == PickerTile.VIDEO) {
                pickerTiles.get(i).needWhiteBg = true;
                pickerTiles.get(i).isSelected = false;
                notifyItemChanged(i);
            }
        }
    }

    private void enableVideoContent() {
        for (int i = 1; i < pickerTiles.size(); i++) {
            if (pickerTiles.get(i).tileType == PickerTile.VIDEO) {
                pickerTiles.get(i).needWhiteBg = false;
                pickerTiles.get(i).isSelected = false;
                notifyItemChanged(i);
            }
        }
    }


    private Integer getPicketTilePositionForUri(MediaUriModel media) {
        try {
            for (int i = 1; i < pickerTiles.size(); i++) {
                if (pickerTiles.get(i).mediaUri.getInitialUri().getPath().equals(media.getInitialUri().getPath())) {
                    return i;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public void onViewRecycled(@NonNull GalleryViewHolder holder) {
        ViewKt.glideClear(holder.ivThumbnail);
        super.onViewRecycled(holder);
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
            View.inflate(context, R.layout.meera_ted_picker_grid_item, null);

        return new GalleryViewHolder(view, cameraPreviewHelper, lifecycleOwner);
    }


    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, int position) {
        PickerTile pickerTile = getItem(position);
        boolean isSelected = false;

        holder.cancelVideoDurationDisposable();

        if (pickerTile.isCameraTile()) {
            holder.ivThumbnail.setImageResource(android.R.color.transparent);
            ViewKt.loadGlide(holder.ivThumbnail, builder.cameraTileDrawable);
            holder.ivThumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
            ViewKt.setTintColor(builder.cameraTileDrawable, context, R.color.ui_white);

            //holder.ivThumbnail.setImageDrawable(builder.cameraTileDrawable);
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            holder.tvSelected.setVisibility(View.GONE);
            holder.ivSelected.setVisibility(View.GONE);
            holder.previewCamera.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
            holder.previewCamera.setVisibility(View.GONE);
            holder.vCameraBackground.setVisibility(View.VISIBLE);
            if (ActivityCompat.checkSelfPermission(holder.itemView.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (isCameraPreviewEnabled) {
                    holder.itemView.postDelayed(() -> {
                            cameraPreviewHelper.attachPreview(lifecycleOwner, holder.previewCamera, cameraLensFacing);
                            holder.previewCamera.setVisibility(View.VISIBLE);
                            holder.vCameraBackground.setVisibility(View.GONE);
                        }, CAMERA_PREVIEW_DELAY
                    );
                }
            }
        } else if (pickerTile.isGalleryTile()) {
            holder.ivThumbnail.setBackgroundResource(builder.galleryTileBackgroundResId);
            holder.ivThumbnail.setImageDrawable(builder.galleryTileDrawable);
            holder.tvSelected.setVisibility(View.GONE);
            holder.ivSelected.setVisibility(View.GONE);
        } else {

            holder.previewCamera.setVisibility(View.GONE);

            holder.ivThumbnail.setBackgroundResource(0);
            if (isMultiModeEnabled || isPreviewModeEnabled) {
                holder.tvSelected.setVisibility(View.VISIBLE);
            } else holder.tvSelected.setVisibility(View.GONE);
            if (!isPreviewModeEnabled && !IS_APP_REDESIGNED) updatePadding(pickerTile, holder);
            MediaUriModel media = pickerTile.getImageUriModel();
            boolean isNeedToLoadImage = media.getActualUri() != holder.uri;
            holder.uri = media.getActualUri();
            if (builder.imageProvider == null) {
                if (isNeedToLoadImage) {
                    loadImage(holder.ivThubnailWithPadding, media.getActualUri()); //костыль так как с эффектом продавлливания были проблемы (что только не пробовал)
                    loadImage(holder.ivThumbnail, media.getActualUri());
                }
            } else {
                builder.imageProvider.onProvideImage(holder.ivThumbnail, media.getActualUri());
            }
            isSelected = isSelectedContainsMedia(media);
            int selectedBackground = 0;
            int whiteBgVisibility = View.GONE;
            int ivSelectedVisibility = View.VISIBLE;
            if (isSelected) {
                selectedBackground = R.drawable.meera_circle_tab_bg;
                String selectedText = "";
                if (isMultiModeEnabled || isMultiPickingEnabled) {
                    selectedText = String.valueOf(pickerTile.cnt);
                    ivSelectedVisibility = View.GONE;

                    if(isMultiPickingEnabled){
                        selectedBackground = R.drawable.circle_tab_white_stroke_bg;
                    }
                }
                if (isPreviewModeEnabled || isMultiPickingEnabled) {
                    whiteBgVisibility = View.GONE;
                }
                holder.tvSelected.setText(selectedText);
            } else {
                if (isMultiModeEnabled || isMultiPickingEnabled) {
                    selectedBackground = selectedPickerTiles.size() < selectMaxCount
                        ? R.drawable.bg_shadow_ring
                        : R.drawable.gray_ring_bg;
                }
                holder.tvSelected.setText("");
                ivSelectedVisibility = View.GONE;
            }
            holder.tvSelected.setBackgroundResource(selectedBackground);
            holder.ivSelected.setVisibility(ivSelectedVisibility);

            int margin = SELECTED_VIEW_MARGIN;
            if (isMultiModeEnabled || isMultiPickingEnabled) {
                margin = SELECTED_VIEW_MARGIN_MULTI_MODE;
            }
            margin = (int) CommonUtilsKt.convertDpToPx(context, (float) margin);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.tvSelected.getLayoutParams();
            lp.setMargins(margin, margin, margin, margin);
            holder.tvSelected.setLayoutParams(lp);
        }
        if (!isPreviewModeEnabled) {
            holder.vWhiteBg.setOnClickListener(v -> {
            });
        }

        // init video duration
        if (pickerTile.tileType == PickerTile.VIDEO) {
            if (duration != null) {
                Uri videoUri;
                if (isMultiPickingEnabled) {
                    videoUri = pickerTile.mediaUri.getActualUri();
                } else {
                    videoUri = pickerTile.mediaUri.getInitialUri();
                }

                holder.getVideoDurationDisposable = getVideoDuration(videoUri, holder, (duration, actualHolder) -> {
                    long secondsDuration = duration / 1000;
                    long minutesDuration = duration / 1000 / 60;
                    String minutes = String.valueOf(minutesDuration);
                    if (minutes.length() == 1) minutes = "0" + minutes;
                    String seconds = String.valueOf(((duration % (1000 * 60)) / 1000));
                    if (seconds.length() == 1) seconds = "0" + seconds;

                    actualHolder.tvVideoTime.setText(String.format("%s:%s", minutes, seconds));
                    actualHolder.tvVideoTimeWithPadding.setText(String.format("%s:%s", minutes, seconds));
                    if (isPreviewModeEnabled) {
                        actualHolder.flVideoContainer.setVisibility(View.VISIBLE);
                        actualHolder.flVideoContainerWithPadding.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            holder.flVideoContainer.setVisibility(View.GONE);
            holder.flVideoContainerWithPadding.setVisibility(View.GONE);
        }


        initClickListeners(holder, pickerTile);
    }

    //TODO https://nomera.atlassian.net/browse/BR-25291
    private Disposable getVideoDuration(Uri uri, GalleryViewHolder holder, TedBottomSheetDialogFragment.IOnDurationReadyInHolder listener) {
        return Observable.fromCallable(() -> fileManager.getVideoDurationMils(uri))
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> listener.onResult(aLong, holder));
    }

    private void initClickListeners(GalleryViewHolder holder, PickerTile pickerTile) {
        if (onItemClickListener != null) {
            View v;
            if ((isMultiModeEnabled || isMultiPickingEnabled) && pickerTile.tileType != PickerTile.CAMERA) {
                v = holder.selectedContainer;
                holder.ivThumbnail.setOnClickListener(view -> { // open preview in multi selected mode
                    if (onOpenImagePreview != null) {
                        onOpenImagePreview.onOpenPreview(holder.itemView, holder.getAbsoluteAdapterPosition());
                    }
                });
                holder.ivThubnailWithPadding.setOnClickListener(view -> {
                    if (onOpenImagePreview != null) {
                        onOpenImagePreview.onOpenPreview(holder.itemView, holder.getAbsoluteAdapterPosition());
                    }
                });
            } else {
                v = holder.ivThumbnail;
            }
            v.setOnClickListener(view -> {
                if (isSelectionBlocked) return;

                if (!(isMultiModeEnabled || isMultiPickingEnabled) && isPreviewModeEnabled && selectedUriList != null
                    && !selectedUriList.isEmpty()) {
                    onItemClickListener.onItemClick(holder.itemView, holder.getAbsoluteAdapterPosition());
                    return;
                }

                if (isMultiPickingEnabled
                    && pickerTile.mediaUri != null
                    && pickerTile.mediaUri.isEdited()) {
                    onItemClickListener.onItemClick(holder.itemView, holder.getAbsoluteAdapterPosition());
                    return;
                }

                if (isPreviewModeEnabled && pickerTile.tileType == PickerTile.VIDEO) {
                    isSelectionBlocked = true;
                    checkVideoDurationBeforeClick(v, holder, pickerTile);
                    return;
                }

                onItemClick(v, holder, pickerTile);
            });
        }
    }

    private void checkVideoDurationBeforeClick(View v, GalleryViewHolder holder, PickerTile pickerTile) {
        if (duration != null) {
            Uri videoUri;
            if (isMultiPickingEnabled) {
                videoUri = pickerTile.mediaUri.getActualUri();
            } else {
                videoUri = pickerTile.mediaUri.getInitialUri();
            }

            duration.requestVideoDuration(videoUri, duration -> {
                long secondsDuration = duration / 1000;
                int maxVideoDuration = getVideoMaxDuration();
                if (secondsDuration > maxVideoDuration) {
                    onItemClickListener.onItemClick(holder.itemView, holder.getAbsoluteAdapterPosition());
                } else {
                    onItemClick(v, holder, pickerTile);
                }
                isSelectionBlocked = false;
            });
        }
    }

    private void onItemClick(View v, GalleryViewHolder holder, PickerTile pickerTile) {
        if (v == holder.selectedContainer || isPreviewModeEnabled)
            pickerTile.isSelected = !pickerTile.isSelected;
        MediaUriModel media = pickerTile.getImageUriModel();
        if (isSelectedContainsMedia(media)) {
            media.clearEditedUri();
            selectedPickerTiles.remove(pickerTile);
            updateCounters(pickerTile.cnt);
            pickerTile.cnt = 0;
        } else {
            if (selectedPickerTiles.size() < selectMaxCount && pickerTile.tileType != PickerTile.CAMERA) {
                selectedPickerTiles.add(pickerTile);
                pickerTile.cnt = selectedUriList.size() + 1;
            }
        }
        updateWhiteBg();
        onItemClickListener.onItemClick(holder.itemView, holder.getAbsoluteAdapterPosition());
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

    private void loadImage(TedSquareImageView ivThumbnail, Uri uri) {
        Glide.with(context)
            .load(uri)
            .apply(new RequestOptions().centerCrop()
                .placeholder(R.drawable.ic_gallery_new)
                .error(R.drawable.alert_error))
            .into(ivThumbnail);
    }

    private void updatePadding(PickerTile pickerTile, GalleryViewHolder holder) {
        if (pickerTile.isSelected) {
            //Timber.d("Bazaleev holder pos = " + holder.getAdapterPosition() + " adress = " + holder.clVideoContainer);
            holder.ivThumbnail.setVisibility(View.GONE);
            holder.ivThubnailWithPadding.setVisibility(View.VISIBLE);
            holder.flVideoContainer.setVisibility(View.GONE);
            holder.flVideoContainerWithPadding.setVisibility(View.VISIBLE);
        } else {
            //Timber.d("Bazaleev holder pos = " + holder.getAdapterPosition() + " adress = " + holder.clVideoContainer);
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            holder.ivThubnailWithPadding.setVisibility(View.GONE);
            holder.flVideoContainer.setVisibility(View.VISIBLE);
            holder.flVideoContainerWithPadding.setVisibility(View.GONE);
        }
        if (pickerTile.tileType != PickerTile.VIDEO) {
            holder.flVideoContainer.setVisibility(View.GONE);
            holder.flVideoContainerWithPadding.setVisibility(View.GONE);
        }
    }

    private Boolean isSelectedContainsMedia(MediaUriModel media) {
        if (media == null) return false;
        for (MediaUriModel item : selectedUriList) {
            if (media.getInitialUri().equals(item.getInitialUri())) {
                return true;
            }
        }
        return false;
    }

    public void updateWhiteBg() {
        if (isPreviewModeEnabled) return;

        boolean isContainVideo = false;
        for (PickerTile i : selectedPickerTiles) {
            if (i.tileType == PickerTile.VIDEO) {
                isContainVideo = true;
                break;
            }
        }

        if (isContainVideo) {
            for (PickerTile i : pickerTiles) {
                i.needWhiteBg = !selectedPickerTiles.contains(i) || i.tileType != PickerTile.VIDEO;
            }
        } else {
            boolean isNeed = selectedPickerTiles.size() == selectMaxCount;
            for (PickerTile i : pickerTiles) {
                if (!selectedPickerTiles.contains(i)) {
                    if (selectedPickerTiles.size() >= 1
                        && selectedPickerTiles.get(0).tileType == PickerTile.IMAGE
                        && i.tileType == PickerTile.VIDEO
                    ) {
                        i.needWhiteBg = true;
                    } else {
                        i.needWhiteBg = isNeed;
                    }
                }
            }
        }
    }

    public PickerTile getItem(int position) {
        return pickerTiles.get(position);
    }

    public List<PickerTile> getCollection() {
        return this.pickerTiles;
    }

    @Override
    public int getItemCount() {
        return pickerTiles.size();
    }

    public void setOnItemClickListener(
        OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnOpenImagePreview(OnOpenImagePreview callback) {
        this.onOpenImagePreview = callback;
    }

    public void addItemSelected(MediaUriModel media) {
        PickerTile tile = new PickerTile(media);
        selectedPickerTiles.add(tile);
        tile.isSelected = true;
        tile.cnt = selectedPickerTiles.size();
        pickerTiles.add(1, tile);
        notifyDataSetChanged();
        updateWhiteBg();
    }

    public void addEditedPickerTile(MediaUriModel media) {
        PickerTile tile = findAndReplacePickerTile(media);
        if (tile == null) return;
        selectedPickerTiles.add(tile);
        tile.isSelected = true;
        tile.cnt = selectedPickerTiles.size();
        notifyDataSetChanged();
    }

    public void unselectPickerTile(MediaUriModel media) {
        for(PickerTile tile : selectedPickerTiles) {
            if (tile.mediaUri.getInitialUri() == media.getInitialUri()) {
                media.clearEditedUri();
                selectedPickerTiles.remove(tile);
                updateCounters(tile.cnt);
                tile.cnt = 0;
                break;
            }
        }
    }

    private PickerTile findAndReplacePickerTile(MediaUriModel media) {
        for (int i = 0; i < pickerTiles.size() - 1; i++) {
            PickerTile tile = pickerTiles.get(i);
            if (tile.getImageUriModel() != null)
                if (tile.getImageUriModel().getInitialUri() == media.getInitialUri()) {
                    PickerTile editedTile = new PickerTile(media, tile.tileType);
                    editedTile.cnt = tile.cnt;
                    pickerTiles.remove(i);
                    pickerTiles.add(i, editedTile);
                    return editedTile;
                }
        }
        return null;
    }

    public void addItemNotSelected(MediaUriModel media, int type) {
        PickerTile tile = new PickerTile(media);
        //selectedPickerTiles.add(tile);
        tile.cnt = 0;
        pickerTiles.add(1, tile);
        updateWhiteBg();
        notifyDataSetChanged();
    }

    public void setSelected(MediaUriModel selectedMedia, boolean isSelected, Integer preselected) {
        if (selectedMedia == null) return;
        int position = 0;
        int unselectedTileCnt = 0;
        for (int i = 0; i < pickerTiles.size() - 1; i++) {
            PickerTile tile = pickerTiles.get(i);
            if (tile.mediaUri != null && tile.mediaUri.getInitialUri().getPath().equals(selectedMedia.getInitialUri().getPath())) {
                position = i;
                if (isSelected) {
                    selectedPickerTiles.add(tile);
                    tile.cnt = selectedPickerTiles.size() + preselected;
                    tile.isSelected = true;
                } else {
                    tile.isSelected = false;
                    selectedMedia.clearEditedUri();
                    selectedPickerTiles.remove(tile);
                    unselectedTileCnt = tile.cnt;
                    updateCounters(tile.cnt);
                    tile.cnt = 0;
                }
                break;
            }
        }
        updateWhiteBg();
        if (isMultiPickingEnabled) {
            notifySelectedTiles(position, isSelected, unselectedTileCnt);
        } else {
            notifyDataSetChanged();
        }
    }

    private void notifySelectedTiles(Integer position, boolean isSelected, int unselectedTileCnt) {
        notifyItemChanged(position);
        if (!isSelected) {
            updateSelectedItemsUnderCnt(unselectedTileCnt);
        }
    }

    private void updateSelectedItemsUnderCnt(int unselectedTileCnt) {
        for (int i = 0; i < pickerTiles.size() - 1; i++) {
            PickerTile tile = pickerTiles.get(i);
            if (tile.isSelected && tile.cnt >= unselectedTileCnt) {
                notifyItemChanged(i);
            }
        }
    }

    private ArrayList<Integer> getSelectedTilesPositionsList() {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < pickerTiles.size() - 1; i++) {
            PickerTile tile = pickerTiles.get(i);
            if (tile.isSelected) {
                positions.add(i);
            }
        }
        return positions;
    }

    public void addAndSelectVideo(PickerTile tile) {
        pickerTiles.add(1, tile);
        selectedUriList.add(tile.getImageUriModel());
        selectedPickerTiles.add(tile);
        tile.cnt = selectedUriList.size();
        tile.isSelected = true;
        tile.needWhiteBg = false;
        updateWhiteBg();
        notifyDataSetChanged();
    }

    public void setDurationListener(TedBottomSheetDialogFragment.IVideoDurationRequest duration) {
        this.duration = duration;
    }

    public void reloadCameraTile() {
        if (builder.showCamera) {
            notifyItemChanged(0);
        }
    }

    public void addItemInSelectedList(MediaUriModel model) {
        if (!selectedUriList.contains(model)) {
            selectedUriList.add(model);
        }
    }

    public void removeItemFromSelectedList(MediaUriModel model) {
        selectedUriList.remove(model);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnOpenImagePreview {
        void onOpenPreview(View v, int position);
    }


    public static class PickerTile {

        public static final int IMAGE = 1;
        public static final int CAMERA = 2;
        public static final int GALLERY = 3;
        public static final int VIDEO = 4;
        private final MediaUriModel mediaUri;
        private int cnt = 0;
        private boolean needWhiteBg = false;
        public boolean isSelected = false;
        public long duration = 0;
        private final
        @TileType
        int tileType;

        private PickerTile(@SpecialTileType int tileType) {
            this(null, tileType);
        }

        public PickerTile(@Nullable MediaUriModel imageUri, @TileType int tileType) {
            this.mediaUri = imageUri;
            this.tileType = tileType;
        }

        public int getCnt() {
            return cnt;
        }

        public void setCnt(int cnt) {
            this.cnt = cnt;
        }

        PickerTile(@NonNull MediaUriModel imageUri) {
            this(imageUri, IMAGE);
        }

        @Nullable
        public MediaUriModel getImageUriModel() {
            return mediaUri;
        }

        @TileType
        public int getTileType() {
            return tileType;
        }

        @Override
        public String toString() {
            if (isImageTile()) {
                return "ImageTile: " + mediaUri;
            } else if (isVideoTile()) {
                return "VideoTile: " + mediaUri;
            }else if (isCameraTile()) {
                return "CameraTile";
            }  else if (isGalleryTile()) {
                return "PickerTile";
            } else {
                return "Invalid item";
            }
        }

        private boolean isImageTile() {
            return tileType == IMAGE;
        }

        private boolean isVideoTile() {
            return tileType == VIDEO;
        }

        public boolean isCameraTile() {
            return tileType == CAMERA;
        }

        private boolean isGalleryTile() {
            return tileType == GALLERY;
        }

        @IntDef({IMAGE, CAMERA, GALLERY, VIDEO})
        @Retention(RetentionPolicy.SOURCE)
        private @interface TileType {
        }

        @IntDef({CAMERA, GALLERY})
        @Retention(RetentionPolicy.SOURCE)
        private @interface SpecialTileType {
        }
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder {

        TedSquareImageView ivThumbnail;
        View vCameraBackground;
        PreviewView previewCamera;
        TedSquareImageView ivThubnailWithPadding;
        TextView tvSelected;
        View ivSelected;
        FrameLayout selectedContainer;
        FrameLayout flVideoContainer;
        TextView tvVideoTime;
        TextView tvVideoTimeWithPadding;
        FrameLayout flVideoContainerWithPadding;
        View vWhiteBg;
        Uri uri;
        FrameLayout flImageContainer;
        Handler handler;
        CameraPreviewHelper cameraPreviewHelper;
        LifecycleOwner lifecycleOwner;
        Disposable getVideoDurationDisposable;

        public GalleryViewHolder(View view, CameraPreviewHelper cameraPreviewHelper, LifecycleOwner lifecycleOwner) {
            super(view);
            ivThumbnail = view.findViewById(R.id.iv_thumbnail);
            vCameraBackground = view.findViewById(R.id.v_camera_background);
            previewCamera = view.findViewById(R.id.preview_camera);
            tvSelected = view.findViewById(R.id.tv_selected);
            ivSelected = view.findViewById(R.id.iv_selected);
            vWhiteBg = view.findViewById(R.id.v_white_bg);
            selectedContainer = view.findViewById(R.id.fl_container_selected);
            flImageContainer = view.findViewById(R.id.fl_img_container);
            ivThubnailWithPadding = itemView.findViewById(R.id.iv_thumbnail_with_padding);
            flVideoContainer = itemView.findViewById(R.id.fl_video);
            flVideoContainerWithPadding = itemView.findViewById(R.id.fl_video_selected);
            tvVideoTime = itemView.findViewById(R.id.tv_video_time);
            tvVideoTimeWithPadding = itemView.findViewById(R.id.tv_video_time_selected);
            this.cameraPreviewHelper = cameraPreviewHelper;
            this.lifecycleOwner = lifecycleOwner;
        }

        public void cancelVideoDurationDisposable() {
            if (getVideoDurationDisposable != null && !getVideoDurationDisposable.isDisposed()) {
                getVideoDurationDisposable.dispose();
                getVideoDurationDisposable = null;
            }
        }

        public void itemChecked() {
            selectedContainer.performClick();
        }

        public void setCameraPreviewEnabled(boolean enabled) {
            if (enabled) {
                previewCamera.setVisibility(View.VISIBLE);
                vCameraBackground.setVisibility(View.GONE);
            } else {
                previewCamera.setVisibility(View.GONE);
                vCameraBackground.setVisibility(View.VISIBLE);
            }
        }
    }
}
