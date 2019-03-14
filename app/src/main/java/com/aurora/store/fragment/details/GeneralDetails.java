package com.aurora.store.fragment.details;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.store.manager.CategoryManager;
import com.aurora.store.R;
import com.aurora.store.fragment.DetailsFragment;
import com.aurora.store.model.App;
import com.aurora.store.model.ImageSource;
import com.aurora.store.sheet.MoreInfoSheet;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.Util;
import com.aurora.store.utility.ViewUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeneralDetails extends AbstractHelper {
    @BindView(R.id.icon)
    ImageView appIcon;
    @BindView(R.id.showLessMoreTxt)
    TextView showLessMoreTxt;
    @BindView(R.id.versionString)
    TextView app_version;
    @BindView(R.id.txt_updated)
    Chip txtUpdated;
    @BindView(R.id.txt_google_dependencies)
    Chip txtDependencies;
    @BindView(R.id.txt_rating)
    Chip txtRating;
    @BindView(R.id.txt_installs)
    Chip txtInstalls;
    @BindView(R.id.txt_size)
    Chip txtSize;
    @BindView(R.id.category)
    Chip category;
    @BindView(R.id.developer_layout)
    LinearLayout developerLayout;

    public GeneralDetails(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        ButterKnife.bind(this, view);
        drawAppBadge();
        if (app.isInPlayStore()) {
            drawGeneralDetails();
            drawDescription();
            setupReadMore();
        }
    }

    private void drawAppBadge() {
        if (view != null) {
            ImageSource imageSource = app.getIconInfo();
            if (null != imageSource.getApplicationInfo()) {
                appIcon.setImageDrawable(context.getPackageManager().getApplicationIcon(imageSource.getApplicationInfo()));
            } else {
                Glide.with(context)
                        .asBitmap()
                        .load(imageSource.getUrl())
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .placeholder(R.color.colorTransparent)
                                .priority(Priority.HIGH))
                        .transition(new BitmapTransitionOptions().crossFade())
                        .into(appIcon);
            }

            setText(view, R.id.displayName, app.getDisplayName());
            setText(view, R.id.packageName, app.getDeveloperName());
            drawVersion();
        }
    }

    private void drawGeneralDetails() {
        if (context != null) {
            if (app.isEarlyAccess()) {
                setText(view, R.id.rating, R.string.early_access);
            } else {
                setText(view, R.id.rating, R.string.details_rating, app.getRating().getAverage());
            }

            setText(view, R.id.category, new CategoryManager(context).getCategoryName(app.getCategoryId()));

            if (app.getPrice() != null && app.getPrice().isEmpty())
                setText(view, R.id.price, R.string.category_appFree);
            else
                setText(view, R.id.price, app.getPrice());
            setText(view, R.id.contains_ads, app.containsAds() ? R.string.details_contains_ads : R.string.details_no_ads);

            txtUpdated.setText(app.getUpdated());
            txtDependencies.setText(app.getDependencies().isEmpty()
                    ? R.string.list_app_independent_from_gsf
                    : R.string.list_app_depends_on_gsf);
            txtRating.setText(app.getLabeledRating());
            txtInstalls.setText(Util.addDiPrefix(app.getInstalls()));
            txtSize.setText(Formatter.formatShortFileSize(context, app.getSize()));

            drawOfferDetails();
            drawChanges();

            show(view, R.id.layout_main);
            //show(view, R.id.app_detail);
            show(view, R.id.related_links);
        }
    }

    private void drawChanges() {
        String changes = app.getChanges();
        if (TextUtils.isEmpty(changes)) {
            hide(view, R.id.changes_container);
        } else {
            setText(view, R.id.changes_upper, Html.fromHtml(changes).toString());
            show(view, R.id.changes_container);
        }
    }

    private void drawOfferDetails() {
        List<String> keyList = new ArrayList<>(app.getOfferDetails().keySet());
        Collections.reverse(keyList);
        for (String key : keyList) {
            addOfferItem(key, app.getOfferDetails().get(key));
        }
    }

    private void addOfferItem(String key, String value) {
        if (null == value) {
            return;
        }
        TextView itemView = new TextView(context);
        try {
            itemView.setAutoLinkMask(Linkify.ALL);
            itemView.setText(context.getString(R.string.two_items, key, Html.fromHtml(value)));
        } catch (RuntimeException e) {
            Log.w("System WebView missing: %s", e.getMessage());
            itemView.setAutoLinkMask(0);
            itemView.setText(context.getString(R.string.two_items, key, Html.fromHtml(value)));
        }
        itemView.setTextColor(ViewUtil.getStyledAttribute(context, android.R.attr.textColorPrimary));
        developerLayout.addView(itemView);
        ViewUtil.setVisibility(developerLayout, true);
    }

    private void drawVersion() {
        String versionName = app.getVersionName();
        if (TextUtils.isEmpty(versionName)) {
            return;
        }
        app_version.setText(versionName);
        app_version.setVisibility(View.VISIBLE);
        if (!app.isInstalled()) {
            return;
        }
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            String currentVersion = info.versionName;
            if (info.versionCode == app.getVersionCode() || null == currentVersion) {
                return;
            }
            String newVersion = versionName;
            if (currentVersion.equals(newVersion)) {
                newVersion = String.valueOf(app.getVersionCode());
            }
            app_version.setText(newVersion);
        } catch (PackageManager.NameNotFoundException e) {
            // We've checked for that already
        }
    }

    private void drawDescription() {
        if (context != null) {
            if (TextUtils.isEmpty(app.getDescription())) {
                hide(view, R.id.more_card);
            } else {
                show(view, R.id.more_card);
            }
        }
    }

    private void setupReadMore() {
        showLessMoreTxt.setOnClickListener(v -> {
            MoreInfoSheet mDetailsFragmentMore = new MoreInfoSheet();
            mDetailsFragmentMore.setApp(app);
            mDetailsFragmentMore.show(fragment.getChildFragmentManager(), "MORE");
        });
    }
}