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

package com.android.customization.picker.icon.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import com.android.customization.picker.icon.data.repository.ThemePickerIconStyleRepository.Companion.ICON_THEMED
import com.android.customization.picker.icon.data.repository.ThemePickerIconStyleRepository.Companion.SET_ICON_THEMED
import com.android.customization.picker.icon.shared.model.IconStyle
import com.android.customization.picker.icon.shared.model.ThemePickerIconStyle
import com.android.wallpaper.util.PreviewUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

/**
 * DiamaneOS Tally: Home's icon style by name (Launcher3's TallyIconStyle), read and set through its
 * customization provider.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TallyIconStyleSource(
    private val contentResolver: ContentResolver,
    previewUtilsFlow: Flow<PreviewUtils?>,
    backgroundScope: CoroutineScope,
    private val backgroundDispatcher: CoroutineDispatcher,
) {
    /**
     * DiamaneOS Tally: the icon style Home (Launcher3's TallyIconStyle) has by name, or null where
     * Home does not name its style (another launcher).
     */
    val iconStyle: Flow<IconStyle?> =
        previewUtilsFlow
            .flatMapLatest {
                callbackFlow {
                    if (it == null) {
                        trySend(null)
                        awaitClose()
                        return@callbackFlow
                    }
                    val contentObserver =
                        object : ContentObserver(null) {
                            override fun onChange(selfChange: Boolean) {
                                trySend(getTallyIconStyle(it))
                            }
                        }
                    // The observers go away however the flow ends, a failed first read included.
                    try {
                        registerIconStyleObserver(it, contentObserver)
                        trySend(getTallyIconStyle(it))
                        awaitClose()
                    } finally {
                        contentResolver.unregisterContentObserver(contentObserver)
                    }
                }
            }
            .stateIn(
                scope = backgroundScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = null,
            )

    /** DiamaneOS Tally: Colour is offered where Home has it. */
    suspend fun isColourAvailable(previewUtils: PreviewUtils): Boolean =
        withContext(backgroundDispatcher) { getTallyIconStyle(previewUtils) } != null

    /** Watches every way Home's icon style can change: stock's switch and Tally's name. */
    fun registerIconStyleObserver(previewUtils: PreviewUtils, observer: ContentObserver) {
        for (path in listOf(ICON_THEMED, SET_ICON_THEMED, TALLY_ICON_STYLE)) {
            contentResolver.registerContentObserver(
                previewUtils.getUri(path),
                /* notifyForDescendants= */ true,
                observer,
            )
        }
    }

    /**
     * DiamaneOS Tally: Home's icon style by name, or null where Home does not name it. Another
     * launcher's provider may reject the unknown path instead of returning no rows.
     */
    private fun getTallyIconStyle(previewUtils: PreviewUtils): IconStyle? {
        val cursor =
            try {
                contentResolver.query(
                    previewUtils.getUri(TALLY_ICON_STYLE),
                    /* projection= */ null,
                    /* selection= */ null,
                    /* selectionArgs= */ null,
                    /* sortOrder= */ null,
                )
            } catch (e: IllegalArgumentException) {
                null
            } catch (e: UnsupportedOperationException) {
                null
            } ?: return null
        return cursor.use {
            val column = it.getColumnIndex(COL_TALLY_ICON_STYLE)
            if (column < 0 || !it.moveToNext()) return null
            when (it.getString(column)) {
                TALLY_STYLE_COLOUR -> ThemePickerIconStyle.COLOUR
                TALLY_STYLE_MINIMAL -> ThemePickerIconStyle.MONOCHROME
                TALLY_STYLE_NONE -> ThemePickerIconStyle.DEFAULT
                else -> null
            }
        }
    }

    /**
     * DiamaneOS Tally: Colour is set by name. Minimal and Default keep stock's switch, which Home
     * reads as Minimal on, or every app's own icon.
     */
    fun setColour(previewUtils: PreviewUtils): Boolean {
        val values = ContentValues()
        values.put(COL_TALLY_ICON_STYLE, TALLY_STYLE_COLOUR)
        val rowsUpdated =
            contentResolver.update(
                previewUtils.getUri(TALLY_ICON_STYLE),
                values,
                /* where= */ null,
                /* selectionArgs= */ null,
            )
        return rowsUpdated > 0
    }

    companion object {
        const val TALLY_ICON_STYLE = "tally_icon_style"
        const val COL_TALLY_ICON_STYLE = "icon_style"
        const val TALLY_STYLE_COLOUR = "colour"
        const val TALLY_STYLE_MINIMAL = "minimal"
        const val TALLY_STYLE_NONE = "none"
    }
}
