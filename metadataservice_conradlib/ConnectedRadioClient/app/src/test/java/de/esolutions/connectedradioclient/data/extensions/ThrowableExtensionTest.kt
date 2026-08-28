package de.esolutions.connectedradioclient.data.extensions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ThrowableExtensionTest {

    @Test
    @DisplayName("formats_exception_chain_to_string_with_high_enough_limit[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259344#]")
    fun formats_exception_chain_to_string_with_high_enough_limit() {
        val tertiaryException = NotImplementedError("Tertiary Exception")
        val secondaryException = Exception("Secondary Exception", tertiaryException)
        val primaryException = RuntimeException("Primary Exception", secondaryException)

        val expectedString = "java.lang.RuntimeException: Primary Exception\n" +
            "Caused by java.lang.Exception: Secondary Exception\n" +
            "Caused by kotlin.NotImplementedError: Tertiary Exception"

        Assertions.assertEquals(expectedString, primaryException.toSimpleString())
    }

    @Test
    @DisplayName("formats_exception_chain_to_string_with_blocking_limit[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259344#]")
    fun formats_exception_chain_to_string_with_blocking_limit() {
        val tertiaryException = NotImplementedError("Tertiary Exception")
        val secondaryException = Exception("Secondary Exception", tertiaryException)
        val primaryException = RuntimeException("Primary Exception", secondaryException)

        val expectedString = "java.lang.RuntimeException: Primary Exception\n" +
            "Caused by java.lang.Exception: Secondary Exception\n" +
            "and more..."

        Assertions.assertEquals(expectedString, primaryException.toSimpleString(2))
    }

    @Test
    @DisplayName("formats_exception_to_string_without_cause_and_blocking_limit[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259344#]")
    fun formats_exception_to_string_without_cause_and_blocking_limit() {
        val primaryException = RuntimeException("Primary Exception")

        val expectedString = "java.lang.RuntimeException: Primary Exception"

        Assertions.assertEquals(expectedString, primaryException.toSimpleString(1))
    }

    @Test
    @DisplayName("formats_exception_to_string_without_cause_and_high_enough_limit[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259344#]")
    fun formats_exception_to_string_without_cause_and_high_enough_limit() {
        val primaryException = RuntimeException("Primary Exception")

        val expectedString = "java.lang.RuntimeException: Primary Exception"

        Assertions.assertEquals(expectedString, primaryException.toSimpleString())
    }
}