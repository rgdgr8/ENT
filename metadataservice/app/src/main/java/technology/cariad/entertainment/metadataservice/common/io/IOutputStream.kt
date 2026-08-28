package technology.cariad.entertainment.metadataservice.common.io

import java.io.Closeable

/**
 * Interface for partial wrapper of a lower level output stream.
 * Only defines the functionality currently required and may be extended in the future.
 */
interface IOutputStream : Closeable {

    /**
     * Writes [count] bytes from the specified byte array to the output stream.
     * @param bytes Data that should be written
     * @param count Number of bytes to write
     */
    fun write(bytes: ByteArray, count: Int)
}

