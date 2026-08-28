package technology.cariad.entertainment.metadataservice.mock.android

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

/**
 * Mocks Android Uri parse function to return a mocked Uri.
 * Instantiate either during setup or inside of test case.
 * It is not necessary to keep a reference to the object.
 */
class UriMock {

    companion object {
        private var uri: Uri = mockk()
    }

    init {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns uri
    }
}
