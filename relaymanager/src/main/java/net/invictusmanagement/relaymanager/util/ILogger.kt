package net.invictusmanagement.relaymanager.util

interface ILogger {
    fun log(logger: String, message: String, exception: Throwable? = null)
    fun logError(logger: String, message: String, exception: Throwable? = null)
}
