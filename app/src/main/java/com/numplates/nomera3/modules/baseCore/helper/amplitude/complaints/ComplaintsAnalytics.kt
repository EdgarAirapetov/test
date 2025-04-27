package com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import javax.inject.Inject

interface AmplitudeComplaints {
    /**
     * @param reason complaint reason id
     * @param idFrom user's reporter id
     * @param idTo user's id who is the complaint against
     * @param haveText user added text to report
     * @param charCount text size like 0, 1, 2..
     * @param haveMedia 'true' is complaint contains any media
     * @param videoCount video files count like 0, 1, 2..
     * @param imgCount image files count like 0, 1, 2..
     * @param where property where
     */
    fun profileReportFinish(
        reason: ComplainReasonId,
        idFrom: Long,
        idTo: Long,
        haveText: Boolean,
        charCount: Int,
        haveMedia: Boolean,
        videoCount: Int,
        imgCount: Int,
        where: AmplitudePropertyWhere,
    )

    fun profileReportStart()
    fun rulesOpen(where: RulesOpenWhere)
}

class AmplitudeComplaintsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate,
    private val mapper: ComplaintsMapper,
) : AmplitudeComplaints {

    override fun profileReportFinish(
        reason: ComplainReasonId,
        idFrom: Long,
        idTo: Long,
        haveText: Boolean,
        charCount: Int,
        haveMedia: Boolean,
        videoCount: Int,
        imgCount: Int,
        where: AmplitudePropertyWhere,
    ) {
        delegate.logEvent(
            eventName = AmplitudeComplaintEventName.PROFILE_REPORT_FINISH,
            properties = {
                it.apply {
                    addProperty(mapper.mapReasonToType(reason))
                    addProperty(AmplitudePropertyNameConst.REPORT_FROM, idFrom)
                    addProperty(AmplitudePropertyNameConst.REPORT_TO, idTo)
                    addProperty(AmplitudePropertyNameConst.HAVE_TEXT, haveText)
                    addProperty(AmplitudePropertyNameConst.CHAR_COUNT, charCount)
                    addProperty(AmplitudePropertyNameConst.HAVE_MEDIA, haveMedia)
                    addProperty(AmplitudePropertyNameConst.VIDEO_COUNT, videoCount)
                    addProperty(AmplitudePropertyNameConst.IMG_COUNT, imgCount)
                    addProperty(where)
                }
            }
        )
    }

    override fun profileReportStart() {
        delegate.logEvent(eventName = AmplitudeComplaintEventName.PROFILE_REPORT_START)
    }

    override fun rulesOpen(where: RulesOpenWhere) {
        delegate.logEvent(
            eventName = AmplitudeComplaintEventName.RULES_OPEN,
            properties = {
                it.apply {
                    addProperty(where)
                }
            }
        )
    }
}
