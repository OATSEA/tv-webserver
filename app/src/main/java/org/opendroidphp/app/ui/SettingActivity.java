package org.opendroidphp.app.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import org.opendroidphp.R;

public class SettingActivity extends SherlockPreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_default);
    }
}
