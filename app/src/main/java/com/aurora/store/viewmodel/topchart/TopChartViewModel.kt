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

package com.aurora.store.viewmodel.topchart

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.TopChartsHelper
import com.aurora.store.data.network.HttpClient
import com.aurora.store.data.providers.AuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

@HiltViewModel
@SuppressLint("StaticFieldLeak") // false positive, see https://github.com/google/dagger/issues/3253
class TopChartViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val authData: AuthData = AuthProvider.with(context).getAuthData()
    private val topChartsHelper: TopChartsHelper =
        TopChartsHelper(authData).using(HttpClient.getPreferredClient(context))

    val liveData: MutableLiveData<StreamCluster> = MutableLiveData()
    var streamCluster: StreamCluster = StreamCluster()

    fun getStreamCluster(type: TopChartsHelper.Type, chart: TopChartsHelper.Chart) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                streamCluster = topChartsHelper.getCluster(type, chart)
                liveData.postValue(streamCluster)
            } catch (_: Exception) {
            }
        }
    }

    fun nextCluster() {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    if (streamCluster.hasNext()) {
                        val newCluster = topChartsHelper.getNextStreamCluster(
                            streamCluster.clusterNextPageUrl
                        )

                        streamCluster.apply {
                            clusterAppList.addAll(newCluster.clusterAppList)
                            clusterNextPageUrl = newCluster.clusterNextPageUrl
                        }

                        liveData.postValue(streamCluster)
                    } else {
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}
