package com.aurora.store.view.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aurora.Constants
import com.aurora.extensions.browse
import com.aurora.store.R
import com.aurora.store.data.model.ExodusTracker
import com.aurora.store.data.model.Report
import com.aurora.store.data.providers.ExodusDataProvider
import com.aurora.store.databinding.ActivityGenericRecyclerBinding
import com.aurora.store.view.epoxy.views.HeaderViewModel_
import com.aurora.store.view.epoxy.views.details.ExodusViewModel_
import org.json.JSONObject

class DetailsExodusFragment : Fragment(R.layout.activity_generic_recycler) {

    private var _binding: ActivityGenericRecyclerBinding? = null
    private val binding: ActivityGenericRecyclerBinding
        get() = _binding!!

    private val args: DetailsExodusFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ActivityGenericRecyclerBinding.bind(view)

        // Toolbar
        binding.layoutToolbarAction.apply {
            txtTitle.text = ""
            toolbar.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        updateController(getExodusTrackersFromReport(args.report))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateController(reviews: List<ExodusTracker>) {
        binding.recycler.withModels {
            add(
                HeaderViewModel_()
                    .id("header")
                    .title(getString(R.string.exodus_view_report))
                    .browseUrl("browse")
                    .click { _ -> context?.browse(Constants.EXODUS_REPORT_URL + args.report.id) }
            )
            reviews.forEach {
                add(
                    ExodusViewModel_()
                        .id(it.id)
                        .tracker(it)
                        .click { _ ->
                            context?.browse(it.url)
                        }
                )
            }
        }
    }

    private fun getExodusTrackersFromReport(report: Report): List<ExodusTracker> {
        val trackerObjects: List<JSONObject> = fetchLocalTrackers(report.trackers)
        return trackerObjects.map {
            ExodusTracker().apply {
                id = it.getInt("id")
                name = it.getString("name")
                url = it.getString("website")
                signature = it.getString("code_signature")
                date = it.getString("creation_date")
                description = it.getString("description")
                networkSignature = it.getString("network_signature")
                documentation = listOf(it.getString("documentation"))
                categories = listOf(it.getString("categories"))
            }
        }.toList()
    }

    private fun fetchLocalTrackers(trackerIds: List<Int>): List<JSONObject> {
        return try {
            ExodusDataProvider
                .with(requireContext())
                .getFilteredTrackers(trackerIds)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
