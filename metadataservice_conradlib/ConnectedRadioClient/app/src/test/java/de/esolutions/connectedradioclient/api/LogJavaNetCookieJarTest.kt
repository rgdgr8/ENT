package de.esolutions.connectedradioclient.api

import okhttp3.Cookie
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.net.CookieHandler

class LogJavaNetCookieJarTest {

    private val cookieHandler: CookieHandler = Mockito.mock(CookieHandler::class.java)
    private val cookieJar = LogJavaNetCookieJar(cookieHandler)

    private fun buildCookie(key: String, value: String): Cookie {
        return Cookie.Builder().name(key).value(value).domain("domain").build();
    }

    @Test
    @DisplayName("cookie_from_a_list_will_be_found[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-251551#]")
    fun cookie_from_a_list_will_be_found() {
        val cookieA = buildCookie("A", "value")
        val cookieB = buildCookie("B", "value")
        val cookieC = buildCookie("C", "value")

        val cookieList = listOf(cookieA, cookieB, cookieC)

        val result = cookieJar.findCookie("B", cookieList)
        Assertions.assertEquals(cookieB, result)
    }

    @Test
    @DisplayName("find_cookie_returns_null_on_missing[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-251551#]")
    fun find_cookie_returns_null_on_missing() {
        val result = cookieJar.findCookie("B", listOf())
        Assertions.assertNull(result)
    }

    @Test
    @DisplayName("it_should_log_if_the_cookie_was_not_set[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-251551#]")
    fun it_should_log_if_the_cookie_was_not_set() {
        val cookieA = buildCookie("cookie", "A")

        val result = cookieJar.shouldLogCookieChange(cookieA, null)
        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("it_should_log_if_the_cookie_value_changed[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-251551#]")
    fun it_should_log_if_the_cookie_value_changed() {
        val cookieA = buildCookie("cookie", "A")
        val cookieB = buildCookie("cookie", "B")

        val result = cookieJar.shouldLogCookieChange(cookieA, cookieB)
        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("it_should_not_log_if_the_cookie_value_is_unchanged[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-251551#]")
    fun it_should_not_log_if_the_cookie_value_is_unchanged() {
        val cookie1 = buildCookie("cookie", "A")
        val cookie2 = buildCookie("cookie", "A")

        val result = cookieJar.shouldLogCookieChange(cookie1, cookie2)
        Assertions.assertFalse(result)
    }
}