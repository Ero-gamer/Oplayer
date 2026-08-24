package one.only.player.feature.videopicker.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import one.only.player.core.media.extensions.storageRootLabels
import one.only.player.core.model.StoragePath

@Composable
internal fun rememberStorageRootLabels(): Map<StoragePath, String> {
    val context = LocalContext.current
    return remember(context) { context.storageRootLabels() }
}
