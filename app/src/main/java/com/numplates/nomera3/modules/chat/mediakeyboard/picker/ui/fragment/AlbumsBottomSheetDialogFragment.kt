package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment

import android.Manifest
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPickerAlbumsBinding
import com.numplates.nomera3.modules.baseCore.ui.permission.PermissionDelegate
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity.Album
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.adapter.PickerAlbumsAdapter
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import java.io.File

class AlbumsBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<FragmentPickerAlbumsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPickerAlbumsBinding
        get() = FragmentPickerAlbumsBinding::inflate

    private val albumsAdapter by lazy { PickerAlbumsAdapter(this::albumClicked) }

    private val currentAlbumId by lazy { arguments?.getString(KEY_ALBUM, null) }

    private val permissionDelegate by lazy {
        PermissionDelegate(act, viewLifecycleOwner)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                val bottomSheetBehavior = BottomSheetBehavior.from(it)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior.skipCollapsed = true
                val layoutParams = it.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.layoutParams = layoutParams
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        checkPermission()
    }

    private fun initViews() {
        binding?.apply {
            tvCancel.setOnClickListener { dismiss() }
            rvAlbums.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = albumsAdapter
            }
        }
    }

    private fun checkPermission() {
        permissionDelegate.setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    loadAlbums()
                }

                override fun onDenied() {
                    dismiss()
                }

                override fun onError(error: Throwable?) {
                    dismiss()
                }
            },
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun loadAlbums() {
        val contentResolver = act?.contentResolver ?: return
        val contentUri = MediaStore.Files.getContentUri("external")
        val projections = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATA,
        )
        val orderBy = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"
        val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        val albums = mutableMapOf<String?, Album>()

        contentResolver.query(contentUri, projections, selection, null, orderBy)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val bucketIdIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)
                val bucketNameIndex =
                    cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                val imageUriIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)

                val defaultAlbum = Album(
                    id = DEFAULT_ALBUM_ID,
                    name = getString(R.string.recent),
                    lastImageUri = Uri.fromFile(File(cursor.getString(imageUriIndex))),
                    chosen = currentAlbumId == DEFAULT_ALBUM_ID
                )
                albums[DEFAULT_ALBUM_ID] = defaultAlbum

                do {
                    val bucketId = cursor.getString(bucketIdIndex)

                    val album = albums[bucketId] ?: let {
                        val bucketName = cursor.getString(bucketNameIndex)
                        val lastImageUri = Uri.fromFile(File(cursor.getString(imageUriIndex)))
                        val album = Album(
                            id = bucketId,
                            name = bucketName,
                            lastImageUri = lastImageUri,
                            chosen = currentAlbumId == bucketId
                        )
                        albums[bucketId] = album

                        album
                    }

                    album.imagesCount++
                    defaultAlbum.imagesCount++

                } while (cursor.moveToNext())

                cursor.close()
            }

        }

        albumsAdapter.items = albums.values.toList()
    }

    private fun albumClicked(album: Album) {
        setFragmentResult(
            KEY_ALBUM,
            bundleOf(KEY_ALBUM to album)
        )
        dismiss()
    }

    companion object {
        const val KEY_ALBUM = "ALBUM_ID"
        private val DEFAULT_ALBUM_ID = null

        fun newInstance(currentAlbumId: String?): AlbumsBottomSheetDialogFragment {
            return AlbumsBottomSheetDialogFragment().apply {
                arguments = bundleOf(KEY_ALBUM to currentAlbumId)
            }
        }
    }

}
