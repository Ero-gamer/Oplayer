package one.only.player.core.media.extensions

import one.only.player.core.common.extensions.isInsideNoMediaDirectory
import one.only.player.core.model.StoragePath

// .nomedia 目录下的路径不进媒体库
fun Iterable<StoragePath>.excludeNoMediaPaths(): List<StoragePath> = filterNot { path -> path.value.isInsideNoMediaDirectory() }
