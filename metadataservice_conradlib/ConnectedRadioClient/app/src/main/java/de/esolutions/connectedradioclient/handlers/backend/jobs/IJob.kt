package de.esolutions.connectedradioclient.handlers.backend.jobs

interface IJob : Runnable {
    override fun run()
}