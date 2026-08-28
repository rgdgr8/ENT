package technology.cariad.entertainment.metadataservice.common.io

import technology.cariad.entertainment.metadataservice.common.io.android.Pipe

interface IPipeFactory {
    companion object {

        /**
         * Creates a new pipe.
         */
        fun createPipe(): IPipe {
            return Pipe()
        }
    }
}

