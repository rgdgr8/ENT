package technology.cariad.entertainment.metadataservice.service

import android.net.Uri
import org.junit.Assert
import org.junit.Test

class AccessManagerAndroidTest {

    @Test
    fun uriPermission_shouldGrantPermissionForValidUri() {

        val expectedUri = Uri.parse("content://de.test.Provider/image.jpg")
        AccessManager.grantUriPermission(1, "content://de.test.Provider/image.jpg")

        val allowed = AccessManager.checkUriPermission(1, expectedUri)
        Assert.assertEquals(true, allowed)
    }

    @Test
    fun uriPermission_shouldDoNothingForUriWithoutPath() {

        AccessManager.grantUriPermission(1, "not://valid.uri")

        val allowed = AccessManager.checkUriPermission(1, Uri.parse("not://valid.uri"))
        Assert.assertEquals(false, allowed)
    }

    @Test
    fun uriPermission_shouldDoNothingForEmptyUri() {

        AccessManager.grantUriPermission(1, "")

        val allowed = AccessManager.checkUriPermission(1, Uri.EMPTY)
        Assert.assertEquals(false, allowed)
    }
}

