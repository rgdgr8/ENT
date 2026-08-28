package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder

import technology.cariad.entertainment.metadataservice.common.io.IOutputStream

interface IAudioRecorderThread {

    /**
     * Starts the audio recorder thread.
     * It is not allowed to start an already started thread.
     * @param sink Output stream the audio data is written to
     */
    fun start(sink: IOutputStream)

    /**
     * Stops the audio recorder thread.
     * It is not allowed to stop when thread is not started.
     */
    fun stop()
}

