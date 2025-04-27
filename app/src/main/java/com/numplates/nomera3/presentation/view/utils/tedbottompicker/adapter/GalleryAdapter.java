package com.numplates.nomera3.presentation.view.utils.tedbottompicker.adapter;

import static com.meera.core.utils.files.FileUtilsImpl.MEDIA_TYPE_VIDEO;
import static com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.PickerFragmentKt.MAX_PICTURE_COUNT;
import static com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.PickerFragmentKt.MAX_VIDEO_COUNT;
import static com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile.CAMERA;
import static com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile.GALLERY;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.meera.core.extensions.ViewKt;
import com.meera.core.utils.files.FileManager;
import com.meera.media_controller_common.MediaControllerOpenPlace;
import com.numplates.nomera3.R;
import com.numplates.nomera3.presentation.view.utils.camera.CameraProvider;
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment;
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile;
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.view.TedSquareFrameLayout;
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.view.TedSquareImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by TedPark on 2016. 8. 30..
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private OnOpenImagePreview onOpenImagePreview;
    private OnItemClickListener onItemClickListener;
    private OnBlockedItemClickListener onBlockedItemClickListener;

    public ArrayList<PickerTile> pickerTiles;
    public ArrayList<PickerTile> selectedPickerTiles;
    private Set<String> preselectedMedia = new HashSet<>();
    private List<Uri> selectedUriList;

    private final Context context;
    private final CameraProvider cameraProvider;
    private final LifecycleOwner lifecycleOwner;
    private final FileManager fileManager;
    private TedBottomSheetDialogFragment.BaseBuilder builder;
    public boolean isMultiModeEnabled = false;
    public String bucketId;

    private TedBottomSheetDialogFragment.IVideoDurationRequest duration;

    public GalleryAdapter(
        Context context,
        TedBottomSheetDialogFragment.BaseBuilder builder,
        CameraProvider cameraProvider,
        LifecycleOwner lifecycleOwner,
        FileManager fileManager) {
        this.context = context.getApplicationContext();
        this.builder = builder;
        this.cameraProvider = cameraProvider;
        this.lifecycleOwner = lifecycleOwner;
        this.fileManager = fileManager;
        loadTiles();
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
        loadTiles();
    }

    public void permissionsGranted() {
        loadTiles();
    }

    public void disableAll() {
        selectedPickerTiles.clear();
        selectedUriList.clear();
        for (PickerTile i : pickerTiles) {
            i.setSelected(false);
            i.setOverlaid(true);
        }
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        selectedPickerTiles.clear();
        selectedUriList.clear();
        for (PickerTile i : pickerTiles) {
            i.setSelected(false);
            i.setOverlaid(false);
        }
        notifyDataSetChanged();
    }

    public void updateCounters(int cnt) {
        for (PickerTile i : selectedPickerTiles) {
            if (i.getCounter() > cnt)
                i.setCounter(i.getCounter() - 1);
        }
    }

    public void setSelectedUriList(List<Uri> selectedUriList, @NonNull Uri uri) {
        Timber.d("Picker: selectedUri = " + selectedUriList + "\n selectedPickerTiles = " + selectedPickerTiles);
        boolean isVideoUri = fileManager.getMediaType(uri) == MEDIA_TYPE_VIDEO;

        HashSet<Uri> selectedSet = new HashSet<>();
        selectedSet.addAll(selectedUriList);
        selectedSet.addAll(this.selectedUriList);
        selectedSet.add(uri);
        boolean isNeedToUpdateAll = false;
        if (preselectedMedia.size() + selectedSet.size() == MAX_PICTURE_COUNT || isVideoUri)
            isNeedToUpdateAll = true;

        this.selectedUriList.clear();
        this.selectedUriList.addAll(selectedUriList);
        Timber.d("setSelectedUriList called");
        if (isNeedToUpdateAll)
            notifyDataSetChanged();
        else {
            for (Uri i : selectedSet) {
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

    public void loadTiles() {
        pickerTiles = new ArrayList<>();
        selectedUriList = new ArrayList<>();
        selectedPickerTiles = new ArrayList<>();

        if (builder.showCamera) {
            pickerTiles.add(new PickerTile(CAMERA));
        }

        if (builder.showGallery) {
            pickerTiles.add(new PickerTile(GALLERY));
        }

        Cursor cursor = null;
        try {
            String[] columns;
            String orderBy;
            String selection = null;
            Uri uri;
            if (builder.mediaType == TedBottomSheetDialogFragment.BaseBuilder.MediaType.IMAGE) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                columns = new String[]{
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_ID
                };
                orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
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
            }

            cursor = context.getApplicationContext()
                .getContentResolver()
                .query(uri, columns, selection, null, orderBy);
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

                    String imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex));
                    String bucketId = cursor.getString(cursor.getColumnIndex(bucketIdIndex));
                    int type;
                    //long durations;
                    if (fileManager.getMediaType(Uri.parse(imageLocation)) == MEDIA_TYPE_VIDEO) {
                        type = PickerTile.VIDEO;
                    } else {
                        type = PickerTile.IMAGE;
                    }
                    File imageFile = new File(imageLocation);
                    PickerTile pt = new PickerTile(Uri.fromFile(imageFile), type);
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

    public void updateCameraItem() {
        notifyItemChanged(0);
    }

    private void disableVideoContent() {
        for (int i = 1; i < pickerTiles.size(); i++) {
            if (pickerTiles.get(i).getTileType() == PickerTile.VIDEO) {
                pickerTiles.get(i).setOverlaid(true);
                pickerTiles.get(i).setSelected(false);
                notifyItemChanged(i);
            }
        }
    }

    private void enableVideoContent() {
        for (int i = 1; i < pickerTiles.size(); i++) {
            if (pickerTiles.get(i).getTileType() == PickerTile.VIDEO) {
                pickerTiles.get(i).setOverlaid(false);
                pickerTiles.get(i).setSelected(false);
                notifyItemChanged(i);
            }
        }
    }


    private Integer getPicketTilePositionForUri(Uri uri) {
        try {
            for (int i = 1; i < pickerTiles.size(); i++) {
                if (pickerTiles.get(i).getImageUri().getPath().equals(uri.getPath())) {
                    return i;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.tedbottompicker_grid_item, null);
        return new GalleryViewHolder(view, cameraProvider, lifecycleOwner);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, int position) {
        PickerTile pickerTile = getItem(position);
        boolean isSelected = false;

        if (pickerTile.isCameraTile()) {
            holder.ivThumbnail.setImageResource(android.R.color.transparent);
            ViewKt.loadGlide(holder.ivThumbnail, builder.cameraTileDrawable);
            holder.ivThumbnail.setBackgroundResource(builder.cameraTileBackgroundResId);
            //holder.ivThumbnail.setImageDrawable(builder.cameraTileDrawable);
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            holder.ivSelected.setVisibility(View.GONE);
            holder.previewCamera.setVisibility(View.VISIBLE);
            if (ActivityCompat.checkSelfPermission(holder.itemView.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraProvider.startCameraPreviewViewHolder(lifecycleOwner, holder.previewCamera);
            }
        } else if (pickerTile.isGalleryTile()) {
            holder.ivThumbnail.setBackgroundResource(builder.galleryTileBackgroundResId);
            holder.ivThumbnail.setImageDrawable(builder.galleryTileDrawable);
            holder.ivSelected.setVisibility(View.GONE);
        } else {

            holder.previewCamera.setVisibility(View.GONE);

            holder.ivThumbnail.setBackgroundResource(0);
            if (isMultiModeEnabled) {
                holder.ivSelected.setVisibility(View.VISIBLE);
            } else holder.ivSelected.setVisibility(View.GONE);

            updatePadding(pickerTile, holder);

            Uri uri = pickerTile.getImageUri();
            boolean isNeedToLoadImage = uri != holder.uri;
            holder.uri = uri;
            if (builder.imageProvider == null) {
                if (isNeedToLoadImage) {
                    loadImage(holder.ivThubnailWithPadding, uri); //костыль так как с эффектом продавлливания были проблемы (что только не пробовал)
                    loadImage(holder.ivThumbnail, uri);
                }
            } else {
                builder.imageProvider.onProvideImage(holder.ivThumbnail, uri);
            }
            isSelected = selectedUriList.contains(uri);
            holder.ivSelected.setBackgroundResource(0);
            if (isSelected) {
                holder.ivSelected.setBackgroundResource(R.drawable.circle_tab_bg);
                holder.ivSelected.setText(String.valueOf(preselectedMedia.size() + pickerTile.getCounter()));
            } else {
                holder.ivSelected.setBackgroundResource(R.drawable.white_ring_bg);
                holder.ivSelected.setText("");
            }
        }
        holder.vWhiteBg.setOnClickListener(v -> {
            boolean isReachMaxCount = selectedPickerTiles.size() + preselectedMedia.size() == MAX_PICTURE_COUNT;
            boolean isVideoTile = selectedPickerTiles.stream().anyMatch(PickerTile::isVideoTile);
            int maxAvailableCount = isVideoTile ? MAX_VIDEO_COUNT : MAX_PICTURE_COUNT;
            if (isReachMaxCount || isVideoTile) {
                onBlockedItemClickListener.onBlockedItemClick(
                    holder.itemView,
                    holder.getBindingAdapterPosition(),
                    maxAvailableCount
                );
            }
        });
        if (pickerTile.isOverlaid()) {
            holder.vWhiteBg.setVisibility(View.VISIBLE);
        } else holder.vWhiteBg.setVisibility(View.GONE);

        // init video duration
        if (pickerTile.getTileType() == PickerTile.VIDEO) {
            if (duration != null) {
                duration.requestVideoDuration(pickerTile.getImageUri(), duration -> {
                    long secondsDuration = duration / 1000;
                    long minutesDuration = duration / 1000 / 60;
                    String minutes = String.valueOf(minutesDuration);
                    if (minutes.length() == 1) minutes = "0" + minutes;
                    String seconds = String.valueOf(((duration % (1000 * 60)) / 1000));
                    if (seconds.length() == 1) seconds = "0" + seconds;

                    holder.tvVideoTime.setText(String.format("%s:%s", minutes, seconds));
                    holder.tvVideoTimeWithPadding.setText(String.format("%s:%s", minutes, seconds));
                    int maxVideoDuration;
                    if (builder.type == MediaControllerOpenPlace.Chat.INSTANCE) maxVideoDuration = 5 * 60;
                    else maxVideoDuration = 1 * 60;
                    if (secondsDuration > maxVideoDuration) {
                        holder.clVideoContainer.setBackgroundColor(Color.parseColor("#65ee2121"));
                        holder.clVideoContainerWithPadding.setBackgroundColor(Color.parseColor("#65ee2121"));
                    } else {
                        holder.clVideoContainer.setBackgroundColor(Color.parseColor("#73000000"));
                        holder.clVideoContainerWithPadding.setBackgroundColor(Color.parseColor("#73000000"));
                    }
                });
            }
        } else {
            holder.clVideoContainer.setVisibility(View.GONE);
        }


        initClickListeners(holder, pickerTile);
    }

    private void initClickListeners(GalleryViewHolder holder, PickerTile pickerTile) {
        if (onItemClickListener != null) {
            View v;
            if (isMultiModeEnabled && pickerTile.getTileType() != CAMERA) {
                v = holder.selectedContainer;
                holder.ivThumbnail.setOnClickListener(view -> { // open preview in multi selected mode
                    if (onOpenImagePreview != null) {
                        onOpenImagePreview.onOpenPreview(holder.itemView, holder.getAdapterPosition());
                    }
                });
                holder.ivThubnailWithPadding.setOnClickListener(view -> {
                    if (onOpenImagePreview != null) {
                        onOpenImagePreview.onOpenPreview(holder.itemView, holder.getAdapterPosition());
                    }
                });
            } else {
                v = holder.ivThumbnail;
            }
            v.setOnClickListener(view -> {
                if (v == holder.selectedContainer) {
                    pickerTile.setSelected(!pickerTile.isSelected());
                }
                Uri uri = pickerTile.getImageUri();
                if (selectedUriList.contains(uri)) {
                    selectedPickerTiles.remove(pickerTile);
                    updateCounters(pickerTile.getCounter());
                    pickerTile.setCounter(0);
                } else {
                    if (preselectedMedia.size() + selectedPickerTiles.size() < MAX_PICTURE_COUNT
                        && pickerTile.getTileType() != CAMERA) {
                        selectedPickerTiles.add(pickerTile);
                        pickerTile.setCounter(selectedPickerTiles.size());
                    }
                }
                updateWhiteBg();
                onItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
            });

        }
    }

    public void setPreselectedMedia(Set<String> preselectedMedia) {
        this.preselectedMedia = preselectedMedia;
        updateWhiteBg();
        notifyDataSetChanged();
    }

    private void loadImage(TedSquareImageView ivThumbnail, Uri uri) {
        Glide.with(context)
            .load(uri)
            .apply(new RequestOptions().centerCrop()
                .placeholder(R.drawable.ic_gallery)
                .error(R.drawable.img_error))
            .into(ivThumbnail);
    }

    private void updatePadding(PickerTile pickerTile, GalleryViewHolder holder) {
        if (pickerTile.isSelected()) {
            //Timber.d("Bazaleev holder pos = " + holder.getAdapterPosition() + " adress = " + holder.clVideoContainer);
            holder.ivThumbnail.setVisibility(View.GONE);
            holder.ivThubnailWithPadding.setVisibility(View.VISIBLE);
            holder.clVideoContainer.setVisibility(View.GONE);
            holder.clVideoContainerWithPadding.setVisibility(View.VISIBLE);
        } else {
            //Timber.d("Bazaleev holder pos = " + holder.getAdapterPosition() + " adress = " + holder.clVideoContainer);
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            holder.ivThubnailWithPadding.setVisibility(View.GONE);
            holder.clVideoContainer.setVisibility(View.VISIBLE);
            holder.clVideoContainerWithPadding.setVisibility(View.GONE);
        }
        if (pickerTile.getTileType() != PickerTile.VIDEO) {
            holder.clVideoContainer.setVisibility(View.GONE);
            holder.clVideoContainerWithPadding.setVisibility(View.GONE);
        }
    }

    public void updateWhiteBg() {
        boolean isContainVideo = false;
        for (PickerTile i : selectedPickerTiles) {
            if (i.getTileType() == PickerTile.VIDEO) {
                isContainVideo = true;
                break;
            }
        }
        if (isContainVideo) {
            for (PickerTile i : pickerTiles) {
                i.setOverlaid(!selectedPickerTiles.contains(i) || i.getTileType() != PickerTile.VIDEO);
            }
        } else {
            boolean isNeed = (preselectedMedia.size() + selectedPickerTiles.size() == MAX_PICTURE_COUNT);
            boolean hasPreselected = preselectedMedia.size() != 0;
            for (PickerTile i : pickerTiles) {
                if (!selectedPickerTiles.contains(i)) {
                    if (selectedPickerTiles.size() >= 1
                        && (selectedPickerTiles.get(0).getTileType() == PickerTile.IMAGE)
                        && i.getTileType() == PickerTile.VIDEO
                    ) {
                        i.setOverlaid(true);
                    } else {
                        i.setOverlaid(isNeed);
                    }
                }
                if (hasPreselected && i.getTileType() == PickerTile.VIDEO) {
                    i.setOverlaid(true);
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
        OnItemClickListener onItemClickListener
    ) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setBlockedOnItemClickListener(
        OnBlockedItemClickListener onBlockedItemClickListener
    ) {
        this.onBlockedItemClickListener = onBlockedItemClickListener;
    }

    public void setOnOpenImagePreview(OnOpenImagePreview callback) {
        this.onOpenImagePreview = callback;
    }

    public void addItemSelected(Uri uri) {
        PickerTile tile = new PickerTile(uri);
        selectedPickerTiles.add(tile);
        tile.setSelected(true);
        tile.setCounter(selectedPickerTiles.size());
        pickerTiles.add(1, tile);
        notifyDataSetChanged();
        updateWhiteBg();
    }

    public void addItemNotSelected(Uri uri, int type) {
        PickerTile tile = new PickerTile(uri);
        //selectedPickerTiles.add(tile);
        tile.setCounter(0);
        pickerTiles.add(1, tile);
        updateWhiteBg();
        notifyDataSetChanged();
    }


    public void setSelected(Uri selected, boolean isSelected) {
        if (selected == null) return;
        for (PickerTile i : pickerTiles) {
            if (i.getImageUri() != null && i.getImageUri().getPath().equals(selected.getPath())) {
                if (isSelected) {
                    if (!selectedUriList.contains(selected)) selectedUriList.add(selected);
                    i.setSelected(true);
                    selectedPickerTiles.add(i);
                    i.setCounter(selectedPickerTiles.size());
                } else {
                    i.setSelected(false);
                    selectedUriList.remove(selected);
                    selectedPickerTiles.remove(i);
                    updateCounters(i.getCounter());
                    i.setCounter(0);
                }
                break;
            }
        }
        updateWhiteBg();
        notifyDataSetChanged();
    }

    public void addAndSelectVideo(PickerTile tile) {
        tile.setCounter(1);
        pickerTiles.add(1, tile);
        selectedUriList.add(tile.getImageUri());
        selectedPickerTiles.add(tile);
        tile.setSelected(true);
        tile.setOverlaid(false);
        updateWhiteBg();
        notifyDataSetChanged();
    }

    public void setDurationListener(TedBottomSheetDialogFragment.IVideoDurationRequest duration) {
        this.duration = duration;
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnBlockedItemClickListener {
        void onBlockedItemClick(View view, int position, int maxAvailableCount);
    }

    public interface OnOpenImagePreview {
        void onOpenPreview(View v, int position);
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder {

        TedSquareFrameLayout root;
        TedSquareImageView ivThumbnail;
        View vCameraBackground;
        PreviewView previewCamera;
        TedSquareImageView ivThubnailWithPadding;
        TextView ivSelected;
        LinearLayout selectedContainer;
        ConstraintLayout clVideoContainer;
        TextView tvVideoTime;
        TextView tvVideoTimeWithPadding;
        ConstraintLayout clVideoContainerWithPadding;
        View vWhiteBg;
        Uri uri;
        FrameLayout flImageContainer;
        CameraProvider cameraProvider;
        LifecycleOwner lifecycleOwner;

        public GalleryViewHolder(View view, CameraProvider cameraProvider, LifecycleOwner lifecycleOwner) {
            super(view);
            root = view.findViewById(R.id.root);
            ivThumbnail = view.findViewById(R.id.iv_thumbnail);
            vCameraBackground = view.findViewById(R.id.v_camera_background);
            previewCamera = view.findViewById(R.id.preview_camera);
            ivSelected = view.findViewById(R.id.iv_selected);
            vWhiteBg = view.findViewById(R.id.v_white_bg);
            selectedContainer = view.findViewById(R.id.ll_container_selected);
            flImageContainer = view.findViewById(R.id.fl_img_container);
            ivThubnailWithPadding = itemView.findViewById(R.id.iv_thumbnail_with_padding);
            clVideoContainer = itemView.findViewById(R.id.cl_video);
            clVideoContainerWithPadding = itemView.findViewById(R.id.cl_video_selected);
            tvVideoTime = itemView.findViewById(R.id.tv_video_time);
            tvVideoTimeWithPadding = itemView.findViewById(R.id.tv_video_time_selected);
            this.cameraProvider = cameraProvider;
            this.lifecycleOwner = lifecycleOwner;
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
