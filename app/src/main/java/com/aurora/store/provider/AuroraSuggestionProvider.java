package com.aurora.store.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aurora.store.R;
import com.aurora.store.api.PlayStoreApiAuthenticator;
import com.aurora.store.manager.BitmapManager;
import com.aurora.store.utility.Accountant;
import com.aurora.store.utility.Log;
import com.aurora.store.utility.PrefUtil;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import com.dragons.aurora.playstoreapiv2.SearchSuggestEntry;

import java.io.File;
import java.io.IOException;

public class AuroraSuggestionProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return SearchManager.SUGGEST_MIME_TYPE;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        MatrixCursor cursor = new MatrixCursor(new String[]{
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                SearchManager.SUGGEST_COLUMN_ICON_1
        });
        try {
            fill(cursor, uri);
        } catch (GooglePlayException e) {
            if (e.getCode() == 401 && PrefUtil.getBoolean(getContext(), Accountant.DUMMY_ACCOUNT)) {
                refreshAndRetry(cursor, uri);
            } else {
                Log.e(e.getMessage());
            }
        } catch (Throwable e) {
            Log.e(e.getMessage());
        }
        return cursor;
    }

    private void refreshAndRetry(MatrixCursor cursor, Uri uri) {
        try {
            new PlayStoreApiAuthenticator(getContext()).refreshToken();
            fill(cursor, uri);
        } catch (Throwable e) {
            Log.e(e.getMessage());
        }
    }

    private void fill(MatrixCursor cursor, Uri uri) throws IOException {
        String query = uri.getLastPathSegment();
        if (TextUtils.isEmpty(query) || query.equals("search_suggest_query")) {
            return;
        }
        int i = 0;
        for (SearchSuggestEntry entry : new PlayStoreApiAuthenticator(getContext()).getApi().searchSuggest(query).getEntryList()) {
            cursor.addRow(constructRow(entry, i++));
        }
    }

    private Object[] constructRow(SearchSuggestEntry entry, int id) {
        return entry.getType() == GooglePlayAPI.SEARCH_SUGGESTION_TYPE.APP.value ?
                constructAppRow(entry, id) : constructSuggestionRow(entry, id);
    }

    private Object[] constructAppRow(SearchSuggestEntry entry, int id) {
        File file = new BitmapManager(getContext()).downloadAndGetFile(entry.getImageContainer().getImageUrl());
        return new Object[]{id, entry.getTitle(), entry.getPackageNameContainer().getPackageName(),
                null != file ? Uri.fromFile(file) : R.drawable.ic_placeholder};
    }

    private Object[] constructSuggestionRow(SearchSuggestEntry entry, int id) {
        return new Object[]{id, entry.getSuggestedQuery(), entry.getSuggestedQuery(), R.drawable.ic_search};
    }
}