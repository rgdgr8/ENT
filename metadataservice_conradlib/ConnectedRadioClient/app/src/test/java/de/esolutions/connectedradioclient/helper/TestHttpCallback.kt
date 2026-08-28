package de.esolutions.connectedradioclient.helper

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch

class TestHttpCallback(
    private val latch: CountDownLatch? = null
) : Callback {

    companion object {
        const val ERROR = 0x1 //0001
        const val RESPONSE = 0x2 //0010
    }

    var response: Response? = null
    var calledFunktions: Int = 0x00

    override fun onFailure(call: Call, e: IOException) {
        calledFunktions = calledFunktions or ERROR
        latch?.countDown()
    }

    override fun onResponse(call: Call, response: Response) {
        calledFunktions = calledFunktions or RESPONSE
        this.response = response
        latch?.countDown()
    }
}