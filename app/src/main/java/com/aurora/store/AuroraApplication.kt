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

package com.aurora.store

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.aurora.Constants
import com.aurora.extensions.isPAndAbove
import com.aurora.store.data.downloader.DownloadManager
import com.aurora.store.data.receiver.PackageManagerReceiver
import com.aurora.store.data.service.NotificationService
import com.aurora.store.util.CommonUtil
import com.aurora.store.util.PackageUtil
import com.tonyodev.fetch2.Fetch
import org.lsposed.hiddenapibypass.HiddenApiBypass

class AuroraApplication : Application() {

    private lateinit var fetch: Fetch
    private lateinit var packageManagerReceiver: PackageManagerReceiver

    companion object{
        val enqueuedInstalls: MutableSet<String> = mutableSetOf()
    }

    override fun onCreate() {
        super.onCreate()

        // TODO: Only exempt required APIs
        // Required for Shizuku installer
        if (isPAndAbove()) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        //Create Notification Channels : General & Alert
        createNotificationChannel()

        NotificationService.startService(this)

        fetch = DownloadManager.with(this).fetch

        packageManagerReceiver = object : PackageManagerReceiver() {

        }

        //Register broadcast receiver for package install/uninstall
        registerReceiver(packageManagerReceiver, PackageUtil.getFilter())

        CommonUtil.cleanupInstallationSessions(applicationContext)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channels = ArrayList<NotificationChannel>()
            channels.add(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ALERT,
                    getString(R.string.notification_channel_alert),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            channels.add(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_GENERAL,
                    getString(R.string.notification_channel_general),
                    NotificationManager.IMPORTANCE_MIN
                )
            )
            channels.add(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_UPDATER_SERVICE,
                    getString(R.string.notification_channel_updater_service),
                    NotificationManager.IMPORTANCE_MIN
                )
            )
            channels.add(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_UPDATES,
                    getString(R.string.notification_channel_updates),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            notificationManager.createNotificationChannels(channels)
        }
    }
}
