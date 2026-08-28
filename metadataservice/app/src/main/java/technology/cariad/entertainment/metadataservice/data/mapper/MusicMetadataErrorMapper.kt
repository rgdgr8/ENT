package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.gmdlib.data.GmdErrorType
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.value.MusicMetadataErrorType

class MusicMetadataErrorMapper : Mapper<GmdErrorType, MusicMetadataErrorType>() {

    override fun mapValue(value: GmdErrorType): MusicMetadataErrorType {
        return when (value) {
            GmdErrorType.EMPTY_RESPONSE ->
                MusicMetadataErrorType.ERROR_TYPE_EMPTY_RESPONSE

            GmdErrorType.IOException ->
                MusicMetadataErrorType.ERROR_TYPE_IOEXCEPTION

            GmdErrorType.UNEXPECTED_STATUSCODE ->
                MusicMetadataErrorType.ERROR_TYPE_UNEXPECTED_STATUS_CODE

            GmdErrorType.TOKEN_RETRIEVAL ->
                MusicMetadataErrorType.ERROR_TYPE_TOKEN_RETRIEVAL
        }
    }
}

