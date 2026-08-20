package one.only.player.core.datastore.serializer

import java.io.File
import one.only.player.core.common.Logger

internal fun <T> readPersistedDataStoreValue(
    file: File,
    defaultValue: T,
    tag: String,
    valueName: String,
    decode: (String) -> T,
): T {
    if (!file.exists()) return defaultValue

    return try {
        file.inputStream().use { input ->
            decode(input.readBytes().decodeToString())
        }
    } catch (exception: Exception) {
        Logger.error(tag, "Failed to bootstrap $valueName", exception)
        defaultValue
    }
}
