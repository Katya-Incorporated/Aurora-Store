/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *
 *  Aurora Store is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Store is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.store.viewmodel.all

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.App
import com.aurora.store.util.AppUtil
import com.aurora.store.util.DownloadWorkerUtil
import com.aurora.store.util.Preferences
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
@SuppressLint("StaticFieldLeak") // false positive, see https://github.com/google/dagger/issues/3253
class UpdatesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val downloadWorkerUtil: DownloadWorkerUtil
) : ViewModel() {

    private val TAG = UpdatesViewModel::class.java.simpleName

    var updateAllEnqueued: Boolean = false

    private val _updates = MutableSharedFlow<List<App>?>()
    val updates = _updates.asSharedFlow()

    val downloadsList get() = downloadWorkerUtil.downloadsList

    fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isExtendedUpdateEnabled = Preferences.getBoolean(
                    context, Preferences.PREFERENCE_UPDATES_EXTENDED
                )
                val updates =
                    AppUtil.getUpdatableApps(context, gson, !isExtendedUpdateEnabled)
                        .sortedBy { it.displayName.lowercase(Locale.getDefault()) }
                _updates.emit(updates)
            } catch (exception: Exception) {
                Log.d(TAG, "Failed to get updates", exception)
            }
        }
    }

    fun download(app: App) {
        viewModelScope.launch { downloadWorkerUtil.enqueueApp(app) }
    }

    fun cancelDownload(app: App) {
        viewModelScope.launch { downloadWorkerUtil.cancelDownload(app.packageName) }
    }

    fun cancelAll() {
        viewModelScope.launch { downloadWorkerUtil.cancelAll(true) }
    }
}
