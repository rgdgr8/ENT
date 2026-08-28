package de.esolutions.connectedradioclient.data

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RequestHeaderTest {

    @Test
    @DisplayName("to_header_map_adds_all_headers[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253243#]")
    fun to_header_map_adds_all_headers() {
        val requestHeader = RequestHeader("::token::", "::requestId::")
        requestHeader.mcc = "::mcc::"
        requestHeader.accept = "::accept::"
        requestHeader.ifNoneMatch = "::etag::"
        requestHeader.secondsSinceLastRegistration = 42L
        val map = requestHeader.toHeaders()

        Assertions.assertEquals("Bearer ::token::", map["Authorization"])
        Assertions.assertEquals("::requestId::", map["X-REQUEST-ID"])
        Assertions.assertEquals("::mcc::", map["X-Mcc"])
        Assertions.assertEquals("::accept::", map["Accept"])
        Assertions.assertEquals("::etag::", map["If-None-Match"])
        Assertions.assertEquals("42", map["X-Registration-Age"])
    }

    @Test
    @DisplayName("to_header_map_ignores_null_values[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253243#]")
    fun to_header_map_ignores_null_values() {
        val requestHeader = RequestHeader("::token::", "::requestId::")
        val map = requestHeader.toHeaders()

        Assertions.assertEquals("Bearer ::token::", map["Authorization"])
        Assertions.assertEquals("::requestId::", map["X-REQUEST-ID"])
        Assertions.assertNull(map["X-Mcc"])
        Assertions.assertNull(map["Accept"])
        Assertions.assertNull(map["If-None-Match"])
        Assertions.assertNull(map["X-Registration-Age"])
    }
}