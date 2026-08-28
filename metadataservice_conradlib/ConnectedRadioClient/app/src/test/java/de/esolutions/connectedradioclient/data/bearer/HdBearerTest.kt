package de.esolutions.connectedradioclient.data.bearer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.jupiter.api.Test

class HdBearerTest {

    @Test
    fun parse_correct_from_string() {
        val bearerString = "cc.tx.multicastId"
        val hdBearer = HdBearer.createFromString(bearerString)
        assertNotNull(hdBearer)
        assertEquals("cc", hdBearer!!.cc)
        assertEquals("tx", hdBearer.tx)
        assertEquals("multicastId", hdBearer.multicastId)
    }

    @Test
    fun return_null_on_invalid_stirng() {
        val invalidBearerString1 = "cctxmulticastId"
        val hdBearer1 = HdBearer.createFromString(invalidBearerString1)
        assertNull(hdBearer1)

        val invalidBearerString2 = "cc.tx.multicastId."
        val hdBearer2 = HdBearer.createFromString(invalidBearerString2)
        assertNull(hdBearer2)

        val invalidBearerString3 = "cc.tx"
        val hdBearer3 = HdBearer.createFromString(invalidBearerString3)
        assertNull(hdBearer3)
    }

    @Test
    fun return_correct_bearer_string() {
        val hdBearer1 = HdBearer("tx", "cc", "multicastId")
        val bearerString1 = hdBearer1.getBearerString()
        assertEquals("hd:cc.tx.multicastId", bearerString1)

        val hdBearer2 = HdBearer("tx", "cc", "1")
        val bearerString2 = hdBearer2.getBearerString()
        assertEquals("hd:cc.tx.1", bearerString2)
    }
}