package technology.cariad.entertainment.metadataservice.domain.entity

data class ETagsEntity(
    val bearerListETag: String = "",
    val metadataETag: String = "",
    val streamsETag: String = "",
    val logoETag: String = "",
    val logoSmallETag: String = ""
) {
    override fun toString(): String =
        bearerListETag +
                ":$metadataETag" +
                ":$streamsETag" +
                ":$logoETag" +
                ":$logoSmallETag"

    companion object {

        /**
         * Constructs an ETagsEntity from a colon separated list of e-tags.
         */
        fun fromString(eTags: String): ETagsEntity {
            val eTagsList = eTags.split(":")
            return ETagsEntity(
                bearerListETag = eTagsList.elementAtOrElse(0) { "" },
                metadataETag = eTagsList.elementAtOrElse(1) { "" },
                streamsETag = eTagsList.elementAtOrElse(2) { "" },
                logoETag = eTagsList.elementAtOrElse(3) { "" },
                logoSmallETag = eTagsList.elementAtOrElse(4) { "" },
            )
        }
    }
}

