package com.aurora.store.view.ui.sheets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.aurora.extensions.isRAndAbove
import com.aurora.extensions.openInfo
import com.aurora.extensions.toast
import com.aurora.store.R
import com.aurora.store.data.event.BusEvent
import com.aurora.store.data.installer.AppInstaller
import com.aurora.store.data.providers.BlacklistProvider
import com.aurora.store.databinding.SheetAppMenuBinding
import com.aurora.store.util.PackageUtil
import com.aurora.store.viewmodel.sheets.SheetsViewModel
import org.greenrobot.eventbus.EventBus

class AppMenuSheet : BaseBottomSheet() {

    private lateinit var B: SheetAppMenuBinding

    private val viewModel: SheetsViewModel by viewModels()
    private val args: AppMenuSheetArgs by navArgs()

    private val startForPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                viewModel.copyApk(requireContext(), args.app.packageName)
            } else {
                toast(R.string.permissions_denied)
            }
        }

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        B = SheetAppMenuBinding.inflate(layoutInflater)
        return B.root
    }

    override fun onContentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val blacklistProvider = BlacklistProvider.with(requireContext())
        val isBlacklisted: Boolean = blacklistProvider.isBlacklisted(args.app.packageName)

        with(B.navigationView) {
            //Switch strings for Add/Remove Blacklist
            val blackListMenu: MenuItem = menu.findItem(R.id.action_blacklist)
            blackListMenu.setTitle(
                if (isBlacklisted)
                    R.string.action_whitelist
                else
                    R.string.action_blacklist_add
            )

            //Show/Hide actions based on installed status
            val installed = PackageUtil.isInstalled(requireContext(), args.app.packageName)
            menu.findItem(R.id.action_uninstall).isVisible = installed
            menu.findItem(R.id.action_local).isVisible = installed

            setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.action_blacklist -> {

                        if (isBlacklisted) {
                            blacklistProvider.whitelist(args.app.packageName)
                            requireContext().toast(R.string.toast_apk_whitelisted)
                        } else {
                            blacklistProvider.blacklist(args.app.packageName)
                            requireContext().toast(R.string.toast_apk_blacklisted)
                        }

                        EventBus.getDefault()
                            .post(BusEvent.Blacklisted(args.app.packageName, ""))
                    }

                    R.id.action_local -> {
                        export()
                    }

                    R.id.action_uninstall -> {
                        AppInstaller.getInstance(requireContext())
                            .getPreferredInstaller().uninstall(args.app.packageName)
                    }

                    R.id.action_info -> {
                        requireContext().openInfo(args.app.packageName)
                    }
                }
                dismissAllowingStateLoss()
                false
            }
        }
    }

    private fun export() {
        if (isRAndAbove()) {
            if (!Environment.isExternalStorageManager()) {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            } else {
                viewModel.copyApk(requireContext(), args.app.packageName)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.copyApk(requireContext(), args.app.packageName)
            } else {
                startForPermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    companion object {
        const val TAG = "APP_MENU_SHEET"
    }
}
