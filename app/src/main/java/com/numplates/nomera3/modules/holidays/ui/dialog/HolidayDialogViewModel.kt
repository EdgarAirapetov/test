package com.numplates.nomera3.modules.holidays.ui.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import kotlinx.coroutines.launch
import javax.inject.Inject

class HolidayDialogViewModel : ViewModel() {

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var dialogDismissListener: DialogDismissListener

    init {
        App.component.inject(this)
    }

    fun onDismissDialog(){
        viewModelScope.launch {
            dialogDismissListener.dialogDismissed(DismissDialogType.HOLIDAY)
        }
        appSettings.isHolidayIntroduced = true
        appSettings.holidayIntroducedVersion = BuildConfig.VERSION_NAME
    }
}
