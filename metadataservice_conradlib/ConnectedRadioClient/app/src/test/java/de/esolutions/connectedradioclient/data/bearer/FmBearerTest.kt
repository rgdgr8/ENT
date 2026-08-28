package de.esolutions.connectedradioclient.data.bearer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FmBearerTest {

    @Test
    @DisplayName("parse_correct_from_string[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-248887#]")
    fun parse_correct_from_string() {
        val bearerString = "gcc.pi.frequency"
        val fmBearer = FmBearer.createFromString("mcc", bearerString)
        assertNotNull(fmBearer)
        assertEquals("mcc", fmBearer!!.mcc)
        assertEquals("pi", fmBearer.pi)
        assertEquals("frequency", fmBearer.frequency)
    }

    @Test
    @DisplayName("return_null_on_invalid_stirng[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-248887#]")
    fun return_null_on_invalid_stirng() {
        val invalidBearerString1 = "gccpifrequency"
        val fmBearer1 = FmBearer.createFromString("mcc", invalidBearerString1)
        assertNull(fmBearer1)

        val invalidBearerString2 = "gcc.pi.frequency."
        val fmBearer2 = FmBearer.createFromString("mcc", invalidBearerString2)
        assertNull(fmBearer2)

        val invalidBearerString3 = "gcc.pi"
        val fmBearer3 = FmBearer.createFromString("mcc", invalidBearerString3)
        assertNull(fmBearer3)
    }

    @Test
    @DisplayName("return_correct_bearer_string[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-248887#]")
    fun return_correct_bearer_string() {
        val fmBearer1 = FmBearer("mcc", "pi", "frequency")
        val bearerString1 = fmBearer1.getBearerString()
        assertEquals("fm:?.pi.frequency", bearerString1)
    }
}