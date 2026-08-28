package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.bearer.DabBearer
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MediaRequestObjectTest {

    @Test
    @DisplayName("mcc_from_fm_bearer_will_be_returned[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-250516#]")
    fun mcc_from_fm_bearer_will_be_returned() {
        val bearer = FmBearer.createFromString("::mcc::", "fm:1a0.110b.10710")
        val callback = Mockito.mock(IRadioStationDataCallback::class.java)
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer!!, eTags, callback)

        Assertions.assertEquals("::mcc::", mediaRequestObject.mcc)
    }

    @Test
    @DisplayName("mcc_from_dab_bearer_will_be_returned[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-250516#]")
    fun mcc_from_dab_bearer_will_be_returned() {
        val bearer = DabBearer.createFromString("::mcc::", "dab:1e0.07d7.100d.0")
        val callback = Mockito.mock(IRadioStationDataCallback::class.java)
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer!!, eTags, callback)

        Assertions.assertEquals("::mcc::", mediaRequestObject.mcc)
    }

    @Test
    @DisplayName("mcc_from_hd_bearer_is_null[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-250516#]")
    fun mcc_from_hd_bearer_is_null() {
        val bearer = HdBearer.createFromString("hd:040.00066.1")
        val callback = Mockito.mock(IRadioStationDataCallback::class.java)
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer!!, eTags, callback)

        Assertions.assertNull(mediaRequestObject.mcc)
    }
}