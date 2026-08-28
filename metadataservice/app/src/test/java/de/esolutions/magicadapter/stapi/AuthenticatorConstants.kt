package de.esolutions.magicadapter.stapi

import android.accounts.Account
import android.os.Bundle
import android.os.RemoteException

object AuthenticatorConstants {
    const val BUNDLE_OPTIONS_TRANSACTIONTYPEID = "test_trans_id"
    const val BUNDLE_OPTIONS_OPERATIONTYPEID = "test_op_id"
    const val BUNDLE_OPTIONS_SERVICEID = "test_svc_id"
    const val BUNDLE_OPTIONS_OPERATIONID = "test_op_id"
    const val CORESERVICE_ACCOUNT_TYPE = "test_acc_type"
    const val CORESERVICE_ACCOUNT_NAME = "test_acc_name"
    const val BUNDLE_OPTIONS_OPERATIONID_DEFAULT = "test_default_op"

    @Throws(RemoteException::class)
    fun getTokenRequestBundle(serviceId: String, operationId: String, account: Account): Bundle? {
        return Bundle()
    }
}
