/*
 * Copyright (C) 2026 The DiamaneOS Project
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

package com.android.customization.model.color

import android.content.theming.ThemeStyle
import com.android.systemui.monet.ColorScheme
import com.android.systemui.monet.DynamicColors

/**
 * DiamaneOS: finds colour options that give the same colours, so Wallpaper & style shows each set
 * of colours once. The Tally palette style gives a seed the same palette in several theme styles
 * (Tonal Spot, Vibrant and Expressive, for example), and the wallpaper colours tab offered each
 * seed in each style: the same option up to three times.
 *
 * Options are compared by what applying them gives, not by their style: the colours SystemUI's
 * ThemeOverlayController writes for the option's seed and style (the system palettes and the
 * dynamic, fixed and custom colours, light and dark), built with monet's [ColorScheme] as the
 * thumbnails are. A style that gets colours of its own shows again by itself.
 */
object ColorOptionPalettes {

    /** The colours ThemeOverlayController applies, as it lists them. */
    private val APPLIED_COLORS by lazy {
        DynamicColors.getAllAccentPalette() +
            DynamicColors.getAllNeutralPalette() +
            DynamicColors.getAllDynamicColorsMapped() +
            DynamicColors.getFixedColorsMapped() +
            DynamicColors.getCustomColorsMapped()
    }

    /**
     * [options] grouped by the colours they give, in their order: the first option of each group is
     * the one to show; the others give the same colours.
     */
    fun group(options: List<ColorOption>): List<List<ColorOption>> {
        val groups = LinkedHashMap<List<Int>, MutableList<ColorOption>>()
        for (option in options) {
            groups.getOrPut(appliedColors(option)) { mutableListOf() }.add(option)
        }
        return groups.values.toList()
    }

    /** The colours applying [option] gives, light and dark, at standard contrast. */
    fun appliedColors(option: ColorOption): List<Int> = buildList {
        for (isDark in booleanArrayOf(false, true)) {
            // An option without a style is Tonal Spot, as ColorOption.isActive reads it.
            val style = option.style ?: ThemeStyle.TONAL_SPOT
            val scheme = ColorScheme(option.seedColor, isDark, style).materialScheme
            for (color in APPLIED_COLORS) {
                add(color.second.getArgb(scheme))
            }
        }
    }
}
