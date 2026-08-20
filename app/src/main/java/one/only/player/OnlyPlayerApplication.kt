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
import one.only.player.crash.CrashActivity
import one.only.player.crash.GlobalExceptionHandler

@HiltAndroidApp
class OnlyPlayerApplication :
    Application(),
    SingletonImageLoader.Factory {

    @Inject
    lateinit var imageLoader: Lazy<ImageLoader>

    override fun onCreate() {
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
        Logger.initialize(this)
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(applicationContext, CrashActivity::class.java))
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()
}
