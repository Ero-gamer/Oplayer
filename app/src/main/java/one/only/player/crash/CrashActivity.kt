package one.only.player.crash

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import one.only.player.MainActivity
import one.only.player.core.common.extensions.applyPrivacyProtection
import one.only.player.core.ui.R
import one.only.player.core.ui.components.PageContentTopPadding
import one.only.player.core.ui.designsystem.AppIcons
import one.only.player.core.ui.extensions.withBottomFallback
import one.only.player.core.ui.theme.OnlyPlayerTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

class CrashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        StartupRecovery.markFailed(this)
        super.onCreate(savedInstanceState)
        applyPrivacyProtection(
            shouldPreventScreenshots = true,
            shouldHideInRecents = true,
        )
        val shouldUseDarkTheme = isSystemDarkTheme(resources.configuration)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { shouldUseDarkTheme },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { shouldUseDarkTheme },
            ),
        )
        val exceptionString = intent.getStringExtra(CRASH_EXCEPTION_EXTRA) ?: ""
        if (savedInstanceState == null) {
            Toast.makeText(
                this,
                R.string.crash_screen_feedback_toast,
                Toast.LENGTH_LONG,
            ).show()
        }
        val clipboardManager = getSystemService(ClipboardManager::class.java)

        setContent {
            OnlyPlayerTheme(
                shouldUseDarkTheme = shouldUseDarkTheme,
                shouldUseDynamicColor = false,
            ) {
                CrashScreen(
                    exceptionString = exceptionString,
                    onShareCrashLogClick = { shareCrashLog(exceptionString) },
                    onCopyCrashLogClick = {
                        clipboardManager.setPrimaryClip(
                            createSensitiveClipData(exceptionString),
                        )
                    },
                    onRestartClick = {
                        startActivity(
                            Intent(this@CrashActivity, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            },
                        )
                        finish()
                    },
                )
            }
        }
    }

    private fun shareCrashLog(
        exceptionString: String,
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, exceptionString)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.crash_screen_share)))
    }
}

@Composable
private fun CrashScreen(
    modifier: Modifier = Modifier,
    exceptionString: String,
    onShareCrashLogClick: () -> Unit = {},
    onCopyCrashLogClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResource(R.string.crash_screen_title),
                actions = {
                    IconButton(
                        onClick = onShareCrashLogClick,
                        modifier = Modifier.testTag("button_crash_share"),
                    ) {
                        Icon(
                            imageVector = AppIcons.Share,
                            contentDescription = stringResource(R.string.crash_screen_share),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                    IconButton(
                        onClick = onCopyCrashLogClick,
                        modifier = Modifier.testTag("button_crash_copy"),
                    ) {
                        Icon(
                            imageVector = AppIcons.Copy,
                            contentDescription = stringResource(R.string.crash_screen_copy),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                    IconButton(
                        onClick = onRestartClick,
                        modifier = Modifier.testTag("button_crash_restart"),
                    ) {
                        Icon(
                            imageVector = AppIcons.Update,
                            contentDescription = stringResource(R.string.crash_screen_restart),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues.withBottomFallback())
                .padding(top = PageContentTopPadding)
                .padding(horizontal = 16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = exceptionString,
                    fontFamily = FontFamily.Monospace,
                    style = MiuixTheme.textStyles.footnote1,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun CrashLogsScreenPreview() {
    OnlyPlayerTheme {
        CrashScreen(
            exceptionString = "Exception message",
        )
    }
}

private fun createSensitiveClipData(text: String): ClipData = ClipData.newPlainText(null, text).apply {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@apply
    description.extras = PersistableBundle().apply {
        putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
    }
}

private fun isSystemDarkTheme(configuration: Configuration): Boolean {
    val nightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMode == Configuration.UI_MODE_NIGHT_YES
}
