package technology.cariad.entertainment.metadataservice.data.provider.gracenote

enum class GnRecognitionStatus(val value: Int) {
    UNKNOWN(0),
    STOPPED(1),
    RUNNING(2),
    ERROR(3);

    companion object {
        /**
         * Returns the enum value for the given value.
         */
        fun getByValue(value: Int) = values().firstOrNull { it.value == value } ?: UNKNOWN
    }
}

