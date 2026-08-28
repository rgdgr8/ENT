package technology.cariad.entertainment.metadataservice.domain.value

enum class MusicMetadataErrorType {
    NONE,
    ERROR_TYPE_IOEXCEPTION,
    ERROR_TYPE_UNEXPECTED_STATUS_CODE,
    ERROR_TYPE_TOKEN_RETRIEVAL,
    ERROR_TYPE_EMPTY_RESPONSE
}

