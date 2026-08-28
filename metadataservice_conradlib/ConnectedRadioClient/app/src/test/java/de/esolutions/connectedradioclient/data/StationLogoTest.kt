package de.esolutions.connectedradioclient.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class StationLogoTest {

    @Test
    @DisplayName("parse_correct_from_backend_data[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-283709,OI_Global_CLU51:OIG-55568#]")
    fun parse_correct_from_backend_data() {
        val body = ByteArray(5)
        val disposition = "filename=\"123.webp\""
        val type = "image/webp"
        val stationLogo = StationLogo.createFromBackendData(body, disposition, type)!!

        assertEquals(body, stationLogo.imageData)
        assertEquals("123", stationLogo.fileName)
        assertEquals("WEBP", stationLogo.fileExtension)
    }

    @Test
    fun return_null_on_invalid_filename() {
        val body = ByteArray(5)
        val mimeType = "image/webp"

        // missing file extension
        assertNull(StationLogo.createFromBackendData(body, "filename=\"123\"", mimeType))

        // empty disposition
        assertNull(StationLogo.createFromBackendData(body, "", mimeType))

        // null as disposition
        assertNull(StationLogo.createFromBackendData(body, null, mimeType))
    }

    @Test
    fun return_null_on_invalid_mimeType() {
        val body = ByteArray(5)
        val disposition = "filename=\"123.webp\""

        // empty file extension
        assertNull(StationLogo.createFromBackendData(body, disposition, "image/"))

        // empty mime type
        assertNull(StationLogo.createFromBackendData(body, disposition, ""))

        // null as mime type
        assertNull(StationLogo.createFromBackendData(body, disposition, null))
    }
}