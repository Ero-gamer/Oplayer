package one.only.player.feature.videopicker.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import one.only.player.core.model.Playlist
import one.only.player.core.ui.R
import one.only.player.core.ui.components.CancelButton
import one.only.player.core.ui.components.NextDialog
import one.only.player.core.ui.components.RadioTextButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
) {
    var shouldShowCreateDialog by rememberSaveable { mutableStateOf(playlists.isEmpty()) }

    if (shouldShowCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = {
                if (playlists.isEmpty()) {
                    onDismiss()
                } else {
                    shouldShowCreateDialog = false
                }
            },
            onCreate = onCreatePlaylist,
        )
        return
    }

    NextDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.add_to_playlist),
        content = {
            Column {
                playlists.forEach { playlist ->
                    RadioTextButton(
                        text = playlist.title,
                        isSelected = false,
                        onClick = { onSelectPlaylist(playlist.id) },
                        modifier = Modifier.testTag("option_playlist_${playlist.id}"),
                    )
                }
                RadioTextButton(
                    text = stringResource(R.string.create_playlist),
                    isSelected = false,
                    onClick = { shouldShowCreateDialog = true },
                    modifier = Modifier.testTag("option_playlist_create"),
                )
            }
        },
        confirmButton = null,
        dismissButton = { CancelButton(onClick = onDismiss) },
    )
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    title: String = stringResource(R.string.create_playlist),
    confirmText: String = stringResource(R.string.add),
    initialName: String = "",
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    NextDialog(
        onDismissRequest = onDismiss,
        title = title,
        content = {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_playlist_name"),
                singleLine = true,
                label = stringResource(R.string.name),
            )
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("btn_playlist_create_confirm"),
                text = confirmText,
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                onClick = { onCreate(name.trim()) },
            )
        },
        dismissButton = { CancelButton(onClick = onDismiss) },
    )
}
