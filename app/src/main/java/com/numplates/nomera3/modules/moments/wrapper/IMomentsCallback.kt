package com.numplates.nomera3.modules.moments.wrapper

import com.numplates.nomera3.modules.moments.show.data.MomentToUpload

interface IMomentsCallback {
    fun onReady(result: List<MomentToUpload>)
}
