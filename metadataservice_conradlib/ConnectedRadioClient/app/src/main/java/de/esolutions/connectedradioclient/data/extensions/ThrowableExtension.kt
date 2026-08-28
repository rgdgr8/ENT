package de.esolutions.connectedradioclient.data.extensions


/**
 * Converts throwable to string with class and message information.
 * Recursively prints caused exceptions seperated with a line break.
 */
fun Throwable.toSimpleString(limit: Int = 16): String {
    val throwableString = "${this.javaClass.name}: ${this.message}"

    this.cause?.let {
        if (limit <= 1) {
            return "$throwableString\nand more..."
        }

        val causedByString = "Caused by ${it.toSimpleString(limit - 1)}"
        return "$throwableString\n$causedByString"
    }

    return throwableString
}