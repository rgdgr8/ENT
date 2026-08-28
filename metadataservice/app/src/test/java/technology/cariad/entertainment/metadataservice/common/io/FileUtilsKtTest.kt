package technology.cariad.entertainment.metadataservice.common.io

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream

private val TEST_HASH = byteArrayOf(
    -86, -1, 74, 69, 10, 16, 76, -47, 119, -46,
    -115, 24, -41, 68, -123, -24, -54, -32, 116, -73
)
private val EMPTY_HASH = byteArrayOf(
    -38, 57, -93, -18, 94, 107, 75, 13, 50, 85,
    -65, -17, -107, 96, 24, -112, -81, -40, 7, 9
)

class FileUtilsKtTest {

    @Test
    fun calcFileHash_shouldReturnHash() {
        val inputStream = ByteArrayInputStream("input data".toByteArray())

        val hash = calcFileHash(inputStream)

        Assert.assertArrayEquals(TEST_HASH, hash)
    }

    @Test
    fun calcFileHash_shouldReturnHashForEmptyInput() {
        val inputStream = ByteArrayInputStream(ByteArray(0))

        val hash = calcFileHash(inputStream)

        Assert.assertArrayEquals(EMPTY_HASH, hash)
    }

    @Test
    fun calcFileHash_shouldReturnSameHashForSameInput() {
        val inputStreamOne = ByteArrayInputStream("input data".toByteArray())
        val inputStreamTwo = ByteArrayInputStream("input data".toByteArray())

        val hashOne = calcFileHash(inputStreamOne)
        val hashTwo = calcFileHash(inputStreamTwo)

        Assert.assertArrayEquals(hashOne, hashTwo)
    }

    @Test
    fun calcFileHash_shouldReturnDifferentHashForDifferentInput() {
        val inputStreamOne = ByteArrayInputStream("input data".toByteArray())
        val inputStreamTwo = ByteArrayInputStream("inputdata".toByteArray())

        val hashOne = calcFileHash(inputStreamOne)
        val hashTwo = calcFileHash(inputStreamTwo)

        Assert.assertFalse(hashOne.contentEquals(hashTwo))
    }
}

