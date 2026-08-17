package one.only.player.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import one.only.player.core.database.entities.PlaylistEntity
import one.only.player.core.database.entities.PlaylistItemEntity
import one.only.player.core.database.relations.PlaylistWithCount

@Dao
interface PlaylistDao {

    @Query(
        """
        SELECT playlist.*, COUNT(playlist_item.id) AS item_count
        FROM playlist
        LEFT JOIN playlist_item ON playlist_item.playlist_id = playlist.id
        GROUP BY playlist.id
        ORDER BY playlist.sort_order DESC, playlist.updated_at DESC
        """,
    )
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlist WHERE id = :id")
    suspend fun getById(id: Long): PlaylistEntity?

    @Insert
    suspend fun insert(entity: PlaylistEntity): Long

    @Update
    suspend fun update(entity: PlaylistEntity)

    @Query("DELETE FROM playlist WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM playlist")
    suspend fun clear()

    @Query("DELETE FROM playlist_item")
    suspend fun clearItems()

    @Query("SELECT * FROM playlist_item WHERE playlist_id = :playlistId ORDER BY sort_order ASC, id ASC")
    fun observeItems(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_item WHERE playlist_id = :playlistId ORDER BY sort_order ASC, id ASC")
    suspend fun getItems(playlistId: Long): List<PlaylistItemEntity>

    @Query("SELECT * FROM playlist_item WHERE media_uri = :mediaUri")
    suspend fun getItemsByMediaUri(mediaUri: String): List<PlaylistItemEntity>

    @Query("SELECT * FROM playlist_item")
    suspend fun getAllItems(): List<PlaylistItemEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(entity: PlaylistItemEntity): Long

    @Update
    suspend fun updateItem(entity: PlaylistItemEntity)

    @Query("DELETE FROM playlist_item WHERE id in (:ids)")
    suspend fun deleteItems(ids: List<Long>)

    @Query("DELETE FROM playlist_item WHERE playlist_id = :playlistId")
    suspend fun deleteItemsByPlaylist(playlistId: Long)

    @Query("UPDATE playlist SET updated_at = :updatedAt WHERE id = :id")
    suspend fun touch(
        id: Long,
        updatedAt: Long,
    )
}
