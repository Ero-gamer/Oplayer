package one.only.player.core.data.mappers

import one.only.player.core.database.relations.DirectoryWithMedia
import one.only.player.core.database.relations.MediumWithInfo
import one.only.player.core.model.Folder

fun DirectoryWithMedia.toFolder() = Folder(
    name = directory.name,
    path = directory.path,
    dateModified = directory.modified,
    parentPath = directory.parentPath,
    mediaList = media.map(MediumWithInfo::toVideo),
)
