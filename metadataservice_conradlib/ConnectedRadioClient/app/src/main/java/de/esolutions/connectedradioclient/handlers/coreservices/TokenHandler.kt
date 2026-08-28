package de.esolutions.connectedradioclient.handlers.coreservices

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import de.esolutions.connectedradioclient.data.AccountManagerConfig
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags

class TokenHandler(private val context: Context) {

    companion object {
        lateinit var accountManagerConfig: AccountManagerConfig
        var debugToken: String? = null
    }

    /**
     * Return the saved token (only in testing and dev situation).
     * If the saved token is null try to get a token from the CoreServiceAccount.
     * Return null in case of error or if no token is available.
     */
    fun getToken(operationId: String): String? {
        debugToken?.let {
            Logger.d(
                PrivacyFlags.TOKEN,
                null,
                "Use DEV-Token:  ${Logger.trimToken(it)}",
                LoggerTags.TOKEN
            )
            return it
        }

        try {
            val accountManager = AccountManager.get(this.context)
            val csAccount = this.getCoreServiceAccount(accountManager) ?: return null
            val bundle = this.getAuthTokenBundle(accountManager, csAccount, operationId)

            return if (bundle.containsKey(AccountManager.KEY_ERROR_MESSAGE)) {
                val error = bundle.getString(AccountManager.KEY_ERROR_MESSAGE)
                Logger.w(
                    PrivacyFlags.SAFE,
                    null,
                    "Can't receive token from CoreServices.",
                    LoggerTags.TOKEN
                )
                Logger.w(PrivacyFlags.UNDECIDED, null, "Error: $error", LoggerTags.TOKEN)
                null
            } else {
                val token = bundle.getString(AccountManager.KEY_AUTHTOKEN)
                Logger.i(
                    PrivacyFlags.SAFE,
                    null,
                    "Receive token from CoreServices.",
                    LoggerTags.TOKEN
                )
                Logger.i(PrivacyFlags.TOKEN, null, "Token: ${Logger.trimToken(token)}", LoggerTags.TOKEN)
                token
            }
        } catch (e: Exception) {
            Logger.e(
                PrivacyFlags.SAFE,
                null,
                "Can't receive token from CoreServices.",
                LoggerTags.TOKEN
            )
            Logger.ex(
                PrivacyFlags.UNDECIDED,
                null,
                null,
                e,
                LoggerTags.TOKEN
            )
            return null
        }
    }

    /**
     * Return the CoreServiceAccount from the given [accountManager]
     */
    private fun getCoreServiceAccount(accountManager: AccountManager): Account? {
        Logger.d(PrivacyFlags.SAFE, null, "Try to request CSAccount", LoggerTags.TOKEN)
        val accounts = accountManager.getAccountsByType(accountManagerConfig.accountType)
        if (accounts.isEmpty()) {
            Logger.w(
                PrivacyFlags.SAFE,
                null,
                "Can't receive accounts from AccountManager",
                LoggerTags.TOKEN
            )
            return null
        }
        return accounts.find { account ->
            account.name.equals(accountManagerConfig.accountName)
        }
    }

    /**
     * Return the token bundle from the given [accountManager] and [account]
     */
    private fun getAuthTokenBundle(accountManager: AccountManager, account: Account, operationId: String): Bundle {
        Logger.d(PrivacyFlags.SAFE, null, "Try to request TokenBundle", LoggerTags.TOKEN)

        val options = Bundle()
        options.putString(accountManagerConfig.optionServiceId, accountManagerConfig.serviceId)
        options.putString(accountManagerConfig.optionOperationId, operationId)

        return accountManager.getAuthToken(
            account,
            "",
            options,
            true,
            null,
            null
        ).result
    }
}