package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder

import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.common.io.IOutputStream
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecord.IAudioRecordWrapper
import kotlin.math.abs

class AudioRecorder(
    private val audioRecord: IAudioRecordWrapper
) : IAudioRecorder {

    private val log by logger()

    @Volatile
    private var isStopping = false

    /**
     * @see [IAudioRecorder.recordAudio]
     */
    override fun recordAudio(sink: IOutputStream) {
        log.info(this::recordAudio.name) {
            "initialize audio record"
        }

        val success = audioRecord.initialize()
        if (!success) {
            log.error(this::recordAudio.name) {
                "audio recording initialization failed"
            }
            return
        }

        audioRecord.startRecording()

        log.info(this::recordAudio.name) {
            "start recording of audio data"
        }

        copyAudioData(sink)

        log.info(this::recordAudio.name) {
            "stopping recording of audio data"
        }

        audioRecord.stopRecording()

        log.info(this::recordAudio.name) {
            "stopped recording of audio data"
        }

        isStopping = false
    }

    /**
     * @see [IAudioRecorder.stopRecording]
     */
    override fun stopRecording() {
        log.info(this::stopRecording.name) { "stop recording of audio data" }
        isStopping = true
    }

    @Suppress("MagicNumber")
    private fun copyAudioData(sink: IOutputStream) {

        // -- Dev only --
        var startTime = System.currentTimeMillis()
        var statsCounter = 0
        var byteCounter = 0
        // -- Dev only --

        val buffer = ByteArray(audioRecord.readBufferSizeBytes)

        while (!isStopping) {
            val bufferReadResult = audioRecord.readBytes(buffer)
            sink.write(buffer, bufferReadResult)

            if (BuildConfig.FLAVOR == "dev") {
                statsCounter++
                byteCounter += bufferReadResult
                if (statsCounter % 250 == 0) {
                    val endTime = System.currentTimeMillis()
                    val secondsElapsed = ((endTime - startTime) / 1000.0)
                    val avgBytesPerSecond = (byteCounter / secondsElapsed) / 1024.0

                    val silenceIndicator = buffer.sumOf { abs(it.toInt()) } / buffer.size

                    log.debug(this::recordAudio.name) {
                        "copying audio stream - avg: ${"%.2f".format(avgBytesPerSecond)} kb/s " +
                                "over last ${"%.2f".format(secondsElapsed)} s - " +
                                "silence indicator: $silenceIndicator"
                    }

                    byteCounter = 0
                    startTime = System.currentTimeMillis()
                }
            }
        }
    }
}

