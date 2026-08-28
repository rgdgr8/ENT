package de.esolutions.connectedradioclient.handlers.backend

import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.helper.TestRadioStationDataCallback
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.lang.reflect.Field

class LookupRequestContainerTest {

    private lateinit var privateRequestContainer: Field
    private lateinit var privateMcc: Field
    private lateinit var privateIsLocked: Field
    private lateinit var lookupRequestContainer: LookupRequestContainer

    @BeforeEach
    fun before() {
        val mockHandleRegistration = mock(HandleRegistration::class.java)
        val mockTokenHandler = mock(TokenHandler::class.java)
        lookupRequestContainer = LookupRequestContainer(mockHandleRegistration, mockTokenHandler, "mcc")

        privateRequestContainer =
            lookupRequestContainer::class.java.getDeclaredField("requestContainer")
        privateRequestContainer.isAccessible = true
        privateMcc = lookupRequestContainer::class.java.getDeclaredField("mcc")
        privateMcc.isAccessible = true
        privateIsLocked = lookupRequestContainer::class.java.getDeclaredField("isLocked")
        privateIsLocked.isAccessible = true

        //set config to test always with the same values
        TestHelpingFunctions.setDefaultConfig()
    }

    @Test
    fun hit_size_limit() {
        //List to save if a new element could added
        val addedList = mutableListOf<Boolean>()
        val eTags = ETags(null, null, null, null, null)

        //try to add 6 elements and save the return value
        for (i in 1..6) {
            val mediaRequestObject = MediaRequestObject(
                FmBearer("mcc", "$i", "$i"),
                eTags,
                TestRadioStationDataCallback()
            )
            val addAllowed = lookupRequestContainer.cancelDebounceJob()
            addedList.add(addAllowed)
            if (addAllowed) {
                lookupRequestContainer.addRequest(mediaRequestObject)
            }
            Thread.sleep(5L)
        }

        val requestContainer =
            privateRequestContainer.get(lookupRequestContainer) as MutableMap<*, *>
        val mcc = privateMcc.get(lookupRequestContainer) as String
        val isLocked = privateIsLocked.get(lookupRequestContainer) as Boolean

        assertNotNull(requestContainer)
        assertNotNull(mcc)
        assertTrue(requestContainer.size == 5)
        assertEquals("mcc", mcc)
        assertTrue(isLocked)
        assertArrayEquals(arrayOf(true, true, true, true, true, false), addedList.toTypedArray())
    }

    @Test
    fun hit_total_time_limit() {
        //List to save if a new element could added
        val addedList = mutableListOf<Boolean>()

        val eTags = ETags(null, null, null, null, null)

        //try to add 7 elements and save the return value
        for (i in 1..7) {
            val mediaRequestObject = MediaRequestObject(
                FmBearer("mcc", "$i", "$i"),
                eTags,
                TestRadioStationDataCallback()
            )
            val addAllowed = lookupRequestContainer.cancelDebounceJob()
            addedList.add(addAllowed)
            if (addAllowed) {
                lookupRequestContainer.addRequest(mediaRequestObject)
            }
            //delay for the debounce is 500 milliseconds
            //total time max is 2 seconds
            // => 2000 milliseconds / 450 = 4,44
            //expect 5 elements in the request body
            Thread.sleep(450L)
        }

        val requestContainer =
            privateRequestContainer.get(lookupRequestContainer) as MutableMap<*, *>
        val mcc = privateMcc.get(lookupRequestContainer) as String
        val isLocked = privateIsLocked.get(lookupRequestContainer) as Boolean

        assertNotNull(requestContainer)
        assertNotNull(mcc)
        // time isn't always the same sometimes only 4 elements in the request, sometimes 6.
        assertTrue(requestContainer.size >= 4)
        assertTrue(requestContainer.size <= 6)
        assertEquals("mcc", mcc)
        assertTrue(isLocked)
        assertTrue(addedList.contains(false))
    }

    @Test
    fun hit_debounce_time_limit() {
        //List to save if a new element could be added.
        val addedList = mutableListOf<Boolean>()

        val eTags = ETags(null, null, null, null, null)

        //try to add 2 elements and save the return value
        for (i in 1..2) {
            val mediaRequestObject = MediaRequestObject(
                FmBearer("mcc", "$i", "$i"),
                eTags,
                TestRadioStationDataCallback()
            )
            val addAllowed = lookupRequestContainer.cancelDebounceJob()
            addedList.add(addAllowed)
            if (addAllowed) {
                lookupRequestContainer.addRequest(mediaRequestObject)
            }
            //delay for the debounce is 500 milliseconds
            //expect 1 elements in the request body
            Thread.sleep(750L)
        }

        val requestContainer =
            privateRequestContainer.get(lookupRequestContainer) as MutableMap<*, *>
        val mcc = privateMcc.get(lookupRequestContainer) as String
        val isLocked = privateIsLocked.get(lookupRequestContainer) as Boolean

        // time isn't always the same sometimes only 4 elements in the request, sometimes 6.
        assertEquals(1, requestContainer.size)
        assertEquals("mcc", mcc)
        assertTrue(isLocked)
        assertArrayEquals(arrayOf(true, false), addedList.toTypedArray())
    }
}
