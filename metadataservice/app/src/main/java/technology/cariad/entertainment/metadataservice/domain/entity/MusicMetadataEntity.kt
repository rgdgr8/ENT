package technology.cariad.entertainment.metadataservice.domain.entity

import technology.cariad.entertainment.metadataservice.domain.value.MusicMetadataErrorType

data class MusicMetadataEntity(
    val content: String? = null,
    val errorType: MusicMetadataErrorType = MusicMetadataErrorType.NONE,
    val statusCode: String? = null,
    val errorMessage: String? = null

) {
    constructor(content: String?) : this(content, MusicMetadataErrorType.NONE, null, null)

    constructor(
        errorType: MusicMetadataErrorType,
        statusCode: String?,
        errorMessage: String?
    ) : this(null, errorType, statusCode, errorMessage)
}

