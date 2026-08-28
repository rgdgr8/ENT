package technology.cariad.entertainment.metadataservice.common.io

/**
 * Interface for partial wrapper of a lower level file descriptor.
 * Only defines the functionality currently required and may be extended in the future.
 */
interface IFileDescriptor {

    /**
     * Closes the FileDescriptor.
     */
    fun close()

    /**
     * Detaches the FileDescriptor from the object.
     * FileDescriptor must be closed after usage.
     * @return the native FileDescriptor int
     */
    fun detach(): Int
}

