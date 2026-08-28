package de.esolutions.connectedradioclient.data.extensions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StringExtensionsTest {

    @Test
    fun add_correct_function_to_string_and_replace_correct() {
        var testString = "$(one) $(two), $(two)t again!"

        testString = testString.replaceEsoUrlPlaceholder("one", "oh")
        Assertions.assertEquals("oh $(two), $(two)t again!", testString)

        testString = testString.replaceEsoUrlPlaceholder("two", "no")
        Assertions.assertEquals("oh no, not again!", testString)
    }

    @Test
    @DisplayName("build_url_with_duplicates_slashes[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256200#]")
    fun build_url_with_duplicates_slashes() {
        val url = "https://domain.de/".buildUrl("/url/url")

        Assertions.assertEquals("https://domain.de/url/url", url.toString())
    }

    @Test
    @DisplayName("build_url_with_domain_slash[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256200#]")
    fun build_url_with_domain_slash() {
        val url = "https://domain.de/".buildUrl("url/url")

        Assertions.assertEquals("https://domain.de/url/url", url.toString())
    }

    @Test
    @DisplayName("build_url_with_url_slash[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256200#]")
    fun build_url_with_url_slash() {
        val url = "https://domain.de".buildUrl("/url/url")

        Assertions.assertEquals("https://domain.de/url/url", url.toString())
    }

    @Test
    @DisplayName("build_url_without_slash[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256200#]")
    fun build_url_without_slash() {
        val url = "https://domain.de".buildUrl("url/url")

        Assertions.assertEquals("https://domain.de/url/url", url.toString())
    }

    @Test
    @DisplayName("build_url_trows_exception[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256200#]")
    fun build_url_trows_exception() {
        assertThrows<IllegalArgumentException> {
            "invalid_domain".buildUrl("path")
        }
    }
}