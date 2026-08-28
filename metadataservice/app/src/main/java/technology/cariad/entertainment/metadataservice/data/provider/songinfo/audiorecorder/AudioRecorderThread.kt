package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder

import technology.cariad.entertainment.metadataservice.common.io.IOutputStream
import technology.cariad.entertainment.metadataservice.common.log.logger

/**
 * @note: This class is not thread safe.
 */
class AudioRecorderThread(private val audioRecorder: IAudioRecorder) : IAudioRecorderThread {

    private val log by logger()

    private var recordingThread: Thread? = null
    private var isRunning = false

    /**
     * @see [IAudioRecorderThread.start]
     */
    override fun start(sink: IOutputStream) {
        if (isRunning) {
            log.warn(this::start.name) { "thread is already running" }
            return
        }

        log.info(this::start.name) { "starting thread" }

        recordingThread = Thread({
            audioRecorder.recordAudio(sink)
        }, "AudioRecorder")
            .apply { start() }

        isRunning = true
    }

    /**
     * @see [IAudioRecorderThread.stop]
     */
    override fun stop() {
        if (!isRunning) {
            log.warn(this::stop.name) { "thread is already stopped" }
            return
        }

        log.info(this::stop.name) { "stopping thread" }

        audioRecorder.stopRecording()

        recordingThread?.join()
        recordingThread = null

        isRunning = false
    }
}

