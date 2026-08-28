package de.esolutions.connectedradioclient.irc

enum class IRCMessage(val id: Int) {
    UNKNOWN(0),
    TOKEN(1),
    EMPTY_BODY(2),
    INVALID_RESPONSE(3)
}