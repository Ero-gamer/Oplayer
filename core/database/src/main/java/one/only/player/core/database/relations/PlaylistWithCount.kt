package one.only.player.core.database.relations

import androidx.room.ColumnInfo
import androidx.room.Embedded
import one.only.player.core.database.entities.PlaylistEntity

data class PlaylistWithCount(
    @Embedded
    val playlist: PlaylistEntity,
    @ColumnInfo(name = "item_count")
    val itemCount: Int,
)
