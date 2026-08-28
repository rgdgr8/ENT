package de.esolutions.connectedradioclient.data.bearer

data class HdBearer(
    val tx: String,
    val cc: String,
    val multicastId: String
) : IBearer {
    override fun getBearerString(): String {
        return "hd:$cc.$tx.$multicastId"
    }

    companion object {
        /**
         * Create the [HdBearer] object with the given [bearerString].
         * Return null if something is wrong with the string.
         */
        fun createFromString(bearerString: String): HdBearer? {
            val splitBearerString = bearerString.split(".")
            return if (splitBearerString.size == 3) {
                HdBearer(
                    splitBearerString[1],
                    splitBearerString[0],
                    splitBearerString[2]
                )
            } else {
                null
            }
        }
    }
}