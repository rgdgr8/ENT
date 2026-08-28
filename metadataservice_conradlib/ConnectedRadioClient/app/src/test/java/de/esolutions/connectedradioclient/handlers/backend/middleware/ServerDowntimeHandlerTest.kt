package de.esolutions.connectedradioclient.handlers.backend.middleware

import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ServerDowntimeHandlerTest {

    @AfterEach
    internal fun tearDown() {
        ServerDowntimeHandler.serverDowntime = null
    }

    @Test
    @DisplayName("get_remaining_downtime_returns_null_if_downtime_not_set[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637#]")
    fun get_remaining_downtime_returns_null_if_downtime_not_set() {
        Assertions.assertNull(ServerDowntimeHandler.getRemainingDowntime())
    }

    @Test
    @DisplayName("handle_response_no_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun handle_response_no_retry_after() {
        ServerDowntimeHandler.handleResponse(null)
        Assertions.assertNull(ServerDowntimeHandler.getRemainingDowntime())
    }

    @Test
    @DisplayName("handle_response_with_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun handle_response_with_retry_after() {
        ServerDowntimeHandler.handleResponse(123)
        Assertions.assertNotNull(ServerDowntimeHandler.getRemainingDowntime())
    }

    @Test
    @DisplayName("get_remaining_downtime_is_calculating_correct[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637#]")
    fun get_remaining_downtime_is_calculating_correct() {
        val now = Calendar.getInstance()

        val downtime = Calendar.getInstance()
        downtime.timeInMillis = now.timeInMillis + 123 * 1000
        ServerDowntimeHandler.serverDowntime = downtime

        Mockito.mockStatic(Calendar::class.java).use {
            Mockito.`when`(Calendar.getInstance()).thenReturn(now)

            Assertions.assertEquals(123, ServerDowntimeHandler.getRemainingDowntime())
        }
    }


    @Test
    @DisplayName("get_remaining_downtime_returns_null_if_downtime_is_expired[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637#]")
    fun get_remaining_downtime_returns_null_if_downtime_is_expired() {
        val downtime = Calendar.getInstance()
        downtime.add(Calendar.MINUTE, -15)
        ServerDowntimeHandler.serverDowntime = downtime

        Assertions.assertNull(ServerDowntimeHandler.getRemainingDowntime())
    }


    @Test
    @DisplayName("set_sever_downtime_sets_the_timestamp_correct[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637#]")
    fun set_sever_downtime_sets_the_timestamp_correct() {
        val now = Calendar.getInstance()

        val downtime = Calendar.getInstance()
        downtime.timeInMillis = now.timeInMillis + 123 * 1000

        Mockito.mockStatic(Calendar::class.java).use {
            Mockito.`when`(Calendar.getInstance()).thenReturn(now)

            ServerDowntimeHandler.setServerDowntime(123)

            Assertions.assertEquals(
                downtime.timeInMillis,
                ServerDowntimeHandler.serverDowntime!!.timeInMillis
            )
        }
    }
}