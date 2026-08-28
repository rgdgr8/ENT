package de.esolutions.connectedradioclient.data.bearer

data class FmBearer(
    val mcc: String,
    val pi: String,
    val frequency: String
) : IBearer {
    override fun getBearerString(): String {
        return "fm:?.$pi.$frequency"
    }

    companion object {
        /**
         * Create the [FmBearer] object with the given [mcc] and [bearerString].
         * Return null if something is wrong with the string.
         */
        fun createFromString(mcc: String, bearerString: String): FmBearer? {
            val splitBearerString = bearerString.split(".")
            return if (splitBearerString.size == 3) {
                // Backend send bearer as string fm:gcc.pi.frequency
                // we don't need gcc anymore because hmi ignore this parameter.
                // media use it as cache key with ecc and hmi send it without => cache is not working
                // now the cache works as expected
                FmBearer(
                    mcc,
                    splitBearerString[1],
                    splitBearerString[2]
                )
            } else {
                null
            }
        }
    }
}