package com.aurora.store.view.ui.sheets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aurora.Constants
import com.aurora.store.R
import com.aurora.store.data.model.SelfUpdate
import com.aurora.store.data.service.SelfUpdateService
import com.aurora.store.databinding.SheetSelfUpdateBinding

class SelfUpdateSheet : BaseBottomSheet() {

    private lateinit var B: SheetSelfUpdateBinding
    private lateinit var selfUpdate: SelfUpdate

    companion object {

        const val TAG = "ManualDownloadSheet"

        @JvmStatic
        fun newInstance(
            selfUpdate: SelfUpdate
        ): SelfUpdateSheet {
            return SelfUpdateSheet().apply {
                arguments = Bundle().apply {
                    putString(Constants.STRING_EXTRA, gson.toJson(selfUpdate))
                }
            }
        }
    }

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        B = SheetSelfUpdateBinding.inflate(inflater)
        return B.root
    }

    override fun onContentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        bundle?.let {
            val rawUpdate = bundle.getString(Constants.STRING_EXTRA, "{}")
            selfUpdate = gson.fromJson(rawUpdate, SelfUpdate::class.java)
            if (selfUpdate.versionName.isNotEmpty()) {
                inflateData()
                attachActions()
            } else {
                dismissAllowingStateLoss()
            }
        }
    }

    private fun inflateData() {
        B.txtLine2.text = ("${selfUpdate.versionName} (${selfUpdate.versionCode})")

        val messages: String = if (selfUpdate.changelog.isEmpty())
            getString(R.string.details_changelog_unavailable)
        else
            selfUpdate.changelog

        B.txtChangelog.text = messages.trim()
    }

    private fun attachActions() {
        B.btnPrimary.setOnClickListener {
            val intent = Intent(requireContext(), SelfUpdateService::class.java)
            intent.putExtra(Constants.STRING_EXTRA, gson.toJson(selfUpdate))
            requireContext().startService(intent)
            dismissAllowingStateLoss()
        }

        B.btnSecondary.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}