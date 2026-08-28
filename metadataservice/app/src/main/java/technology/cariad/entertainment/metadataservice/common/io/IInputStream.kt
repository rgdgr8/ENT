package technology.cariad.entertainment.metadataservice.common.io

import java.io.Closeable

/**
 * Interface for partial wrapper of a lower level input stream.
 * Only defines the functionality currently required and may be extended in the future.
 */
interface IInputStream : Closeable {

    /**
     * Reads up to length of the [ByteArray] bytes of data from this input stream.
     * @param bytes Buffer to which the data is read
     * @return the number of bytes that have been read, or -1 if no more data is available
     * @exception IOException if an I/O error occurs
     */
    fun read(bytes: ByteArray): Int
}

