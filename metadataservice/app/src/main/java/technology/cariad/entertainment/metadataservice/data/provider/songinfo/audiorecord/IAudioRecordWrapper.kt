package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecord

interface IAudioRecordWrapper {
    val readBufferSizeBytes: Int

    /**
     * Initializes the audio record wrapper.
     * @return true if successful, otherwise false
     */
    fun initialize(): Boolean

    /**
     * Starts recording audio data.
     * @return true if successful, otherwise false
     */
    fun startRecording(): Boolean

    /**
     * Reads audio data from the audio hardware for recording into a byte array.
     * @param buffer the array to which the recorded audio data is written
     */
    fun readBytes(buffer: ByteArray): Int

    /**
     * Stops recording audio data.
     */
    fun stopRecording()
}
