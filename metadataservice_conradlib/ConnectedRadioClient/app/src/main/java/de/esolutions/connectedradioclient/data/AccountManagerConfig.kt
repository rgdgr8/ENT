package de.esolutions.connectedradioclient.data

data class AccountManagerConfig(
    val serviceId: String,
    val accountType: String,
    val accountName: String,
    val optionServiceId: String,
    val optionOperationId: String
)
