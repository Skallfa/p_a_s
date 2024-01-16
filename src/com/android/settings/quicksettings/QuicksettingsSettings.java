/*
 * Copyright (C) 2022 The PixelDust Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.android.settings.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class QuicksettingsSettings extends DashboardFragment implements OnPreferenceChangeListener {

    private static final String TAG = "QuicksettingsSettings";
    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";

    private Handler mHandler;
    private ListPreference mQsStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQsStyle = (ListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mCustomSettingsObserver.observe();
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE))) {
                updateQsStyle();
            }
        }
    }


    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsStyle) {
           mCustomSettingsObserver.observe();
           return true;
        }
        return false;
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

        switch (qsPanelStyle) {
            case 0:
              setQsStyle("com.android.systemui");
              break;
            case 1:
              setQsStyle("com.android.system.qs.outline");
              break;
            case 2:
              setQsStyle("com.android.system.qs.twotoneaccent");
              break;
            case 3:
              setQsStyle("com.android.system.qs.shaded");
              break;
            default:
              break;
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.quicksettings_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quicksettings_settings);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.quicksettings_settings);
}
