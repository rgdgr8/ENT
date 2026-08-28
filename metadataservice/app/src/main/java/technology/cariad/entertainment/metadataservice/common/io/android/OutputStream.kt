package technology.cariad.entertainment.metadataservice.common.io.android

import technology.cariad.entertainment.metadataservice.common.io.IOutputStream
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.io.FileOutputStream
import java.io.IOException

class OutputStream(private val stream: FileOutputStream) : IOutputStream {

    private val log by logger()

    /**
     * @see [IOutputStream.write]
     */
    override fun write(bytes: ByteArray, count: Int) {
        stream.write(bytes, 0, count)
    }

    /**
     * @see [Closeable.close]
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

