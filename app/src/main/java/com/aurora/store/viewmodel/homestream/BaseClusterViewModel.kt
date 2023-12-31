package com.aurora.store.viewmodel.homestream

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.StreamHelper
import com.aurora.store.data.RequestState
import com.aurora.store.data.ViewState
import com.aurora.store.data.network.HttpClient
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.util.Log
import com.aurora.store.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

abstract class BaseClusterViewModel(application: Application) : BaseAndroidViewModel(application) {

    var authData: AuthData = AuthProvider.with(application).getAuthData()
    var streamHelper: StreamHelper = StreamHelper(authData)
        .using(HttpClient.getPreferredClient())

    val liveData: MutableLiveData<ViewState> = MutableLiveData()
    var streamBundle: StreamBundle = StreamBundle()

    lateinit var type: StreamHelper.Type
    lateinit var category: StreamHelper.Category

    open fun getStreamBundle(
        nextPageUrl: String,
        category: StreamHelper.Category,
        type: StreamHelper.Type
    ): StreamBundle {
        return if (streamBundle.streamClusters.isEmpty())
            streamHelper.getNavStream(type, category)
        else
            streamHelper.next(nextPageUrl)
    }

    override fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    if (!streamBundle.hasCluster() || streamBundle.hasNext()) {

                        //Fetch new stream bundle
                        val newBundle = getStreamBundle(
                            streamBundle.streamNextPageUrl,
                            category,
                            type
                        )

                        //Update old bundle
                        streamBundle.apply {
                            streamClusters.putAll(newBundle.streamClusters)
                            streamNextPageUrl = newBundle.streamNextPageUrl
                        }

                        //Post updated to UI
                        liveData.postValue(ViewState.Success(streamBundle))
                    } else {
                        Log.i("End of Bundle")
                        requestState = RequestState.Complete
                    }
                } catch (e: Exception) {
                    requestState = RequestState.Pending
                    liveData.postValue(ViewState.Error(e.message))
                }
            }
        }
    }

    fun observeCluster(streamCluster: StreamCluster) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    if (streamCluster.hasNext()) {
                        val newCluster =
                            streamHelper.getNextStreamCluster(streamCluster.clusterNextPageUrl)
                        updateCluster(newCluster)
                        liveData.postValue(ViewState.Success(streamBundle))
                    } else {
                        Log.i("End of cluster")
                        streamCluster.clusterNextPageUrl = String()
                    }
                } catch (e: Exception) {
                    liveData.postValue(ViewState.Error(e.message))
                }
            }
        }
    }

    private fun updateCluster(newCluster: StreamCluster) {
        streamBundle.streamClusters[newCluster.id]?.apply {
            clusterAppList.addAll(newCluster.clusterAppList)
            clusterNextPageUrl = newCluster.clusterNextPageUrl
        }
    }
}
