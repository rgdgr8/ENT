package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecord

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioRecord.ERROR
import android.media.AudioRecord.ERROR_BAD_VALUE
import android.media.AudioRecord.ERROR_DEAD_OBJECT
import android.media.AudioRecord.ERROR_INVALID_OPERATION
import android.media.MediaRecorder
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder.RecordingConfig
import java.lang.reflect.Modifier

class AudioRecordWrapper(context: Context, private val recordingConfig: RecordingConfig) :
    IAudioRecordWrapper {

    companion object {
        private const val RADIO_TUNER_SOURCE_NAME = "RADIO_TUNER"
    }

    private val log by logger()

    private lateinit var record: AudioRecord
    private var activeAudioSourceName: String = "UNKNOWN"
    private var activeAudioSourceValue: Int = -1
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val channels = convertToAudioFormatChannel(recordingConfig.channels)
    private val encoding = convertToAudioFormatEncoding(recordingConfig.encoding)
    private val sampleRate = recordingConfig.sampleRate

    private val minBufferSizeBytes = AudioRecord.getMinBufferSize(
        sampleRate, channels, encoding
    )

    private val recordBufferSizeBytes = minBufferSizeBytes * 2
    override val readBufferSizeBytes = minBufferSizeBytes / 2

    /**
     * @see [IAudioRecordWrapper.initialize]
     */
    override fun initialize(): Boolean {
        log.info(this::initialize.name) {
            "audio capture parameters:\n" +
                "encoding=${recordingConfig.encoding}\n" +
                "sampleRate=${recordingConfig.sampleRate}\n" +
                "channels=${recordingConfig.channels}\n" +
                "inputBus=${recordingConfig.inputDeviceAddress} (Details werden unten geloggt)\n" +
                "minBufferSize=$minBufferSizeBytes\n" +
                "recordBufferSize=$recordBufferSizeBytes\n" +
                "readBufferSizeBytes=$readBufferSizeBytes"
        }

        val audioFormat = buildAudioFormat(encoding, channels, sampleRate) ?: return false
        record = buildAudioRecord(audioFormat, recordBufferSizeBytes) ?: return false
        val device = findDevice(audioManager, recordingConfig.inputDeviceAddress) ?: return false

        log.info(this::initialize.name) {
            "verwendetes AudioDevice für inputBus='${recordingConfig.inputDeviceAddress}':\n" +
            "  Name: ${device.productName}\n" +
            "  Address: ${device.address}\n" +
            "  Type: ${device.type}\n" +
            "  ID: ${device.id}\n" +
            "  isSource: ${device.isSource}\n" +
            "  isSink: ${device.isSink}"
        }

        val success = setPreferredDevice(record, device)
        if (!success) return false

        log.info(this::initialize.name) {
            "audio capture initialization erfolgreich, verwendeter AudioSource: " +
                "$activeAudioSourceName($activeAudioSourceValue)"
        }

        return true
    }

    /**
     * @see [IAudioRecordWrapper.startRecording]
     */
    override fun startRecording(): Boolean {
        try {
            record.startRecording()

            log.info(this::startRecording.name) {
                "audio recording successfully started"
            }
        } catch (exception: IllegalStateException) {
            log.error(this::startRecording.name) {
                "failed with exception: message=${exception.message}"
            }
            return false
        }
        return true
    }

    /**
     * @see [IAudioRecordWrapper.readBytes]
     */
    override fun readBytes(buffer: ByteArray): Int {
        val result = record.read(buffer, 0, buffer.size)

        if (result < 0) {
            log.error(this::readBytes.name) {
                "failed with error: ${
                    when (result) {
                        ERROR_INVALID_OPERATION -> "invalid operation"
                        ERROR_BAD_VALUE -> "bad value"
                        ERROR_DEAD_OBJECT -> "dead object"
                        ERROR -> "generic operation failure"
                        else -> "unknown"
                    }
                }"
            }
            return -1
        }
        return result
    }

    /**
     * @see [IAudioRecordWrapper.stopRecording]
     */
    override fun stopRecording() {
        try {
            record.stop()
            record.release()
            log.info(this::startRecording.name) {
                "audio recording successfully stopped"
            }
        } catch (exception: IllegalStateException) {
            log.error(this::stopRecording.name) {
                "failed with exception: message=${exception.message}"
            }
        }
    }

    private fun buildAudioFormat(encoding: Int, channels: Int, sampleRate: Int): AudioFormat? {
        return try {
            AudioFormat.Builder()
                .setEncoding(encoding)
                .setChannelMask(channels)
                .setSampleRate(sampleRate)
                .build()
        } catch (exception: IllegalArgumentException) {
            log.error(this::buildAudioRecord.name) {
                "building audio format  failed with exception: msg=${exception.message}"
            }
            null
        }
    }

    private fun buildAudioRecord(audioFormat: AudioFormat, bufferSizeBytes: Int): AudioRecord? {
        return try {
            val availableSources = listAvailableAudioSources()
            val orderedCandidates = buildList {
                availableSources.firstOrNull { it.first == RADIO_TUNER_SOURCE_NAME }?.let { add(it) }
                availableSources.forEach { source ->
                    if (source.first != RADIO_TUNER_SOURCE_NAME) {
                        add(source)
                    }
                }
            }.distinctBy { it.second }

            if (orderedCandidates.isEmpty()) {
                log.error(this::buildAudioRecord.name) {
                    "Keine AudioSources gefunden. Kann AudioRecord nicht initialisieren."
                }
                return null
            }

            orderedCandidates.firstNotNullOfOrNull { source ->
                val sourceName = source.first
                val sourceValue = source.second

                log.info(this::buildAudioRecord.name) {
                    "AudioRecord wird gebaut mit AudioSource=$sourceValue ($sourceName)"
                }

                try {
                    AudioRecord.Builder()
                        .setAudioFormat(audioFormat)
                        .setBufferSizeInBytes(bufferSizeBytes)
                        .setAudioSource(sourceValue)
                        .build()
                        .also {
                            activeAudioSourceName = sourceName
                            activeAudioSourceValue = sourceValue
                            if (sourceName != RADIO_TUNER_SOURCE_NAME) {
                                log.warn(this::buildAudioRecord.name) {
                                    "$RADIO_TUNER_SOURCE_NAME konnte nicht verwendet werden. Fallback auf $sourceName($sourceValue)."
                                }
                            }
                        }
                } catch (e: IllegalArgumentException) {
                    log.warn(this::buildAudioRecord.name) {
                        "Ungültige AudioSource ($sourceValue, $sourceName): ${e.message}"
                    }
                    null
                } catch (e: UnsupportedOperationException) {
                    log.warn(this::buildAudioRecord.name) {
                        "AudioSource nicht unterstützt ($sourceValue, $sourceName): ${e.message}"
                    }
                    null
                }
            }
        } catch (exception: UnsupportedOperationException) {
            log.error(this::buildAudioRecord.name) {
                "building audio record failed with exception: msg=${exception.message}"
            }
            null
        }
    }

    private fun listAvailableAudioSources(): List<Pair<String, Int>> {
        val sources = MediaRecorder.AudioSource::class.java.fields
            .filter { field ->
                Modifier.isStatic(field.modifiers) && field.type == Int::class.javaPrimitiveType
            }
            .mapNotNull { field ->
                try {
                    field.name to field.getInt(null)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.second }

        log.info(this::listAvailableAudioSources.name) {
            "verfügbare AudioSources:\n" +
                sources.joinToString(separator = "\n") { "  - ${it.first}=${it.second}" }
        }

        return sources
    }

    private fun findDevice(
        audioManager: AudioManager,
        inputDeviceAddress: String
    ): AudioDeviceInfo? {
        val log by logger(AudioRecordWrapper::class)

        val deviceList = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)

        if (deviceList.isEmpty()) {
            log.error(this::findDevice.name) { "no input devices present" }
            return null
        }

        return deviceList.find { it.address == inputDeviceAddress } ?: run {
            log.error(this::findDevice.name) {
                "audio input device not found: address=${inputDeviceAddress}"
            }
            null
        }
    }

    private fun setPreferredDevice(record: AudioRecord, deviceInfo: AudioDeviceInfo): Boolean {
        val success = record.setPreferredDevice(deviceInfo)
        if (!success) {
            val log by logger(AudioRecordWrapper::class)
            log.error(this::setPreferredDevice.name) {
                "device not found: address=${deviceInfo.address}"
            }
        }
        return success
    }
}

