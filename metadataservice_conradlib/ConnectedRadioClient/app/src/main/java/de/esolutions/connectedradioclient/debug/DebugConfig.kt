package de.esolutions.connectedradioclient.debug

class DebugConfig {
    var maxDelay = 1
    var minDelay = 0
    var sendError = 0.0
    var sendSLogo = 1.0
    var sendLogo = 1.0
    var sendMetadata = 1.0
    var sendWebStreams = 1.0
    var sendBearerList = 1.0
    var deleteSLogo = false
    var deleteLogo = false
    var deleteMetadata = false
    var deleteWebStreams = false
    var deleteBearerList = false
    var expireInSeconds = 86400L // 24h * 60m * 60s => one day in secunds
}