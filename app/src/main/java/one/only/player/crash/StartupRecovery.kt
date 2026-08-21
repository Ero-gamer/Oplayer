package one.only.player.crash

import android.content.Context
import android.content.Intent
import android.os.Process
import java.io.File
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import one.only.player.core.common.Logger
import one.only.player.core.ui.R

internal const val CRASH_EXCEPTION_EXTRA = "exception"
internal const val CRASH_PROCESS_SUFFIX = ":crash"

internal object StartupRecovery {

    private const val TAG = "StartupRecovery"
    private const val MARKER_FILE_NAME = "startup_in_progress"
    private const val STARTUP_TIMEOUT_MILLIS = 15_000L

    private val lock = Any()
    private val watchdogExecutor = ScheduledThreadPoolExecutor(1) { runnable ->
        Thread(runnable, "only-player-startup-watchdog").apply {
            isDaemon = true
        }
    }.apply {
        removeOnCancelPolicy = true
    }

    private var activeStartedAt: Long? = null
    private var watchdog: ScheduledFuture<*>? = null
    private var isCrashPageRequested = false

    // 文件标记不依赖进程内缓存，可由独立崩溃进程安全清理。
    fun begin(context: Context) {
        val appContext = context.applicationContext
        synchronized(lock) {
            if (activeStartedAt != null) return

            val markerFile = markerFile(appContext)
            if (markerFile.exists()) {
                Logger.info(TAG, "Discarding unfinished startup marker")
            }

            val startedAt = System.currentTimeMillis()
            persistMarker(markerFile, startedAt)

            activeStartedAt = startedAt
            watchdog?.cancel(false)
            watchdog = watchdogExecutor.schedule(
                { onStartupTimeout(appContext, startedAt) },
                STARTUP_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS,
            )
        }
    }

    fun markReady(context: Context) {
        synchronized(lock) {
            if (activeStartedAt == null) return
            clearMarker(context.applicationContext)
            activeStartedAt = null
            watchdog?.cancel(false)
            watchdog = null
        }
    }

    fun markFailed(context: Context) {
        synchronized(lock) {
            clearMarker(context.applicationContext)
            activeStartedAt = null
            watchdog?.cancel(false)
            watchdog = null
        }
    }

    fun launchCrashPage(
        context: Context,
        exception: String,
    ) {
        val shouldLaunch = synchronized(lock) {
            if (isCrashPageRequested) {
                false
            } else {
                isCrashPageRequested = true
                clearMarker(context.applicationContext)
                activeStartedAt = null
                watchdog?.cancel(false)
                watchdog = null
                true
            }
        }
        if (!shouldLaunch) return

        val intent = Intent(context, CrashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(CRASH_EXCEPTION_EXTRA, Logger.sanitize(exception))
        }
        runCatching {
            context.applicationContext.startActivity(intent)
        }.onFailure { launchException ->
            Logger.error(TAG, "Failed to launch crash page", launchException)
        }
    }

    private fun onStartupTimeout(
        context: Context,
        startedAt: Long,
    ) {
        val shouldHandleTimeout = synchronized(lock) {
            if (activeStartedAt != startedAt) {
                false
            } else {
                Logger.error(TAG, "Startup timed out after ${STARTUP_TIMEOUT_MILLIS}ms")
                activeStartedAt = null
                watchdog = null
                true
            }
        }

        if (!shouldHandleTimeout) return
        launchCrashPage(
            context = context,
            exception = context.getString(
                R.string.crash_screen_startup_timeout,
                STARTUP_TIMEOUT_MILLIS / 1_000L,
            ),
        )
        Process.killProcess(Process.myPid())
    }

    private fun markerFile(context: Context) = File(context.noBackupFilesDir, MARKER_FILE_NAME)

    private fun persistMarker(
        markerFile: File,
        startedAt: Long,
    ) {
        runCatching {
            markerFile.writeText(startedAt.toString())
        }.onFailure { exception ->
            Logger.error(TAG, "Failed to persist startup marker", exception)
        }
    }

    private fun clearMarker(context: Context) {
        val markerFile = markerFile(context)
        if (!markerFile.exists()) return
        if (!markerFile.delete()) Logger.error(TAG, "Failed to clear startup marker")
    }
}
