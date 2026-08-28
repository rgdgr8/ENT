package technology.cariad.entertainment.metadataservice.common.io

/**
 * Interface for partial wrapper of a lower level pipe.
 * Only defines the functionality currently required and may be extended in the future.
 */
interface IPipe {
    val readStream: IInputStream
    val readFd: IFileDescriptor

    val writeFd: IFileDescriptor
    val writeStream: IOutputStream
}

