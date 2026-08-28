package technology.cariad.entertainment.metadataservice.common.io.android

import technology.cariad.entertainment.metadataservice.common.io.IInputStream
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.io.FileInputStream
import java.io.IOException

internal class InputStream(private val stream: FileInputStream) : IInputStream {

    private val log by logger()

    /**
     * @see [IInputStream.read]
     */
    override fun read(bytes: ByteArray): Int {
        return stream.read(bytes)
    }

    /**
     * @see [FileInputStream.close]
     */
    override fun close() {
        try {
            stream.close()
        } catch (exception: IOException) {
            log.error(this::close.name) { "failed to close the stream. ${exception.message}" }
            IRCLogger.appGenericError(
                "failed to close the stream.",
                "${exception.message}"
            )
        }
    }
}

