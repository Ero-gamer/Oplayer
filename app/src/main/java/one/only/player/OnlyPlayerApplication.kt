package one.only.player

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import one.only.player.core.common.AppThemeModeManager
import one.only.player.core.common.Logger
import one.only.player.core.common.PredictiveBackSupport
import one.only.player.crash.CRASH_PROCESS_SUFFIX
import one.only.player.crash.GlobalExceptionHandler

@HiltAndroidApp
class OnlyPlayerApplication :
    Application(),
    SingletonImageLoader.Factory {

    @Inject
    lateinit var imageLoader: Lazy<ImageLoader>

    override fun onCreate() {
        // 崩溃进程不初始化 Hilt 业务图，避免恢复页依赖主业务启动。
        if (Application.getProcessName().endsWith(CRASH_PROCESS_SUFFIX)) return
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(applicationContext))
        Logger.initialize(this)
        super.onCreate()
        AppForegroundTracker.register(this)
        val startupPreferences = StartupPreferencesCache.initialize(context = this)
        AppThemeModeManager.applyPlatformToCurrent(
            context = applicationContext,
            mode = startupPreferences.themeConfig.toAppThemeMode(),
        )
        PredictiveBackSupport.setEnabled(
            applicationInfo = applicationInfo,
            isEnabled = startupPreferences.shouldEnablePredictiveBack,
        )
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()
}
