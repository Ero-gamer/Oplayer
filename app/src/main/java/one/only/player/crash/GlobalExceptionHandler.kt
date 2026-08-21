package one.only.player.crash

import android.content.Context
import kotlin.system.exitProcess
import one.only.player.core.common.Logger

class GlobalExceptionHandler(
    private val context: Context,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            Logger.error(TAG, "Uncaught exception on ${t.name}", e)
            StartupRecovery.launchCrashPage(
                context = context,
                exception = e.stackTraceToString(),
            )
        } finally {
            exitProcess(0)
        }
    }

    private companion object {
        const val TAG = "GlobalExceptionHandler"
    }
}
