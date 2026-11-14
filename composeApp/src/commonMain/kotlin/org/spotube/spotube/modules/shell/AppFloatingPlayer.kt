/*
 * Copyright (C) 2026 Kingkor Roy Tirtho and Spotube Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.spotube.spotube.modules.shell

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composeunstyled.Text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.composables.MillenniumProgress
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.spotube.core.audioplayer.AudioPlayerInterface
import org.spotube.spotube.core.audioplayer.AudioPlayerQueue
import org.spotube.spotube.core.ui.theming.AppTheme
import org.spotube.spotube.modules.saved_tracks.SAVED_TRACKS_COLLECTION_ID
import org.spotube.spotube.modules.saved_tracks.SavedTracksViewModel
import org.spotube.spotube.resources.iconsax.Iconsax
import org.spotube.spotube.resources.iconsax.IconsaxMusic
import org.spotube.spotube.resources.iconsax.IconsaxNext
import org.spotube.spotube.resources.iconsax.IconsaxPause
import org.spotube.spotube.resources.iconsax.IconsaxPlay
import spotube.composeapp.generated.resources.Res
import spotube.composeapp.generated.resources.player_nothing_playing
import spotube.composeapp.generated.resources.player_pause_content_description
import spotube.composeapp.generated.resources.player_play_content_description

internal val FloatingPlayerContentHeight = 64.dp
internal val FloatingPlayerBottomBleed = 0.dp

@Composable
fun AppFloatingPlayer(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    audioPlayer: AudioPlayerInterface = koinInject(),
    audioPlayerQueue: AudioPlayerQueue = koinInject(),
    savedTracksViewModel: SavedTracksViewModel = koinViewModel<SavedTracksViewModel>(
        key = SAVED_TRACKS_COLLECTION_ID,
        parameters = { parametersOf() }
    )
) {
    val playerUiState = rememberPlayerUiState(audioPlayer, audioPlayerQueue)
    val scope = rememberCoroutineScope()
    val albumArtModifier =
        rememberSharedAlbumArtModifier(sharedTransitionScope, animatedVisibilityScope)

    fun onPlayPause() {
        scope.launch {
            if (playerUiState.isPlaying) {
                audioPlayer.pause()
            } else {
                audioPlayer.play()
            }
        }
    }

    fun onNextTrack() {
        scope.launch {
            audioPlayer.skipToNext()
        }
    }

    FloatingPlayerContent(
        playerUiState = playerUiState,
        modifier = modifier,
        albumArtModifier = albumArtModifier,
        onPlayPause = ::onPlayPause,
        onNextTrack = ::onNextTrack,
        heartButton = {
            PlayerHeartButton(
                audioPlayerQueue = audioPlayerQueue,
                savedTracksViewModel = savedTracksViewModel
            )
        }
    )
}

@Composable
internal fun FloatingPlayerContent(
    playerUiState: PlayerUiState,
    modifier: Modifier = Modifier,
    albumArtModifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    heartButton: @Composable () -> Unit
) {
    val coverModel = playerUiState.coverModel

    // Alt menüyle birebir uyumlu Liquid Glass yüzeyi
    val glassBackground = MillenniumTheme.colors.surface.copy(alpha = 0.78f)
    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.06f)
        )
    )
    val activeAccentColor = Color(0xFF38BDF8)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FloatingPlayerContentHeight)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.45f)
                )
                .clip(CircleShape)
                .background(glassBackground)
                .border(width = 1.dp, brush = glassBorderBrush, shape = CircleShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol taraf: Yuvarlatılmış albüm kapağı ve dinamik şarkı/sanatçı bilgisi
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .then(albumArtModifier)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverModel != null) {
                            AsyncImage(
                                model = coverModel,
                                contentDescription = playerUiState.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            MillenniumIcon(
                                Iconsax.IconsaxMusic,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 8.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = if (playerUiState.queue.isEmpty()) {
                                stringResource(Res.string.player_nothing_playing)
                            } else {
                                playerUiState.title
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (playerUiState.queue.isNotEmpty()) {
                            Text(
                                text = playerUiState.artists,
                                color = Color.White.copy(alpha = 0.68f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Sağ taraf: Kalp, Oynat/Duraklat ve Sonraki Şarkı butonları
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    heartButton()

                    // Modern dairesel Oynat / Duraklat butonu
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (playerUiState.queue.isNotEmpty()) {
                                    activeAccentColor.copy(alpha = 0.24f)
                                } else {
                                    Color.White.copy(alpha = 0.08f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (playerUiState.queue.isNotEmpty()) {
                                    activeAccentColor.copy(alpha = 0.45f)
                                } else {
                                    Color.White.copy(alpha = 0.12f)
                                },
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = playerUiState.queue.isNotEmpty(),
                                onClick = onPlayPause
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        MillenniumIcon(
                            imageVector = if (playerUiState.isPlaying) Iconsax.IconsaxPause else Iconsax.IconsaxPlay,
                            contentDescription = stringResource(
                                if (playerUiState.isPlaying) {
                                    Res.string.player_pause_content_description
                                } else {
                                    Res.string.player_play_content_description
                                }
                            ),
                            tint = if (playerUiState.queue.isNotEmpty()) activeAccentColor else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Sonraki şarkıya geçiş butonu
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = playerUiState.queue.isNotEmpty(),
                                onClick = onNextTrack
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        MillenniumIcon(
                            imageVector = Iconsax.IconsaxNext,
                            contentDescription = null,
                            tint = if (playerUiState.queue.isNotEmpty()) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // En altta ince, zarif şarkı ilerleme çizgisi
            MillenniumProgress(
                progress = playerUiState.progress,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(2.dp)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun rememberSharedAlbumArtModifier(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return Modifier

    with(sharedTransitionScope) {
        return Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(key = "player_album_art"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}
