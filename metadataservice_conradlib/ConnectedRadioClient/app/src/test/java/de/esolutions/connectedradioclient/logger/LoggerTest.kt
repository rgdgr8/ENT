package de.esolutions.connectedradioclient.logger

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Tests for the Logger class.
 * Note: Since Logger now uses android.util.Log directly, we can only test
 * the public utility methods (buildLogMessage, trimToken) and not the actual
 * logging behavior which would require Android instrumentation tests.
 */
class LoggerTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            // Initialize the logger for tests
            Logger.init("test")
        }
    }

    @Test
    @DisplayName("test_logger_error_with_exception_builds_correct_message[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259344,HCP3_GLOBAL_CLU41:HCPG-281629,OI_Global_CLU51:OIG-53050#]")
    fun test_logger_error_with_exception_builds_correct_message() {
        val logInfo = LogInfo("NoBearer", "NoRequestId")
        val tag = LoggerTags.LOGO

        val logMessage = Logger.buildLogMessage(logInfo, "My Message java.lang.RuntimeException: My Exception Text", tag)
        
        val expectedMessage = "[NoBearer] My Message java.lang.RuntimeException: My Exception Text #logo #request:NoRequestId #crc"
        Assertions.assertEquals(expectedMessage, logMessage)
    }

    @Test
    @DisplayName("it_builds_log_messages_as_expected_with_filled_log_info[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253243,HCP3_GLOBAL_CLU41:HCPG-281629,OI_Global_CLU51:OIG-53050#]")
    fun it_builds_log_messages_as_expected_with_filled_log_info() {
        val logInfo = LogInfo("Bearer", "RequestID")
        val logMessage = Logger.buildLogMessage(logInfo, "Message", LoggerTags.REQUEST)

        Assertions.assertEquals("[Bearer] Message #request #request:RequestID #crc", logMessage)
    }

    @Test
    @DisplayName("it_builds_log_messages_as_expected_with_empty_log_info[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253243,HCP3_GLOBAL_CLU41:HCPG-281629,OI_Global_CLU51:OIG-53050#]")
    fun it_builds_log_messages_as_expected_with_empty_log_info() {
        val logInfo = LogInfo(null, null)
        val logMessage = Logger.buildLogMessage(logInfo, "Message", LoggerTags.REQUEST)

        Assertions.assertEquals("Message #request #crc", logMessage)
    }

    @Test
    @DisplayName("it_builds_log_messages_as_expected_without_log_info[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253243,HCP3_GLOBAL_CLU41:HCPG-281629,OI_Global_CLU51:OIG-53050#]")
    fun it_builds_log_messages_as_expected_without_log_info() {
        val logMessage = Logger.buildLogMessage(null, "Message", LoggerTags.REQUEST)

        Assertions.assertEquals("Message #request #crc", logMessage)
    }

    @Test
    fun it_trims_tokens_correct() {
        val fullToken = "keepPart.keepPartToo.RemovePart"
        val trimmedToken = Logger.trimToken(fullToken)

        Assertions.assertEquals("keepPart.keepPartToo", trimmedToken)
    }

    @Test
    fun it_return_empty_string_if_token_is_null() {
        val fullToken = null
        val trimmedToken = Logger.trimToken(fullToken)

        Assertions.assertEquals("", trimmedToken)
    }

    @Test
    fun it_trims_smaller_token_correct() {
        val fullToken = "PartOne.PartTwo"
        val trimmedToken = Logger.trimToken(fullToken)

        Assertions.assertEquals("PartOne.PartTwo", trimmedToken)
    }

    @Test
    fun it_trims_bigger_token_correct() {
        val fullToken = "PartOne.PartTwo.PartThree.PartFour.PartFive"
        val trimmedToken = Logger.trimToken(fullToken)

        Assertions.assertEquals("PartOne.PartTwo", trimmedToken)
    }

    @Test
    fun it_return_empty_string_if_token_is_to_small() {
        val fullToken = "PartOne"
        val trimmedToken = Logger.trimToken(fullToken)

        Assertions.assertEquals("", trimmedToken)
    }

    @Test
    fun it_return_empty_string_if_token_is_empty() {
        val fullToken = ""
        val trimmedToken = Logger.trimToken(fullToken)

        Assertions.assertEquals("", trimmedToken)
    }
}