package com.numplates.nomera3.presentation.view.holder

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.model.Marker
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.UserInfoModel
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEnum


class MapUserInfoMarkerHolder(v: View) {

    var llUserInfo: LinearLayout = v.findViewById(R.id.llUserInfo)
    var ivAvatar: ImageView = v.findViewById(R.id.ivAvatar)
    var tvNickName: TextView = v.findViewById(R.id.tvNickName)
    var nvNumber: NumberPlateEditView = v.findViewById(R.id.nvNumber)

    fun bind(context: Context?, userInfoModel: UserInfoModel, marker: Marker) {
        Glide.with(context!!).asBitmap().load(userInfoModel.avatar).listener(object : RequestListener<Bitmap?> {
            private var isLoadedYet = false
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any,
                target: Target<Bitmap?>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                if (!isLoadedYet) {
                    isLoadedYet = true
                    Handler().post { marker.showInfoWindow() }
                }
                return false
            }
        }).into(ivAvatar)
        ivAvatar.setOnClickListener { v: View? -> }
        tvNickName.text = userInfoModel.name
        var number = userInfoModel.number
        if (number == null || number.isEmpty()) {
            number = "?????????????????????"
        }
        NumberPlateEditView.Builder(nvNumber)
                .setType(NumberPlateEnum.RU_AUTO)
                .setNum(number)
                .build()
    }

}
