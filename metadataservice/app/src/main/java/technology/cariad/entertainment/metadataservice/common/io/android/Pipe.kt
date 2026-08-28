package technology.cariad.entertainment.metadataservice.common.io.android

import android.os.ParcelFileDescriptor
import technology.cariad.entertainment.metadataservice.common.io.IFileDescriptor
import technology.cariad.entertainment.metadataservice.common.io.IInputStream
import technology.cariad.entertainment.metadataservice.common.io.IOutputStream
import technology.cariad.entertainment.metadataservice.common.io.IPipe

class Pipe : IPipe {

    override val readFd: IFileDescriptor
        get() = _readFd
    private val _readFd: FileDescriptor

    override val writeFd: IFileDescriptor
        get() = _writeFd
    private val _writeFd: FileDescriptor

    init {
        ParcelFileDescriptor.createPipe().let {
            _readFd = FileDescriptor(it[0])
            _writeFd = FileDescriptor(it[1])
        }
    }

    override val readStream: IInputStream by lazy {
        InputStream(
            ParcelFileDescriptor.AutoCloseInputStream(
                _readFd.fd
            )
        )
    }
    override val writeStream: IOutputStream by lazy {
        OutputStream(
            ParcelFileDescriptor.AutoCloseOutputStream(
                _writeFd.fd
            )
        )
    }
}

