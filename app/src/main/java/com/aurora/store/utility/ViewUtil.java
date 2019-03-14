package com.aurora.store.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.store.R;
import com.aurora.store.model.MenuEntry;

import java.util.ArrayList;
import java.util.List;

public class ViewUtil {

    public static void setCustomColors(Context mContext, SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeColors(mContext
                .getResources()
                .getIntArray(R.array.colorShades));
    }

    @NonNull
    public static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static int getStyledAttribute(Context context, int styleID) {
        TypedArray arr = context.obtainStyledAttributes(new TypedValue().data, new int[]{styleID});
        int styledColor = arr.getColor(0, -1);
        arr.recycle();
        return styledColor;
    }

    public static void setText(TextView textView, String text) {
        if (null != textView)
            textView.setText(text);
    }

    public static void setText(View v, TextView textView, int stringId, Object... text) {
        setText(textView, v.getResources().getString(stringId, text));
    }

    public static void hideBottomNav(View view, boolean withAnimation) {
        ViewCompat.animate(view)
                .translationY(view.getHeight())
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(withAnimation ? 300 : 0)
                .start();
    }

    public static void showBottomNav(View view, boolean withAnimation) {
        ViewCompat.animate(view)
                .translationY(0)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(withAnimation ? 300 : 0)
                .start();
    }

    public static void showWithAnimation(View view) {
        int mShortAnimationDuration = view.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

    }

    public static void hideWithAnimation(View view) {
        int mShortAnimationDuration = view.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        view.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    public static void setVisibility(View view, boolean visibility) {
        if (visibility)
            showWithAnimation(view);
        else
            hideWithAnimation(view);
    }

    public static List<MenuEntry> parseMenu(Context context, @MenuRes int menuRes) {
        List<MenuEntry> menuEntryList = new ArrayList<>();
        PopupMenu p = new PopupMenu(context, null);
        Menu menu = p.getMenu();
        new MenuInflater(context).inflate(menuRes, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            menuEntryList.add(new MenuEntry(item.getTitle().toString(), item.getIcon(), item.getItemId()));
        }
        return menuEntryList;
    }

}