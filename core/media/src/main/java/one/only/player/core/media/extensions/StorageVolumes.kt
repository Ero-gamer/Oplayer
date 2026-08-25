package one.only.player.core.media.extensions

import android.content.Context
import android.os.storage.StorageManager
import android.provider.MediaStore
import one.only.player.core.common.extensions.canonicalPathOrSelf
import one.only.player.core.model.StoragePath

// 已挂载的外部存储卷。卷根路径在此解析一次并定型，之后一路按 StoragePath 比较
data class MediaStorageVolume(
    val label: String,
    val rootPath: StoragePath,
    val mediaStoreVolumeName: String?,
    val isPrimary: Boolean,
)

fun Context.mediaStorageVolumes(): List<MediaStorageVolume> = getSystemService(StorageManager::class.java)
    .storageVolumes
    .mapNotNull { volume ->
        val directory = volume.directory ?: return@mapNotNull null
        MediaStorageVolume(
            label = volume.getDescription(this),
            rootPath = StoragePath.of(directory.path.canonicalPathOrSelf()),
            // 个别 ROM 对主存储不返回卷名，而 MediaStore 写入必须有它
            mediaStoreVolumeName = volume.mediaStoreVolumeName
                ?: MediaStore.VOLUME_EXTERNAL_PRIMARY.takeIf { volume.isPrimary },
            isPrimary = volume.isPrimary,
        )
    }

fun Context.storageRootLabels(): Map<StoragePath, String> = mediaStorageVolumes()
    .associate { volume -> volume.rootPath to volume.label }

fun Map<StoragePath, String>.storageRootLabelOf(path: String): String? = this[StoragePath.of(path)]

fun Map<StoragePath, String>.isStorageRoot(path: String): Boolean = storageRootLabelOf(path) != null
