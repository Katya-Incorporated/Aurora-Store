package com.aurora.store.utility;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.aurora.store.R;

public class ThemeUtil {

    public static final int PREF_DEFAULT_THEME = 0;

    private int currentTheme;

    public void onCreate(AppCompatActivity activity) {
        currentTheme = getSelectedTheme(activity);
        activity.setTheme(currentTheme);
    }

    public void onResume(AppCompatActivity activity) {
        if (currentTheme != getSelectedTheme(activity)) {
            Intent intent = activity.getIntent();
            activity.finish();
            OverridePendingTransition.invoke(activity);
            activity.startActivity(intent);
            OverridePendingTransition.invoke(activity);
        }
    }

    private int getSelectedTheme(AppCompatActivity activity) {
        String theme = Util.getTheme(activity);
        switch (theme) {
            case "light":
                return R.style.AppTheme;
            case "dark":
                return R.style.AppTheme_Dark;
            case "black":
                return R.style.AppTheme_Black;
            default:
                return R.style.AppTheme;
        }
    }

    private static final class OverridePendingTransition {
        static void invoke(AppCompatActivity activity) {
            activity.overridePendingTransition(0, 0);
        }
    }
}