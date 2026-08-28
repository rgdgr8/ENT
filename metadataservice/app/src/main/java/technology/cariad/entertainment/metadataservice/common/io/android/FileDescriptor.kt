package technology.cariad.entertainment.metadataservice.common.io.android

import android.os.ParcelFileDescriptor
import technology.cariad.entertainment.metadataservice.common.io.IFileDescriptor

internal class FileDescriptor(val fd: ParcelFileDescriptor) : IFileDescriptor {

    override fun close() {
        fd.close()
    }

    override fun detach(): Int {
        return fd.detachFd()
    }
}

