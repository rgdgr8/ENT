package de.esolutions.connectedradioclient.data.bearer

data class DabBearer(
    val mcc: String,
    val ecc: String,
    val eid: String,
    val sid: String,
    val scids: String
) : IBearer {
    override fun getBearerString(): String {
        return "dab:$ecc.$eid.$sid.$scids"
    }

    companion object {
        /**
         * Create the [DabBearer] object with the given [mcc] and [bearerString].
         * Return null if something is wrong with the string.
         */
        fun createFromString(mcc: String, bearerString: String): DabBearer? {
            val splitBearerString = bearerString.split(".")
            return if (splitBearerString.size == 4) {
                DabBearer(
                    mcc,
                    splitBearerString[0].substring(1),
                    splitBearerString[1],
                    splitBearerString[2],
                    splitBearerString[3]
                )
            } else {
                null
            }
        }
    }
}