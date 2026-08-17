package one.only.player.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist_item",
    indices = [
        Index(value = ["playlist_id", "media_uri"], unique = true),
        Index(value = ["playlist_id"]),
        Index(value = ["media_uri"]),
    ],
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,
    @ColumnInfo(name = "media_uri")
    val mediaUri: String,
    @ColumnInfo(name = "media_path")
    val mediaPath: String,
    val title: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Long,
    @ColumnInfo(name = "added_at")
    val addedAt: Long,
)
