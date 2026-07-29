package one.only.player.core.common

import android.content.pm.ApplicationInfo
import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * 系统未提供运行时开关预测性返回的公开 API，与 KSU 相同走 ApplicationInfo 隐藏方法。
 * API 34+ 才生效。
 */
object PredictiveBackSupport {
    private const val TAG = "PredictiveBackSupport"

    fun setEnabled(applicationInfo: ApplicationInfo, isEnabled: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return false
        return runCatching {
            HiddenApiBypass.addHiddenApiExemptions(
                "Landroid/content/pm/ApplicationInfo;->setEnableOnBackInvokedCallback",
            )
            val method = ApplicationInfo::class.java.getDeclaredMethod(
                "setEnableOnBackInvokedCallback",
                Boolean::class.javaPrimitiveType,
            )
            method.isAccessible = true
            method.invoke(applicationInfo, isEnabled)
        }.onFailure {
            Logger.error(TAG, "切换预测性返回失败", it)
        }.isSuccess
    }
}
