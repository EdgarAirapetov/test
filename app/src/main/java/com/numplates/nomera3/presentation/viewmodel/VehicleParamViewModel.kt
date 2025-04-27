package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.data.network.market.Field
import com.numplates.nomera3.data.network.market.ResponseWizard
import com.numplates.nomera3.presentation.viewmodel.viewevents.VehicleParamWizardEvent

class VehicleParamViewModel : ViewModel() {

    private var wizard: ResponseWizard? = null
    private var wizardPos = 0

    val liveField = MutableLiveData<Field>()
    val liveCounter = MutableLiveData<Pair<Int, Int>>()
    val liveEvent = MutableLiveData<VehicleParamWizardEvent>()


    fun init(wizard: ResponseWizard?, wizardPos: Int) {
        this.wizard = wizard
        this.wizardPos = wizardPos
        //wizard?.fields
        checkWizardIsEmpty()
        getNextField()
    }

    private fun checkWizardIsEmpty() {
        wizard?.let { responseWizard ->
            if (responseWizard.fields.isNullOrEmpty())
                liveEvent.value = VehicleParamWizardEvent.EmptyWizardEvent
        } ?: kotlin.run {
            liveEvent.value = VehicleParamWizardEvent.EmptyWizardEvent
        }
    }

    fun skip() = Unit
    fun getNextField() {
        wizard?.let {
            if (wizardPos == it.fields.size) {
                goFieldAtPosition(0)
            } else {
                liveField.value = it.fields[wizardPos]
                wizardPos++
                liveCounter.value = Pair(wizardPos, it.fields.size)
            }
        }
    }

    fun goFieldAtPosition(pos: Int) {
        wizard?.let {
            if (pos > it.fields.size)
                liveEvent.value = VehicleParamWizardEvent.WrongWizardPosition
            else {
                liveField.value = it.fields[pos]
                wizardPos = pos + 1
                liveCounter.value = Pair(wizardPos, it.fields.size)
            }
        }
    }
}
