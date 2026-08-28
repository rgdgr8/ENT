package de.esolutions.connectedradioclient.handlers.backend.exceptions

class RegistrationFailureException(val responseCode: Int?, val retryAfter: Long?) :
    Exception("Backend registration failed")