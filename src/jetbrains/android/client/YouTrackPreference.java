package jetbrains.android.client;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class YouTrackPreference extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.youtrack_prefrences);
    }
}
