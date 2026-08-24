package one.only.player.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// 路径列按 NOCASE 比较，主键唯一性因此对大小写不敏感，同一目录只能有一行
@Entity(
    tableName = "directories",
    indices = [
        Index(value = ["parent_path"]),
    ],
)
data class DirectoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "path", collate = ColumnInfo.NOCASE)
    val path: String,
    @ColumnInfo(name = "filename") val name: String,
    @ColumnInfo(name = "last_modified") val modified: Long,
    @ColumnInfo(name = "parent_path", collate = ColumnInfo.NOCASE) val parentPath: String? = null,
)
