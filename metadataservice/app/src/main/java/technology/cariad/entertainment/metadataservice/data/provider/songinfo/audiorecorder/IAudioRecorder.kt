package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder

import technology.cariad.entertainment.metadataservice.common.io.IOutputStream

interface IAudioRecorder {

    /**
     * Starts recording audio data.
     * @param sink Output stream the audio data is written to
     */
    fun recordAudio(sink: IOutputStream)

    /**
     * Stops recording audio data.
     */
    fun stopRecording()
}
