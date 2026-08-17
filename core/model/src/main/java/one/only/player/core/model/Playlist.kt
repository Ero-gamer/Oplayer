package one.only.player.core.model

data class Playlist(
    val id: Long = 0,
    val title: String,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val sortOrder: Long = 0,
    val itemCount: Int = 0,
)

data class PlaylistItem(
    val id: Long = 0,
    val playlistId: Long,
    val mediaUri: String,
    val mediaPath: String,
    val title: String,
    val sortOrder: Long = 0,
    val addedAt: Long = 0,
    val video: Video? = null,
)
