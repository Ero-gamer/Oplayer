package one.only.player.feature.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import one.only.player.core.common.extensions.round
import one.only.player.core.ui.R
import one.only.player.feature.player.state.rememberPlaybackParametersState
import one.only.player.feature.player.ui.panel.PanelChip
import one.only.player.feature.player.ui.panel.PanelSlider
import one.only.player.feature.player.ui.panel.PanelSwitchRow
import one.only.player.feature.player.ui.panel.rememberPlayerPanelTokens
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(UnstableApi::class)
@Composable
fun BoxScope.PlaybackSpeedSelectorView(
    modifier: Modifier = Modifier,
    shouldShow: Boolean,
    player: Player,
) {
    OverlayView(
        modifier = modifier,
        shouldShow = shouldShow,
        title = stringResource(R.string.select_playback_speed),
    ) {
        PlaybackSpeedSelectorContent(player = player)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlaybackSpeedSelectorContent(player: Player) {
    val hapticFeedback = LocalHapticFeedback.current
    val tokens = rememberPlayerPanelTokens()
    val playbackParametersState = rememberPlaybackParametersState(player)
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val minValue = 0.2f
        val maxValue = 4.0f
        val stepSize = 0.01f
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpeedStepButton(
                painter = painterResource(R.drawable.ic_remove),
                testTag = "btn_speed_decrease",
                onClick = {
                    val newSpeed =
                        (playbackParametersState.speed - stepSize).coerceAtLeast(minValue).round(2)
                    playbackParametersState.setPlaybackSpeed(newSpeed)
                },
            )

            MiuixText(
                text = playbackParametersState.speed.round(2).toString(),
                style = MiuixTheme.textStyles.title4,
                color = tokens.contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )

            SpeedStepButton(
                painter = painterResource(R.drawable.ic_add),
                testTag = "btn_speed_increase",
                onClick = {
                    val newSpeed = (playbackParametersState.speed + stepSize).coerceAtMost(maxValue).round(2)
                    playbackParametersState.setPlaybackSpeed(newSpeed)
                },
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PanelSlider(
                value = playbackParametersState.speed,
                valueRange = minValue..maxValue,
                onValueChange = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    playbackParametersState.setPlaybackSpeed(it.round(2))
                },
                modifier = Modifier.weight(1f),
            )
            SpeedStepButton(
                painter = painterResource(R.drawable.ic_reset),
                testTag = "btn_speed_reset",
                onClick = { playbackParametersState.setPlaybackSpeed(1f) },
            )
        }
        FlowRow(
            maxItemsInEachRow = 5,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                0.2f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f,
            ).forEach { speed ->
                PanelChip(
                    text = speed.toString(),
                    isSelected = playbackParametersState.speed == speed,
                    testTag = "chip_speed_$speed",
                    onClick = { playbackParametersState.setPlaybackSpeed(speed) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        PanelSwitchRow(
            text = stringResource(R.string.skip_silence),
            isChecked = playbackParametersState.isSkipSilenceEnabled,
            testTag = "switch_skip_silence",
            onCheckedChange = { playbackParametersState.setIsSkipSilenceEnabled(it) },
        )
    }
}

@Composable
private fun SpeedStepButton(
    painter: Painter,
    testTag: String,
    onClick: () -> Unit,
) {
    val tokens = rememberPlayerPanelTokens()
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(CircleShape)
            .background(tokens.itemColor)
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        MiuixIcon(
            painter = painter,
            contentDescription = null,
            tint = tokens.itemContentColor,
            modifier = Modifier.size(22.dp),
        )
    }
}
