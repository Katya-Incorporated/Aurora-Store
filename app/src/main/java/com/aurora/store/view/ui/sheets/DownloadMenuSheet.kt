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

package com.aurora.store.view.ui.sheets

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.aurora.extensions.copyToClipBoard
import com.aurora.extensions.toast
import com.aurora.store.R
import com.aurora.store.data.installer.AppInstaller
import com.aurora.store.data.model.DownloadStatus
import com.aurora.store.databinding.SheetDownloadMenuBinding
import com.aurora.store.util.DownloadWorkerUtil
import com.aurora.store.util.PathUtil
import com.aurora.store.viewmodel.sheets.SheetsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadMenuSheet : BottomSheetDialogFragment(R.layout.sheet_download_menu) {

    private val TAG = DownloadMenuSheet::class.java.simpleName

    private var _binding: SheetDownloadMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SheetsViewModel by viewModels()
    private val args: DownloadMenuSheetArgs by navArgs()
    private val playStoreURL = "https://play.google.com/store/apps/details?id="

    private val exportMimeType = "application/zip"

    private val requestDocumentCreation =
        registerForActivityResult(ActivityResultContracts.CreateDocument(exportMimeType)) {
            if (it != null) {
                viewModel.copyDownloadedApp(
                    requireContext(),
                    args.download.packageName,
                    args.download.versionCode,
                    it
                )
            } else {
                toast(R.string.failed_apk_export)
            }
            dismissAllowingStateLoss()
        }

    @Inject
    lateinit var downloadWorkerUtil: DownloadWorkerUtil

    @Inject
    lateinit var appInstaller: AppInstaller

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SheetDownloadMenuBinding.bind(view)

        with(binding.navigationView) {
            val downloadCompleted = args.download.downloadStatus == DownloadStatus.COMPLETED
            val downloadDir = PathUtil.getAppDownloadDir(
                requireContext(),
                args.download.packageName,
                args.download.versionCode
            )

            menu.findItem(R.id.action_cancel).isVisible = !args.download.isFinished
            menu.findItem(R.id.action_clear).isVisible = args.download.isFinished
            menu.findItem(R.id.action_install).isVisible = downloadCompleted
            menu.findItem(R.id.action_local).isVisible =
                downloadCompleted && downloadDir.listFiles() != null

            setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.action_install -> {
                        install()
                        dismissAllowingStateLoss()
                    }
                    R.id.action_copy -> {
                        requireContext().copyToClipBoard(
                            "${playStoreURL}${args.download.packageName}"
                        )
                        requireContext().toast(requireContext().getString(R.string.toast_clipboard_copied))
                        dismissAllowingStateLoss()
                    }
                    R.id.action_cancel -> {
                        findViewTreeLifecycleOwner()?.lifecycleScope?.launch(NonCancellable) {
                            downloadWorkerUtil.cancelDownload(args.download.packageName)
                        }
                        dismissAllowingStateLoss()
                    }
                    R.id.action_clear -> {
                        findViewTreeLifecycleOwner()?.lifecycleScope?.launch(NonCancellable) {
                            downloadWorkerUtil.clearDownload(
                                args.download.packageName,
                                args.download.versionCode
                            )
                        }
                        dismissAllowingStateLoss()
                    }
                    R.id.action_local -> {
                        requestDocumentCreation.launch("${args.download.packageName}.zip")
                    }
                }
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun install() {
        try {
            appInstaller.getPreferredInstaller().install(args.download)
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to install ${args.download.packageName}", exception)
            if (exception is NullPointerException) {
                requireContext().toast(R.string.installer_status_failure_invalid)
            }
        }
    }
}
