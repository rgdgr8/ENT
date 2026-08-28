package de.esolutions.connectedradioclient.irc

enum class IRCEntry(val entry: String) {
    VERSION("ConRadLibVersion"),
    EXCEPTIONS("ConRadLibExceptions"),
    LOGO("ConRadLibErrorLogo"),
    LOOKUP("ConRadLibErrorLookup"),
    REGISTRATION("ConRadLibErrorRegistration")
}