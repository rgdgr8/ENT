package de.esolutions.connectedradioclient.api

import com.squareup.moshi.Types
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.backend.lookup.request.ETagRequest
import de.esolutions.connectedradioclient.data.backend.lookup.request.StationInformation
import de.esolutions.connectedradioclient.data.backend.lookup.result.LookupResult
import de.esolutions.connectedradioclient.data.backend.registration.request.ApiRequestObject
import de.esolutions.connectedradioclient.data.backend.registration.request.RegistrationRequest
import de.esolutions.connectedradioclient.data.backend.registration.result.RegistrationResult
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.helper.TestRadioStationDataCallback
import java.io.File
import java.lang.reflect.ParameterizedType
import java.nio.file.Paths
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MoshiParserTest {

    private var testFilePath: String? = null

    @BeforeEach
    fun before() {
        val workingDirectory = Paths.get("").toAbsolutePath().toString()
        assertNotNull(workingDirectory)
        testFilePath = "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata"
    }

    @Test
    fun return_null_on_error() {
        val lookupData = GenericMoshiParser.parseFromJson<LookupResult>("")
        assertNull(lookupData)

        val registrationResult = GenericMoshiParser.parseFromJson<RegistrationResult>("")
        assertNull(registrationResult)
    }

    @Test
    fun parse_lookup_response_correct_from_json() {
        val parameterizedType: ParameterizedType =
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                LookupResult::class.java
            )
        val lookupJson = File("$testFilePath/lookupResponse.json").readText()
        val lookupData = GenericMoshiParser.parseFromJson<Map<String, LookupResult>>(
            lookupJson,
            parameterizedType
        )
        assertNotNull(lookupData)

        val data1 = lookupData!!["fm:?.d318.10110"]
        assertNull(data1?.data)
        assertEquals(304, data1?.status)

        val data2 = lookupData["fm:?.d318.10570"]
        assertNull(data2?.data)
        assertEquals(204, data2?.status)

        val data3 = lookupData["fm:?.d318.10130"]
        assertEquals(200, data3?.status)
        assertNotNull(data3?.data)
        assertEquals("ANTBAY", data3?.data?.shortName)
        assertEquals("ANTENNE BAYERN", data3?.data?.mediumName)
        assertEquals("Antenne Bayern - Nur die besten Musikhitzzz", data3?.data?.longName)
        assertEquals("dab:de0.10a5.d318.0", data3?.data?.bearers?.get(0))
        assertEquals(232, data3?.data?.logo)

        val data4 = lookupData["fm:?.d318.10135"]
        assertEquals(-1, data4?.data?.logo)

        val data5 = lookupData["fm:e0.d318.10145"]
        assertNull(data5?.data?.logo)
    }

    @Test
    fun parse_lookup_request_correct_from_json_if_bearer_and_streams_null() {
        val parameterizedType: ParameterizedType =
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                LookupResult::class.java
            )
        val lookupJson = File("$testFilePath/lookupResponseNoBearerAndStreams.json").readText()
        val lookupData = GenericMoshiParser.parseFromJson<Map<String, LookupResult>>(
            lookupJson,
            parameterizedType
        )
        assertNotNull(lookupData)

        val data1 = lookupData!!["fm:?.d318.10110"]
        assertNull(data1?.data)
        assertEquals(304, data1?.status)

        val data2 = lookupData["fm:?.d318.10570"]
        assertNull(data2?.data)
        assertEquals(204, data2?.status)

        val data3 = lookupData["fm:?.d318.10130"]
        assertEquals(200, data3?.status)
        assertNotNull(data3?.data)
        assertEquals("ANTBAY", data3?.data?.shortName)
        assertEquals("ANTENNE BAYERN", data3?.data?.mediumName)
        assertEquals("Antenne Bayern - Nur die besten Musikhitzzz", data3?.data?.longName)
        assertNull(data3?.data?.bearers)
        assertNull(data3?.data?.streams)
        assertEquals(232, data3?.data?.logo)

        val data4 = lookupData["fm:?.d318.10135"]
        assertNull(data4?.data?.bearers)
        assertNotNull(data4?.data?.streams)
        assertEquals(1, data4?.data?.streams?.size)
        assertEquals(-1, data4?.data?.logo)

        val data5 = lookupData["fm:?.d318.10145"]
        assertNotNull(data5?.data?.bearers)
        assertEquals("dab:de0.10a5.d318.0", data5?.data?.bearers?.get(0))
        assertNull(data5?.data?.streams)
        assertNull(data5?.data?.logo)
    }

    @Test
    fun parse_lookup_request_correct_to_json() {
        val map: MutableMap<String, MediaRequestObject> = mutableMapOf()
        val eTags = ETags("::metadata::", null, null, null, null)
        val withTimeStamp = MediaRequestObject(
            HdBearer("tx", "cc", "1"),
            eTags,
            TestRadioStationDataCallback()
        )
        val withoutTimeStamp = MediaRequestObject(
            FmBearer("mcc", "pi", "frequency"),
            eTags,
            TestRadioStationDataCallback()
        )
        map[withTimeStamp.bearer.getBearerString()] = withTimeStamp
        map[withoutTimeStamp.bearer.getBearerString()] = withoutTimeStamp

        val cleanMap = map.mapValues { StationInformation(ETagRequest(eTags)) }

        val json = GenericMoshiParser.parseToJson(cleanMap)
        assertTrue(json.contains("\"hd:cc.tx.1\":{\"etags\":{\"metadata\":\"::metadata::\"}}"))
        assertTrue(json.contains("\"fm:?.pi.frequency\":{\"etags\":{\"metadata\":\"::metadata::\"}}"))
    }

    @Test
    @DisplayName("parse_json_has_overflow[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun parse_json_has_overflow() {
        val registrationJson = File("$testFilePath/registrationResponseWithOverflow.json").readText()
        val registrationResult = GenericMoshiParser.parseFromJson<RegistrationResult>(
            registrationJson
        )
        assertNull(registrationResult)
    }

    @Test
    @DisplayName("parse_json_has_wrong_format[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun parse_json_has_wrong_format() {
        val registrationJson = File("$testFilePath/registrationResponseInvalidJson.json").readText()
        val registrationResult = GenericMoshiParser.parseFromJson<RegistrationResult>(
            registrationJson
        )
        assertNull(registrationResult)
    }

    @Test
    @DisplayName("parse_json_has_wrong_datatype[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun parse_json_has_wrong_datatype() {
        val registrationJson = File("$testFilePath/registrationResponseWithWrongDatatype.json").readText()
        val registrationResult = GenericMoshiParser.parseFromJson<RegistrationResult>(
            registrationJson
        )
        assertNull(registrationResult)
    }

    @Test
    @DisplayName("parse_registration_response_correct_from_json[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun parse_registration_response_correct_from_json() {
        val registrationJson = File("$testFilePath/registrationResponse.json").readText()
        val registrationResult = GenericMoshiParser.parseFromJson<RegistrationResult>(
            registrationJson
        )
        assertNotNull(registrationResult)

        val config = registrationResult!!.config
        assertEquals(20, config.batchSize)
        assertEquals(0.5, config.batchDebounce)
        assertEquals(300, config.connectionKeepalive)
        assertEquals(true, config.sessionEnabled)
        assertEquals("testCookie", config.sessionCookieName)
        assertEquals(86400, config.maxRegistrationAge)

        // RetryBehavior
        assertEquals(8, config.statusCodeRetryBehavior.size)
        assertEquals(RetryBehavior.DELAYED, config.statusCodeRetryBehavior[202])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[401])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[403])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[429])
        assertEquals(RetryBehavior.ONCE, config.statusCodeRetryBehavior[500])
        assertEquals(RetryBehavior.ONCE, config.statusCodeRetryBehavior[502])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[503])
        assertEquals(RetryBehavior.ONCE, config.statusCodeRetryBehavior[504])

        // ContentExpiry
        assertEquals(4, config.statusCodeContentExpiry.size)
        assertEquals(86400, config.statusCodeContentExpiry[200])
        assertEquals(86400, config.statusCodeContentExpiry[204])
        assertEquals(86400, config.statusCodeContentExpiry[304])
        assertEquals(2419200, config.statusCodeContentExpiry[400])

        val api = registrationResult.api
        assertEquals("url/to/lookup", api.lookup)
        assertEquals("url/to/logo", api.logo)
    }

    @Test
    @DisplayName("parse_registration_response_without_session_enabled_correct_from_json[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun parse_registration_response_without_session_enabled_correct_from_json() {
        val registrationJson = File("$testFilePath/registrationResponseWithoutSessionEnabled.json")
            .readText()
        val registrationResult = GenericMoshiParser.parseFromJson<RegistrationResult>(
            registrationJson
        )
        assertNotNull(registrationResult)

        val config = registrationResult!!.config
        assertEquals(20, config.batchSize)
        assertEquals(0.5, config.batchDebounce)
        assertEquals(300, config.connectionKeepalive)
        assertEquals(true, config.sessionEnabled)
        assertEquals("testCookie", config.sessionCookieName)
        assertEquals(86400, config.maxRegistrationAge)

        // RetryBehavior
        assertEquals(8, config.statusCodeRetryBehavior.size)
        assertEquals(RetryBehavior.DELAYED, config.statusCodeRetryBehavior[202])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[401])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[403])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[429])
        assertEquals(RetryBehavior.ONCE, config.statusCodeRetryBehavior[500])
        assertEquals(RetryBehavior.ONCE, config.statusCodeRetryBehavior[502])
        assertEquals(RetryBehavior.EXPONENTIAL, config.statusCodeRetryBehavior[503])
        assertEquals(RetryBehavior.ONCE, config.statusCodeRetryBehavior[504])

        // ContentExpiry
        assertEquals(4, config.statusCodeContentExpiry.size)
        assertEquals(86400, config.statusCodeContentExpiry[200])
        assertEquals(86400, config.statusCodeContentExpiry[204])
        assertEquals(86400, config.statusCodeContentExpiry[304])
        assertEquals(2419200, config.statusCodeContentExpiry[400])

        val api = registrationResult.api
        assertEquals("url/to/lookup", api.lookup)
        assertEquals("url/to/logo", api.logo)
    }

    @Test
    fun parse_registration_request_correct_to_json() {
        val apiRequestObject = ApiRequestObject("2.0", "3.0")
        val requestObject = RegistrationRequest("123456", apiRequestObject)

        val json = GenericMoshiParser.parseToJson(requestObject)
        assertNotEquals("", json)
        assertTrue(json.contains("\"version\":\"123456\""))
        assertTrue(json.contains("\"lookup\":\"2.0\""))
        assertTrue(json.contains("\"logo\":\"3.0\""))
    }
}