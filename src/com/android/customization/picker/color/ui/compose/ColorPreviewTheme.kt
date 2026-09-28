/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.customization.picker.color.ui.compose

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.android.wallpaper.picker.customization.ui.viewmodel.ColorUpdateViewModel
import com.google.ux.material.libmonet.dynamiccolor.DynamicScheme
import com.google.ux.material.libmonet.dynamiccolor.MaterialDynamicColors

@Immutable
interface CustomColorScheme {
    val primary: Color
    val primaryContainer: Color
    val onPrimary: Color
    val onPrimaryFixedVariant: Color
    val secondaryFixedDim: Color
    val secondaryContainer: Color
    val onSecondaryContainer: Color
    val tertiary: Color
    val tertiaryFixed: Color
    val surfaceBright: Color
    val surfaceDim: Color
    val onSurface: Color
    val onSurfaceVariant: Color
}

val defaultCustomColorScheme =
    object : CustomColorScheme {
        override val primary: Color = Color.Transparent
        override val primaryContainer: Color = Color.Transparent
        override val onPrimary: Color = Color.Transparent
        override val onPrimaryFixedVariant: Color = Color.Transparent
        override val secondaryFixedDim: Color = Color.Transparent
        override val secondaryContainer: Color = Color.Transparent
        override val onSecondaryContainer: Color = Color.Transparent
        override val tertiary: Color = Color.Transparent
        override val tertiaryFixed: Color = Color.Transparent
        override val surfaceBright: Color = Color.Transparent
        override val surfaceDim: Color = Color.Transparent
        override val onSurface: Color = Color.Transparent
        override val onSurfaceVariant: Color = Color.Transparent
    }

val LocalAnimatedColorScheme = compositionLocalOf { defaultCustomColorScheme }

@Composable
fun ColorPreviewTheme(scheme: DynamicScheme?, content: @Composable () -> Unit) {
    if (scheme == null) {
        return content()
    }
    val colorScheme = remember(scheme) { getColorScheme(scheme) }
    val colorTransitionData = updateTransitionData(colorScheme)

    // Done at the root so that the whole content tree will receive the LocalAnimatedColorScheme.
    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(
            value = LocalAnimatedColorScheme provides colorTransitionData,
            content = content,
        )
    }
}

@Composable
fun updateTransitionData(colorScheme: ColorScheme): CustomColorScheme {
    val transition = updateTransition(colorScheme)
    val primary = transition.animateThemeColor { state -> state.primary }
    val primaryContainer = transition.animateThemeColor { state -> state.primaryContainer }
    val onPrimary = transition.animateThemeColor { state -> state.onPrimary }
    val onPrimaryFixedVariant =
        transition.animateThemeColor { state -> state.onPrimaryFixedVariant }
    val secondaryFixedDim = transition.animateThemeColor { state -> state.secondaryFixedDim }
    val secondaryContainer = transition.animateThemeColor { state -> state.secondaryContainer }
    val tertiary = transition.animateThemeColor { state -> state.tertiary }
    val tertiaryFixed = transition.animateThemeColor { state -> state.tertiaryFixed }
    val onSecondaryContainer = transition.animateThemeColor { state -> state.onSecondaryContainer }
    val surfaceBright = transition.animateThemeColor { state -> state.surfaceBright }
    val surfaceDim = transition.animateThemeColor { state -> state.surfaceDim }
    val onSurface = transition.animateThemeColor { state -> state.onSurface }
    val onSurfaceVariant = transition.animateThemeColor { state -> state.onSurfaceVariant }
    return remember(transition) {
        object : CustomColorScheme {
            override val primary: Color by primary
            override val primaryContainer: Color by primaryContainer
            override val onPrimary: Color by onPrimary
            override val onPrimaryFixedVariant: Color by onPrimaryFixedVariant
            override val secondaryFixedDim: Color by secondaryFixedDim
            override val secondaryContainer: Color by secondaryContainer
            override val onSecondaryContainer: Color by onSecondaryContainer
            override val tertiary: Color by tertiary
            override val tertiaryFixed: Color by tertiaryFixed
            override val surfaceBright: Color by surfaceBright
            override val surfaceDim: Color by surfaceDim
            override val onSurface: Color by onSurface
            override val onSurfaceVariant: Color by onSurfaceVariant
        }
    }
}

@Composable
fun Transition<ColorScheme>.animateThemeColor(
    targetValueByState: @Composable (state: ColorScheme) -> Color
): State<Color> {
    return animateColor(
        transitionSpec = {
            tween(
                durationMillis = ColorUpdateViewModel.COLOR_ANIMATION_DURATION_MILLIS.toInt(),
                easing = LinearEasing,
            )
        },
        targetValueByState = targetValueByState,
    )
}

// Convert DynamicScheme to Compose ColorScheme
// DiamaneOS: ask the scheme for each role; monet's scheme answers with the colour the system
// applies.
// TODO (b/450071500): use dynamiccolors library to get Compose ColorScheme when it is ready
fun getColorScheme(scheme: DynamicScheme): ColorScheme {
    val materialColors = MaterialDynamicColors()
    return ColorScheme(
        primary = Color(scheme.getArgb(materialColors.primary())),
        onPrimary = Color(scheme.getArgb(materialColors.onPrimary())),
        primaryContainer = Color(scheme.getArgb(materialColors.primaryContainer())),
        onPrimaryContainer = Color(scheme.getArgb(materialColors.onPrimaryContainer())),
        inversePrimary = Color(scheme.getArgb(materialColors.inversePrimary())),
        secondary = Color(scheme.getArgb(materialColors.secondary())),
        onSecondary = Color(scheme.getArgb(materialColors.onSecondary())),
        secondaryContainer = Color(scheme.getArgb(materialColors.secondaryContainer())),
        onSecondaryContainer = Color(scheme.getArgb(materialColors.onSecondaryContainer())),
        tertiary = Color(scheme.getArgb(materialColors.tertiary())),
        onTertiary = Color(scheme.getArgb(materialColors.onTertiary())),
        tertiaryContainer = Color(scheme.getArgb(materialColors.tertiaryContainer())),
        onTertiaryContainer = Color(scheme.getArgb(materialColors.onTertiaryContainer())),
        background = Color(scheme.getArgb(materialColors.background())),
        onBackground = Color(scheme.getArgb(materialColors.onBackground())),
        surface = Color(scheme.getArgb(materialColors.surface())),
        onSurface = Color(scheme.getArgb(materialColors.onSurface())),
        surfaceVariant = Color(scheme.getArgb(materialColors.surfaceVariant())),
        onSurfaceVariant = Color(scheme.getArgb(materialColors.onSurfaceVariant())),
        surfaceTint = Color(scheme.getArgb(materialColors.surfaceTint())),
        inverseSurface = Color(scheme.getArgb(materialColors.inverseSurface())),
        inverseOnSurface = Color(scheme.getArgb(materialColors.inverseOnSurface())),
        error = Color(scheme.getArgb(materialColors.error())),
        onError = Color(scheme.getArgb(materialColors.onError())),
        errorContainer = Color(scheme.getArgb(materialColors.errorContainer())),
        onErrorContainer = Color(scheme.getArgb(materialColors.onErrorContainer())),
        outline = Color(scheme.getArgb(materialColors.outline())),
        outlineVariant = Color(scheme.getArgb(materialColors.outlineVariant())),
        scrim = Color(scheme.getArgb(materialColors.scrim())),
        surfaceBright = Color(scheme.getArgb(materialColors.surfaceBright())),
        surfaceDim = Color(scheme.getArgb(materialColors.surfaceDim())),
        surfaceContainer = Color(scheme.getArgb(materialColors.surfaceContainer())),
        surfaceContainerHigh = Color(scheme.getArgb(materialColors.surfaceContainerHigh())),
        surfaceContainerHighest = Color(scheme.getArgb(materialColors.surfaceContainerHighest())),
        surfaceContainerLow = Color(scheme.getArgb(materialColors.surfaceContainerLow())),
        surfaceContainerLowest = Color(scheme.getArgb(materialColors.surfaceContainerLowest())),
        primaryFixed = Color(scheme.getArgb(materialColors.primaryFixed())),
        primaryFixedDim = Color(scheme.getArgb(materialColors.primaryFixedDim())),
        onPrimaryFixed = Color(scheme.getArgb(materialColors.onPrimaryFixed())),
        onPrimaryFixedVariant = Color(scheme.getArgb(materialColors.onPrimaryFixedVariant())),
        secondaryFixed = Color(scheme.getArgb(materialColors.secondaryFixed())),
        secondaryFixedDim = Color(scheme.getArgb(materialColors.secondaryFixedDim())),
        onSecondaryFixed = Color(scheme.getArgb(materialColors.onSecondaryFixed())),
        onSecondaryFixedVariant = Color(scheme.getArgb(materialColors.onSecondaryFixedVariant())),
        tertiaryFixed = Color(scheme.getArgb(materialColors.tertiaryFixed())),
        tertiaryFixedDim = Color(scheme.getArgb(materialColors.tertiaryFixedDim())),
        onTertiaryFixed = Color(scheme.getArgb(materialColors.onTertiaryFixed())),
        onTertiaryFixedVariant = Color(scheme.getArgb(materialColors.onTertiaryFixedVariant())),
    )
}
