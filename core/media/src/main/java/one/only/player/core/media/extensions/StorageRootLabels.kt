package one.only.player.core.media.extensions

import android.content.Context
import android.os.storage.StorageManager
import one.only.player.core.common.extensions.canonicalPathOrSelf
import one.only.player.core.model.StoragePath

// 卷根路径在此解析一次，之后按 StoragePath 归一比较，查询侧不再重复解析
fun Context.storageRootLabels(): Map<StoragePath, String> = getSystemService(StorageManager::class.java).storageVolumes.mapNotNull { volume ->
    val directory = volume.directory ?: return@mapNotNull null
    StoragePath.of(directory.path.canonicalPathOrSelf()) to volume.getDescription(this)
}.toMap()

fun Map<StoragePath, String>.storageRootLabelOf(path: String): String? = this[StoragePath.of(path)]

fun Map<StoragePath, String>.isStorageRoot(path: String): Boolean = storageRootLabelOf(path) != null
