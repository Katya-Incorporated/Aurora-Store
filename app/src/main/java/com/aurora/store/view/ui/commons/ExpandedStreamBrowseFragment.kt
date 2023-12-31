package com.aurora.store.view.ui.commons

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.airbnb.epoxy.EpoxyModel
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.store.R
import com.aurora.store.databinding.ActivityGenericRecyclerBinding
import com.aurora.store.view.custom.recycler.EndlessRecyclerOnScrollListener
import com.aurora.store.view.epoxy.groups.CarouselHorizontalModel_
import com.aurora.store.view.epoxy.views.AppProgressViewModel_
import com.aurora.store.view.epoxy.views.app.AppListViewModel_
import com.aurora.store.view.epoxy.views.details.MiniScreenshotView
import com.aurora.store.view.epoxy.views.details.MiniScreenshotViewModel_
import com.aurora.store.view.epoxy.views.shimmer.AppListViewShimmerModel_
import com.aurora.store.viewmodel.browse.ExpandedStreamBrowseViewModel


class ExpandedStreamBrowseFragment : BaseFragment(R.layout.activity_generic_recycler) {

    private var _binding: ActivityGenericRecyclerBinding? = null
    private val binding: ActivityGenericRecyclerBinding
        get() = _binding!!

    private val args: ExpandedStreamBrowseFragmentArgs by navArgs()

    lateinit var VM: ExpandedStreamBrowseViewModel
    lateinit var endlessRecyclerOnScrollListener: EndlessRecyclerOnScrollListener

    lateinit var title: String
    lateinit var cluster: StreamCluster

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ActivityGenericRecyclerBinding.bind(view)
        VM = ViewModelProvider(this)[ExpandedStreamBrowseViewModel::class.java]

        // Toolbar
        binding.layoutToolbarAction.apply {
            txtTitle.text = args.title
            imgActionPrimary.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        VM.liveData.observe(viewLifecycleOwner) {
            if (!::cluster.isInitialized) attachRecycler()
            cluster = it

            updateController(cluster)
            updateTitle(cluster)
        }

        VM.getInitialCluster(args.expandedStreamUrl)
        updateController(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTitle(streamCluster: StreamCluster) {
        if (streamCluster.clusterTitle.isNotEmpty())
            binding.layoutToolbarAction.txtTitle.text = streamCluster.clusterTitle
    }

    private fun attachRecycler() {
        endlessRecyclerOnScrollListener = object : EndlessRecyclerOnScrollListener() {
            override fun onLoadMore(currentPage: Int) {
                VM.next()
            }
        }
        binding.recycler.addOnScrollListener(endlessRecyclerOnScrollListener)
    }

    private fun updateController(streamCluster: StreamCluster?) {
        binding.recycler.withModels {
            setFilterDuplicates(true)
            if (streamCluster == null) {
                for (i in 1..6) {
                    add(
                        AppListViewShimmerModel_()
                            .id(i)
                    )
                }
            } else {
                streamCluster.clusterAppList.forEach {
                    val screenshotsViewModels = mutableListOf<EpoxyModel<*>>()

                    for ((position, artwork) in it.screenshots.withIndex()) {
                        screenshotsViewModels.add(
                            MiniScreenshotViewModel_()
                                .id(artwork.url)
                                .position(position)
                                .artwork(artwork)
                                .callback(object : MiniScreenshotView.ScreenshotCallback {
                                    override fun onClick(position: Int) {
                                        openScreenshotFragment(it, position)
                                    }
                                })
                        )
                    }

                    if (screenshotsViewModels.isNotEmpty()) {
                        add(
                            CarouselHorizontalModel_()
                                .id("${it.id}_screenshots")
                                .models(screenshotsViewModels)
                        )
                    }

                    add(
                        AppListViewModel_()
                            .id(it.packageName.hashCode())
                            .app(it)
                            .click { _ -> openDetailsFragment(it) }
                    )
                }

                if (streamCluster.hasNext()) {
                    add(
                        AppProgressViewModel_()
                            .id("progress")
                    )
                }
            }
        }
    }
}
