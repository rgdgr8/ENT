package de.esolutions.connectedradioclient.handlers.coreservices

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerFuture
import android.content.Context
import android.os.Bundle
import de.esolutions.connectedradioclient.data.AccountManagerConfig
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.helper.ModifierAccessHacker
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.DisplayName
import org.mockito.MockedStatic
import org.mockito.Mockito
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class TokenHandlerTest {

    companion object {

        lateinit var mockContext: Context
        var mockAccountManagerStatic: MockedStatic<AccountManager>? = null
        lateinit var mockAccountManager: AccountManager
        var mockAccount: Account? = null

        lateinit var instanceField: Field
        lateinit var modifiersField: Field

        @BeforeAll
        @JvmStatic
        fun setUp() {
            mockContext = Mockito.mock(Context::class.java)
            mockAccountManager = Mockito.mock(AccountManager::class.java)
            mockAccountManagerStatic = Mockito.mockStatic(AccountManager::class.java)
            mockAccount = Mockito.mock(Account::class.java)

            // Make field "name" of class Account accessible
            instanceField = Account::class.java.getDeclaredField("name")
            modifiersField = ModifierAccessHacker.modifier
            modifiersField.isAccessible = true
            modifiersField.setInt(instanceField, instanceField.modifiers.and(Modifier.FINAL.inv()))
            instanceField.isAccessible = true

            TokenHandler.accountManagerConfig = AccountManagerConfig(
                "serviceId",
                "accountType",
                "accountName",
                "optionServiceId",
                "optionOperationId"
            )
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            mockAccountManagerStatic?.close()

            // Reset field "name" of class Account
            modifiersField.setInt(instanceField, instanceField.modifiers.and(Modifier.FINAL.inv()))
            modifiersField.isAccessible = false
            instanceField.isAccessible = false
        }
    }

    @AfterEach
    fun reset() {
        Mockito.reset(mockContext, mockAccountManager, mockAccount)
        TokenHandler.debugToken = null
    }

    @Test
    fun get_token_returns_debug_token() {
        TokenHandler.debugToken = "MyToken"
        val tokenHandler = TokenHandler(mockContext)

        Assertions.assertEquals("MyToken", tokenHandler.getToken("operationId"))
    }

    @Test
    fun get_token_returns_null_if_no_accounts_available() {
        // Get the account manager by context.
        Mockito.`when`(AccountManager.get(Mockito.same(mockContext)))
            .thenReturn(mockAccountManager)

        // Get the accounts by configured type.
        Mockito.`when`(mockAccountManager.getAccountsByType("accountType"))
            .thenReturn(arrayOf())

        Assertions.assertNull(TokenHandler(mockContext).getToken("operationId"))
    }

    @Test
    fun get_token_returns_null_if_account_name_not_exists() {
        // Get the account manager by context.
        Mockito.`when`(AccountManager.get(Mockito.same(mockContext)))
            .thenReturn(mockAccountManager)

        // Get the accounts by configured type.
        Mockito.`when`(mockAccountManager.getAccountsByType("accountType"))
            .thenReturn(arrayOf(mockAccount))

        instanceField.set(mockAccount, "::wrong_account_name::")

        Assertions.assertNull(TokenHandler(mockContext).getToken("operationId"))
    }

    @Test
    @DisplayName("get_token_retrieving_auth_token_by_configured_settings[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256035#]")
    fun get_token_uses_account_manager_by_configured_settings() {
        // Get the account manager by context.
        Mockito.`when`(AccountManager.get(Mockito.same(mockContext)))
            .thenReturn(mockAccountManager)

        // Get the accounts by configured type.
        Mockito.`when`(mockAccountManager.getAccountsByType("accountType"))
            .thenReturn(arrayOf(mockAccount))

        // Find the mock account from the list by this name, which is configured in the config.
        instanceField.set(mockAccount, "accountName")

        // Android classes are not initializable in unit tests.
        val requestBundleConstruction = Mockito.mockConstruction(Bundle::class.java)

        // Bundle with token which should get returned by AccountManagerFuture.
        val resultBundle = Mockito.mock(Bundle::class.java)
        Mockito.`when`(resultBundle.getString(AccountManager.KEY_AUTHTOKEN)).thenReturn("::token::")

        // Result returned by AccountManager.getAuthToken().
        val accountManagerFuture = Mockito.mock(AccountManagerFuture::class.java)
        Mockito.`when`(accountManagerFuture.result).thenReturn(resultBundle)

        // Receive the AccountManagerFuture with Token-Bundle from AccountManager.getAuthToken().
        @Suppress("UNCHECKED_CAST")
        Mockito.`when`(
            mockAccountManager.getAuthToken(
                Mockito.eq(mockAccount),
                Mockito.eq(""),
                Mockito.any(Bundle::class.java),
                Mockito.eq(true),
                Mockito.eq(null),
                Mockito.eq(null),
            )
        ).thenReturn(accountManagerFuture as AccountManagerFuture<Bundle>)

        // Now request the token and check it.
        val actualToken = TokenHandler(mockContext).getToken("operationId")
        Assertions.assertEquals("::token::", actualToken)

        // Check if the account manager was filled with the configured values.
        val requestBundle = requestBundleConstruction.constructed().first()
        Mockito.verify(requestBundle).putString("optionServiceId", "serviceId")
        Mockito.verify(requestBundle).putString("optionOperationId", "operationId")

        Mockito.verify(mockAccountManager).getAccountsByType("accountType")
    }
}