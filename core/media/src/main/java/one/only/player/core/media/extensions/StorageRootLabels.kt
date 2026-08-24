package one.only.player.core.media.extensions

import android.content.Context
import android.os.storage.StorageManager
import one.only.player.core.common.extensions.canonicalPathOrSelf
import one.only.player.core.model.StoragePath

fun Context.storageRootLabels(): Map<StoragePath, String> = getSystemService(StorageManager::class.java).storageVolumes.mapNotNull { volume ->
    val directory = volume.directory ?: return@mapNotNull null
    StoragePath.of(directory.path.canonicalPathOrSelf()) to volume.getDescription(this)
}.toMap()

fun Map<StoragePath, String>.storageRootLabelOf(path: String): String? = this[StoragePath.of(path.canonicalPathOrSelf())]

fun Map<StoragePath, String>.isStorageRoot(path: String): Boolean = storageRootLabelOf(path) != null
