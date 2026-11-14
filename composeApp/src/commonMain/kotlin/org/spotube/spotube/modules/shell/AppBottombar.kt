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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import org.jetbrains.compose.resources.stringResource
import org.spotube.millennium.composables.MillenniumIcon
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.spotube.NavigationItem
import org.spotube.spotube.bottombarTabs
import org.spotube.spotube.core.navigation.NavigationState
import org.spotube.spotube.core.navigation.Navigator
import org.spotube.spotube.core.navigation.Routes
import org.spotube.spotube.modules.downloads.DownloadBadgeIndicator

@Composable
fun AppBottombar(
    navigator: Navigator,
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
    libraryBadge: @Composable () -> Unit = { DownloadBadgeIndicator() }
) {
    // Apple / Telegram tarzı yarı saydam koyu cam yüzeyi
    val glassBackground = MillenniumTheme.colors.surface.copy(alpha = 0.78f)
    val glassBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.06f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.45f)
                )
                .clip(CircleShape)
                .background(glassBackground)
                .border(width = 1.dp, brush = glassBorderBrush, shape = CircleShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottombarTabs.filterIsInstance<NavigationItem.Tab<*>>()
                .forEach { tab ->
                    val selected = navigationState.topLevelRoute == tab.route
                    val title = stringResource(tab.title)

                    // Telegram tarzı yumuşak renk geçişleri
                    val activeAccentColor = Color(0xFF38BDF8) // Canlı cam mavisi vurgu
                    val pillBackgroundColor by animateColorAsState(
                        targetValue = if (selected) activeAccentColor.copy(alpha = 0.20f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) activeAccentColor else Color.White.copy(alpha = 0.80f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(pillBackgroundColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { navigator.navigate(tab.route) }
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                MillenniumIcon(
                                    imageVector = tab.icon,
                                    contentDescription = title,
                                    tint = contentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (tab.route == Routes.Library) {
                                    libraryBadge()
                                }
                            }
                            Text(
                                text = title,
                                color = contentColor,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
        }
    }
}
