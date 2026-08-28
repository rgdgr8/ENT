package de.esolutions.connectedradioclient.api

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SystemPropertyReaderTest {

    @Test
    @DisplayName("it_can_read_the_brand_from_the_system_properties[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-244019#]")
    fun it_can_read_the_brand_from_the_system_properties() {
        val result = SystemPropertyReader.getProduct()

        assertTrue(result.isNotEmpty())
        assertNotEquals("unknown", result)
    }

    @Test
    @DisplayName("it_can_read_the_product_from_the_system_properties[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-244019#]")
    fun it_can_read_the_product_from_the_system_properties() {
        val result = SystemPropertyReader.getBrand()

        assertTrue(result.isNotEmpty())
        assertNotEquals("unknown", result)
    }

    @Test
    @DisplayName("it_can_read_the_cluster_from_the_system_properties[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-244019#]")
    fun it_can_read_the_cluster_from_the_system_properties() {
        val result = SystemPropertyReader.getCluster()

        assertTrue(result.isNotEmpty())
        assertNotEquals("unknown", result)
    }
}