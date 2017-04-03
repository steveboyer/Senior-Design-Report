package us.steveboyer.sdremote;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

/**
 * Created by steve on 4/1/15.
 */
public class UserSettingsActivity extends PreferenceActivity {

    public final static String MAIN_PREFERENCES = "MainPreferences";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            PreferenceScreen screen = getPreferenceScreen();
            screen.removePreference(findPreference("button_0_programmed"));
            screen.removePreference(findPreference("button_1_programmed"));
            screen.removePreference(findPreference("button_2_programmed"));
            screen.removePreference(findPreference("button_3_programmed"));
            screen.removePreference(findPreference("button_4_programmed"));
            screen.removePreference(findPreference("button_5_programmed"));
            screen.removePreference(findPreference("button_6_programmed"));
            screen.removePreference(findPreference("button_7_programmed"));
            screen.removePreference(findPreference("button_8_programmed"));
            screen.removePreference(findPreference("button_9_programmed"));
            screen.removePreference(findPreference("button_volm_programmed"));
            screen.removePreference(findPreference("button_volp_programmed"));
            screen.removePreference(findPreference("button_chanm_programmed"));
            screen.removePreference(findPreference("button_chanp_programmed"));
            screen.removePreference(findPreference("button_mute_programmed"));
            screen.removePreference(findPreference("button_source_programmed"));
            screen.removePreference(findPreference("button_power_programmed"));
        }
    }
}
