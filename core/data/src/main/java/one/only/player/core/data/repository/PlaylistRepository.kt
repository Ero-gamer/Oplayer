package one.only.player.core.data.repository

import kotlinx.coroutines.flow.Flow
import one.only.player.core.model.Playlist
import one.only.player.core.model.PlaylistItem
import one.only.player.core.model.Video

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>

    fun observeItems(playlistId: Long): Flow<List<PlaylistItem>>

    suspend fun getById(id: Long): Playlist?

    suspend fun getItems(playlistId: Long): List<PlaylistItem>

    suspend fun create(title: String): Long

    suspend fun rename(
        id: Long,
        title: String,
    )

    suspend fun delete(id: Long)

    suspend fun clear()

    suspend fun addVideos(
        playlistId: Long,
        videos: List<Video>,
    ): Int

    suspend fun removeItems(ids: List<Long>)

    suspend fun updateLocalVideoTarget(
        oldLocalUri: String,
        newLocalUri: String,
        newLocalPath: String,
        newTitle: String,
    )

    suspend fun updateLocalFolderPath(
        oldPath: String,
        newPath: String,
    )
}
