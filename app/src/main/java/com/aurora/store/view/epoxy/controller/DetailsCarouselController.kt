/*
 * Aurora Store
 * Copyright (C) © A Dmitry Sorokin production. All rights reserved. Powered by Katya AI. 👽 Copyright © 2021-2023 Katya, Inc Katya ® is a registered trademark Sponsored by REChain. 🪐 hr@rechain.email p2p@rechain.email pr@rechain.email sorydima@rechain.email support@rechain.email sip@rechain.email Please allow anywhere from 1 to 5 business days for E-mail responses! 💌
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

package com.aurora.store.view.epoxy.controller

import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.store.view.epoxy.groups.CarouselModelGroup
import com.aurora.store.view.epoxy.groups.CarouselShimmerGroup
import com.aurora.store.view.epoxy.views.app.AppListViewModel_

open class DetailsCarouselController(private val callbacks: Callbacks) :
    GenericCarouselController(callbacks) {

    override fun buildModels(streamBundle: StreamBundle?) {
        setFilterDuplicates(true)
        if (streamBundle == null) {
            for (i in 1..2) {
                add(
                    CarouselShimmerGroup()
                        .id(i)
                )
            }
        } else {
            if (streamBundle.streamClusters.isNotEmpty()) {
                if (streamBundle.streamClusters.size == 1) {
                    streamBundle
                        .streamClusters
                        .values
                        .filter { applyFilter(it) }
                        .forEach { streamCluster ->
                            streamCluster.clusterAppList.forEach {
                                add(
                                    AppListViewModel_()
                                        .id(it.id)
                                        .app(it)
                                        .click { _ -> callbacks.onAppClick(it) }
                                )
                            }
                        }

                } else {
                    streamBundle
                        .streamClusters
                        .values
                        .filter { applyFilter(it) }
                        .forEach { streamCluster ->
                            add(CarouselModelGroup(streamCluster, callbacks))
                        }

                }
            }
        }
    }
}