package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.value.MusicMetadataErrorType
import technology.cariad.entertainment.metadataserviceclient.api.MetadataError

class ErrorTypeMapper : Mapper<MusicMetadataErrorType, Int>() {
    override fun mapValue(value: MusicMetadataErrorType): Int =
        when (value) {
            MusicMetadataErrorType.NONE ->
                MetadataError.NONE

            MusicMetadataErrorType.ERROR_TYPE_IOEXCEPTION ->
                MetadataError.ERROR_TYPE_IOEXCEPTION

            MusicMetadataErrorType.ERROR_TYPE_UNEXPECTED_STATUS_CODE ->
                MetadataError.ERROR_TYPE_UNEXPECTED_STATUS_CODE

            MusicMetadataErrorType.ERROR_TYPE_TOKEN_RETRIEVAL ->
                MetadataError.ERROR_TYPE_TOKEN_RETRIEVAL

            MusicMetadataErrorType.ERROR_TYPE_EMPTY_RESPONSE ->
                MetadataError.ERROR_TYPE_EMPTY_RESPONSE
        }
}

