package de.esolutions.connectedradioclient.logger

enum class LoggerTags(private val tag: String) {
    PARSING("#parsing"),
    JSON("#json"),
    COOKIE("#cookie"),
    TOKEN("#token"),
    REQUEST("#request"),
    RESPONSE("#response"),
    LOGO("#logo"),
    LOOKUP("#lookup"),
    REGISTRATION("#registration"),
    MEDIA("#media"),
    NETWORK("#network"),
    EXECUTOR("#executor"),
    CONFIG("#config");

    override fun toString(): String {
        return tag
    }
}