package de.esolutions.connectedradioclient.data.bearer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DabBearerTest {

    @Test
    fun parse_correct_from_string() {
        val bearerString = "gcc.eid.sid.scids"
        val dabBearer = DabBearer.createFromString("mcc", bearerString)
        assertNotNull(dabBearer)
        assertEquals("mcc", dabBearer!!.mcc)
        assertEquals("cc", dabBearer.ecc)
        assertEquals("eid", dabBearer.eid)
        assertEquals("sid", dabBearer.sid)
        assertEquals("scids", dabBearer.scids)
    }

    @Test
    fun return_null_on_invalid_stirng() {
        val invalidBearerString1 = "gcceidsidscids"
        val dabBearer1 = DabBearer.createFromString("mcc", invalidBearerString1)
        assertNull(dabBearer1)

        val invalidBearerString2 = "gcc.eid.sid.scids."
        val dabBearer2 = DabBearer.createFromString("mcc", invalidBearerString2)
        assertNull(dabBearer2)

        val invalidBearerString3 = "gcc.eid.sid"
        val dabBearer3 = DabBearer.createFromString("mcc", invalidBearerString3)
        assertNull(dabBearer3)
    }

    @Test
    fun return_correct_bearer_string() {
        val dabBearer = DabBearer("mcc", "ecc", "eid", "sid", "scids")
        val bearerString = dabBearer.getBearerString()
        assertEquals("dab:ecc.eid.sid.scids", bearerString)
    }
}