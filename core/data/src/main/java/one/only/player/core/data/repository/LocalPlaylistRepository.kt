package one.only.player.core.data.repository

import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import one.only.player.core.common.extensions.canonicalPathOrSelf
import one.only.player.core.database.dao.PlaylistDao
import one.only.player.core.database.entities.PlaylistEntity
import one.only.player.core.database.entities.PlaylistItemEntity
import one.only.player.core.database.relations.PlaylistWithCount
import one.only.player.core.model.Playlist
import one.only.player.core.model.PlaylistItem
import one.only.player.core.model.Video

class LocalPlaylistRepository @Inject constructor(
    private val dao: PlaylistDao,
) : PlaylistRepository {

    override fun observePlaylists(): Flow<List<Playlist>> = dao.observePlaylists().map { rows ->
        rows.map(PlaylistWithCount::toModel)
    }

    override fun observeItems(playlistId: Long): Flow<List<PlaylistItem>> = dao.observeItems(playlistId).map { entities ->
        entities.map(PlaylistItemEntity::toModel)
    }

    override suspend fun getById(id: Long): Playlist? {
        val entity = dao.getById(id) ?: return null
        return entity.toModel(itemCount = dao.getItems(id).size)
    }

    override suspend fun getItems(playlistId: Long): List<PlaylistItem> = dao.getItems(playlistId).map(PlaylistItemEntity::toModel)

    override suspend fun create(title: String): Long {
        val now = System.currentTimeMillis()
        return dao.insert(
            PlaylistEntity(
                title = title.trim().ifBlank { DEFAULT_PLAYLIST_TITLE },
                createdAt = now,
                updatedAt = now,
                sortOrder = now,
            ),
        )
    }

    override suspend fun rename(
        id: Long,
        title: String,
    ) {
        val current = dao.getById(id) ?: return
        val normalizedTitle = title.trim().ifBlank { return }
        dao.update(
            current.copy(
                title = normalizedTitle,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun delete(id: Long) {
        if (id <= 0L) return
        dao.deleteItemsByPlaylist(id)
        dao.delete(id)
    }

    override suspend fun clear() {
        dao.clearItems()
        dao.clear()
    }

    override suspend fun addVideos(
        playlistId: Long,
        videos: List<Video>,
    ): Int {
        if (playlistId <= 0L) return 0
        dao.getById(playlistId) ?: return 0
        val existingUris = dao.getItems(playlistId).map(PlaylistItemEntity::mediaUri).toMutableSet()
        var addedCount = 0
        videos.distinctBy(Video::uriString).forEach { video ->
            if (video.uriString.isBlank() || video.isInRecycleBin) return@forEach
            if (video.uriString in existingUris) return@forEach
            val now = System.currentTimeMillis()
            val insertedId = dao.insertItem(
                PlaylistItemEntity(
                    playlistId = playlistId,
                    mediaUri = video.uriString,
                    mediaPath = video.path,
                    title = video.nameWithExtension,
                    sortOrder = now,
                    addedAt = now,
                ),
            )
            if (insertedId <= 0L) return@forEach
            existingUris += video.uriString
            addedCount++
        }
        if (addedCount > 0) {
            dao.touch(playlistId, System.currentTimeMillis())
        }
        return addedCount
    }

    override suspend fun removeItems(ids: List<Long>) {
        val safeIds = ids.distinct().filter { it > 0L }
        if (safeIds.isEmpty()) return
        dao.deleteItems(safeIds)
    }

    override suspend fun updateLocalVideoTarget(
        oldLocalUri: String,
        newLocalUri: String,
        newLocalPath: String,
        newTitle: String,
    ) {
        val items = dao.getItemsByMediaUri(oldLocalUri)
        if (items.isEmpty()) return
        val now = System.currentTimeMillis()
        items.forEach { current ->
            val duplicate = dao.getItems(current.playlistId).firstOrNull { item ->
                item.id != current.id && item.mediaUri == newLocalUri
            }
            if (duplicate != null) {
                dao.deleteItems(listOf(current.id))
                return@forEach
            }
            dao.updateItem(
                current.copy(
                    mediaUri = newLocalUri,
                    mediaPath = newLocalPath,
                    title = newTitle,
                ),
            )
            dao.touch(current.playlistId, now)
        }
    }

    override suspend fun updateLocalFolderPath(
        oldPath: String,
        newPath: String,
    ) {
        val oldCanonicalPath = oldPath.canonicalPathOrSelf()
        dao.getAllItems().forEach { current ->
            val currentPath = current.mediaPath.canonicalPathOrSelf()
            if (currentPath != oldCanonicalPath && !currentPath.startsWith(oldCanonicalPath + File.separator)) {
                return@forEach
            }
            dao.updateItem(
                current.copy(
                    mediaPath = current.mediaPath.replacePathPrefix(
                        oldPath = oldPath,
                        newPath = newPath,
                    ),
                ),
            )
        }
    }

    private fun String.replacePathPrefix(
        oldPath: String,
        newPath: String,
    ): String {
        val canonicalPath = canonicalPathOrSelf()
        val oldCanonicalPath = oldPath.canonicalPathOrSelf()
        val relativePath = canonicalPath.removePrefix(oldCanonicalPath).trimStart(File.separatorChar)
        if (relativePath.isBlank()) return newPath
        return File(newPath, relativePath).path
    }

    private companion object {
        const val DEFAULT_PLAYLIST_TITLE = "Playlist"
    }
}

private fun PlaylistWithCount.toModel(): Playlist = playlist.toModel(itemCount = itemCount)

private fun PlaylistEntity.toModel(itemCount: Int): Playlist = Playlist(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sortOrder = sortOrder,
    itemCount = itemCount,
)

private fun PlaylistItemEntity.toModel(): PlaylistItem = PlaylistItem(
    id = id,
    playlistId = playlistId,
    mediaUri = mediaUri,
    mediaPath = mediaPath,
    title = title,
    sortOrder = sortOrder,
    addedAt = addedAt,
)
