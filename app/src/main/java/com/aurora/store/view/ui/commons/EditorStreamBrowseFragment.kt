package com.aurora.store.view.ui.commons

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.airbnb.epoxy.EpoxyModel
import com.aurora.gplayapi.data.models.App
import com.aurora.store.R
import com.aurora.store.databinding.ActivityGenericRecyclerBinding
import com.aurora.store.view.epoxy.groups.CarouselHorizontalModel_
import com.aurora.store.view.epoxy.views.EditorHeadViewModel_
import com.aurora.store.view.epoxy.views.HorizontalDividerViewModel_
import com.aurora.store.view.epoxy.views.app.AppListViewModel_
import com.aurora.store.view.epoxy.views.details.MiniScreenshotView
import com.aurora.store.view.epoxy.views.details.MiniScreenshotViewModel_
import com.aurora.store.view.epoxy.views.shimmer.AppListViewShimmerModel_
import com.aurora.store.viewmodel.editorschoice.EditorBrowseViewModel


class EditorStreamBrowseFragment : BaseFragment(R.layout.activity_generic_recycler) {

    private var _binding: ActivityGenericRecyclerBinding? = null
    private val binding: ActivityGenericRecyclerBinding
        get() = _binding!!

    private val args: EditorStreamBrowseFragmentArgs by navArgs()

    lateinit var VM: EditorBrowseViewModel
    lateinit var title: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ActivityGenericRecyclerBinding.bind(view)
        VM = ViewModelProvider(this)[EditorBrowseViewModel::class.java]

        // Toolbar
        binding.layoutToolbarAction.apply {
            txtTitle.text = args.title
            imgActionPrimary.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        VM.liveData.observe(viewLifecycleOwner) {
            updateController(it)
        }

        VM.getEditorStreamBundle(args.browseUrl)
        updateController(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateController(appList: MutableList<App>?) {
        binding.recycler.withModels {
            setFilterDuplicates(true)
            if (appList == null) {
                for (i in 1..6) {
                    add(
                        AppListViewShimmerModel_()
                            .id(i)
                    )
                }
            } else {
                appList.forEach { app ->
                    val screenshotsViewModels = mutableListOf<EpoxyModel<*>>()

                    for ((position, artwork) in app.screenshots.withIndex()) {
                        screenshotsViewModels.add(
                            MiniScreenshotViewModel_()
                                .id(artwork.url)
                                .position(position)
                                .artwork(artwork)
                                .callback(object : MiniScreenshotView.ScreenshotCallback {
                                    override fun onClick(position: Int) {
                                        openScreenshotFragment(app, position)
                                    }
                                })
                        )
                    }

                    add(
                        AppListViewModel_()
                            .id("app_${app.id}")
                            .app(app)
                            .click { _ -> openDetailsFragment(app) }
                    )

                    app.editorReason?.let { editorReason ->
                        add(
                            EditorHeadViewModel_()
                                .id("bulletin_${app.id}")
                                .title(
                                    editorReason.bulletins
                                        .joinToString(transform = { "\n• $it" })
                                        .substringAfter(delimiter = "\n")
                                )
                                .click { _ -> openDetailsFragment(app) }
                        )
                    }

                    if (screenshotsViewModels.isNotEmpty()) {
                        add(
                            CarouselHorizontalModel_()
                                .id("screenshots_${app.id}")
                                .models(screenshotsViewModels)
                        )
                    }

                    app.editorReason?.let { editorReason ->
                        if (editorReason.description.isNotEmpty()) {
                            add(
                                EditorHeadViewModel_()
                                    .id("description_${app.id}")
                                    .title(editorReason.description)
                                    .click { _ -> openDetailsFragment(app) }
                            )
                        }
                    }

                    add(
                        HorizontalDividerViewModel_()
                            .id("divider_${app.id}")
                    )
                }
            }
        }
    }
}
