package de.esolutions.connectedradioclient.data.bearer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IBearerTest {

    @Test
    fun return_correct_list_of_bearer() {
        val dabBearer = "dab:gcc.eid.sid.scids"
        val fmBearer = "fm:gcc.pi.frequency"
        val hdBearer = "hd:cc.tx.multicastId"
        val bearerStringList = listOf(dabBearer, fmBearer, hdBearer)
        val bearerList = IBearer.createListFromBackendData("mcc", bearerStringList)
        assertEquals(3, bearerList.size)
    }

    @Test
    fun return_null_for_list_of_invalid_bearer() {
        val dabBearer = "dab:gcceidsidscids"
        val fmBearer = "fmgcc.pi.frequency"
        val hdBearer = "hdcctxmulticastId"
        val bearerStringList = listOf(dabBearer, fmBearer, hdBearer)
        val bearerList = IBearer.createListFromBackendData("mcc", bearerStringList)
        assertEquals(0, bearerList.size)
    }

    @Test
    fun return_only_valid_bearer() {
        val dabBearer = "dab:gcc.eid.sid.scids"
        val fmBearer = "fmgcc.pi.frequency"
        val hdBearer = "hd:tx.multicastId"
        val bearerStringList = listOf(dabBearer, fmBearer, hdBearer)
        val bearerList = IBearer.createListFromBackendData("mcc", bearerStringList)
        assertEquals(1, bearerList.size)
    }
}