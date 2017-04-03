package us.steveboyer.sdremote;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnClickListener, PopupMenu.OnMenuItemClickListener
    {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private final static int ONE = 1;
    private final static int TWO = 2;
    private final static int THREE = 3;

    private final static int BUTTON_ZERO = 0;
    private final static int BUTTON_NULL = 1;
    private final static int BUTTON_ONE = 17;
    private final static int BUTTON_TWO = 2;
    private final static int BUTTON_THREE = 3;
    private final static int BUTTON_FOUR = 4;
    private final static int BUTTON_FIVE = 5;
    private final static int BUTTON_SIX = 6;
    private final static int BUTTON_SEVEN = 7;
    private final static int BUTTON_EIGHT = 8;
    private final static int BUTTON_NINE = 9;
    private final static int BUTTON_CHANP = 10;
    private final static int BUTTON_CHANM = 11;
    private final static int BUTTON_VOLP = 12;
    private final static int BUTTON_VOLM = 13;
    private final static int BUTTON_POWER = 14;
    private final static int BUTTON_SOURCE = 15;
    private final static int BUTTON_MUTE = 16;
    private final static int NUM_BUTTONS = 18;
        
    private SharedPreferences mainPreferences;
    private SharedPreferences.Editor prefsEditor;

    private PopupMenu popupMenu;
    private mGPIO gpio;
    private Switch recordSwitch;
    private boolean recordSwitchBool = false;
    private boolean[] buttonsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonsEnabled  = new boolean[NUM_BUTTONS];

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        popupMenu = new PopupMenu(this, findViewById(R.id.button_favs));
        popupMenu.getMenu().add(Menu.NONE, ONE, Menu.NONE, "Item 1");
        popupMenu.getMenu().add(Menu.NONE, TWO, Menu.NONE, "Item 2");
        popupMenu.getMenu().add(Menu.NONE, THREE, Menu.NONE, "Item 3");
        popupMenu.setOnMenuItemClickListener(this);
        findViewById(R.id.button_favs).setOnClickListener(this);

        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        mainPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = mainPreferences.edit();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String strUsername = SP.getString("pref_user", "webiopi");
        String strPassword = SP.getString("pref_pass", "raspberry");
        String strPort = SP.getString("pref_port", "8000");
        String strIP = SP.getString("pref_ip", "192.168.1.1");

        Log.d("Conn", "attempting connection on " + strIP + " with port " + strPort + ", pass: " + strPassword + ", user: " + strUsername);

        this.gpio = new mGPIO(new mGPIO.mConnection(strIP, Integer.parseInt(strPort), strUsername, strPassword));

        mainPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equalsIgnoreCase(getString(R.string.key_pref_reset_buttons))) {
                    if (mainPreferences.getBoolean(key, false)) {
                        editor.putBoolean(getString(R.string.key_pref_reset_buttons), false);
                        setButtonsProgrammed(false, editor);
                        sendReset();
                        if(!recordSwitchBool) {
                            setButtonsEnabled(false);
                        }
                    }
                }
            }
        });

        prefsEditor = mainPreferences.edit();

        recordSwitch = (Switch)findViewById(R.id.switch_record);
        recordSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recordSwitchBool = isChecked;
                if(isChecked){
                    setButtonsEnabled(true);
                } else {
                    buttonsEnabled[BUTTON_ZERO] = mainPreferences.getBoolean("button_0_programmed", false);
                    buttonsEnabled[BUTTON_ONE] = mainPreferences.getBoolean("button_1_programmed", false);
                    buttonsEnabled[BUTTON_TWO] = mainPreferences.getBoolean("button_2_programmed", false);
                    buttonsEnabled[BUTTON_THREE] = mainPreferences.getBoolean("button_3_programmed", false);
                    buttonsEnabled[BUTTON_FOUR] = mainPreferences.getBoolean("button_4_programmed", false);
                    buttonsEnabled[BUTTON_FIVE] = mainPreferences.getBoolean("button_5_programmed", false);
                    buttonsEnabled[BUTTON_SIX] = mainPreferences.getBoolean("button_6_programmed", false);
                    buttonsEnabled[BUTTON_SEVEN] = mainPreferences.getBoolean("button_7_programmed", false);
                    buttonsEnabled[BUTTON_EIGHT] = mainPreferences.getBoolean("button_8_programmed", false);
                    buttonsEnabled[BUTTON_NINE] = mainPreferences.getBoolean("button_9_programmed", false);
                    buttonsEnabled[BUTTON_CHANP] = mainPreferences.getBoolean("button_chanp_programmed", false);
                    buttonsEnabled[BUTTON_CHANM] = mainPreferences.getBoolean("button_chanm_programmed", false);
                    buttonsEnabled[BUTTON_POWER] = mainPreferences.getBoolean("button_power_programmed", false);
                    buttonsEnabled[BUTTON_VOLP] = mainPreferences.getBoolean("button_volp_programmed", false);
                    buttonsEnabled[BUTTON_VOLM] = mainPreferences.getBoolean("button_volm_programmed", false);
                    buttonsEnabled[BUTTON_MUTE] = mainPreferences.getBoolean("button_mute_programmed", false);
                    buttonsEnabled[BUTTON_SOURCE] = mainPreferences.getBoolean("button_source_programmed", false);

                    ((mButton)findViewById(R.id.button0)).mSetEnabled(buttonsEnabled[BUTTON_ZERO]);
                    ((mButton)findViewById(R.id.button1)).mSetEnabled(buttonsEnabled[BUTTON_ONE]);
                    ((mButton)findViewById(R.id.button2)).mSetEnabled(buttonsEnabled[BUTTON_TWO]);
                    ((mButton)findViewById(R.id.button3)).mSetEnabled(buttonsEnabled[BUTTON_THREE]);
                    ((mButton)findViewById(R.id.button4)).mSetEnabled(buttonsEnabled[BUTTON_FOUR]);
                    ((mButton)findViewById(R.id.button5)).mSetEnabled(buttonsEnabled[BUTTON_FIVE]);
                    ((mButton)findViewById(R.id.button6)).mSetEnabled(buttonsEnabled[BUTTON_SIX]);
                    ((mButton)findViewById(R.id.button7)).mSetEnabled(buttonsEnabled[BUTTON_SEVEN]);
                    ((mButton)findViewById(R.id.button8)).mSetEnabled(buttonsEnabled[BUTTON_EIGHT]);
                    ((mButton)findViewById(R.id.button9)).mSetEnabled(buttonsEnabled[BUTTON_NINE]);
                    ((mButton)findViewById(R.id.button_chanp)).mSetEnabled(buttonsEnabled[BUTTON_CHANP]);
                    ((mButton)findViewById(R.id.button_chanm)).mSetEnabled(buttonsEnabled[BUTTON_CHANM]);
                    ((mButton)findViewById(R.id.button_power)).mSetEnabled(buttonsEnabled[BUTTON_POWER]);
                    ((mButton)findViewById(R.id.button_volp)).mSetEnabled(buttonsEnabled[BUTTON_VOLP]);
                    ((mButton)findViewById(R.id.button_volm)).mSetEnabled(buttonsEnabled[BUTTON_VOLM]);
                    ((mButton)findViewById(R.id.button_source)).mSetEnabled(buttonsEnabled[BUTTON_SOURCE]);
                    ((mButton)findViewById(R.id.button_mute)).mSetEnabled(buttonsEnabled[BUTTON_MUTE]);

                }
            }
        });

        buttonsEnabled[BUTTON_ZERO] = mainPreferences.getBoolean("button_0_programmed", false);
        buttonsEnabled[BUTTON_ONE] = mainPreferences.getBoolean("button_1_programmed", false);
        buttonsEnabled[BUTTON_TWO] = mainPreferences.getBoolean("button_2_programmed", false);
        buttonsEnabled[BUTTON_THREE] = mainPreferences.getBoolean("button_3_programmed", false);
        buttonsEnabled[BUTTON_FOUR] = mainPreferences.getBoolean("button_4_programmed", false);
        buttonsEnabled[BUTTON_FIVE] = mainPreferences.getBoolean("button_5_programmed", false);
        buttonsEnabled[BUTTON_SIX] = mainPreferences.getBoolean("button_6_programmed", false);
        buttonsEnabled[BUTTON_SEVEN] = mainPreferences.getBoolean("button_7_programmed", false);
        buttonsEnabled[BUTTON_EIGHT] = mainPreferences.getBoolean("button_8_programmed", false);
        buttonsEnabled[BUTTON_NINE] = mainPreferences.getBoolean("button_9_programmed", false);
        buttonsEnabled[BUTTON_CHANP] = mainPreferences.getBoolean("button_chanp_programmed", false);
        buttonsEnabled[BUTTON_CHANM] = mainPreferences.getBoolean("button_chanm_programmed", false);
        buttonsEnabled[BUTTON_POWER] = mainPreferences.getBoolean("button_power_programmed", false);
        buttonsEnabled[BUTTON_VOLP] = mainPreferences.getBoolean("button_volp_programmed", false);
        buttonsEnabled[BUTTON_VOLM] = mainPreferences.getBoolean("button_volm_programmed", false);
        buttonsEnabled[BUTTON_MUTE] = mainPreferences.getBoolean("button_mute_programmed", false);
        buttonsEnabled[BUTTON_SOURCE] = mainPreferences.getBoolean("button_source_programmed", false);

        ((mButton)findViewById(R.id.button0)).mSetEnabled(buttonsEnabled[BUTTON_ZERO]);
        ((mButton)findViewById(R.id.button1)).mSetEnabled(buttonsEnabled[BUTTON_ONE]);
        ((mButton)findViewById(R.id.button2)).mSetEnabled(buttonsEnabled[BUTTON_TWO]);
        ((mButton)findViewById(R.id.button3)).mSetEnabled(buttonsEnabled[BUTTON_THREE]);
        ((mButton)findViewById(R.id.button4)).mSetEnabled(buttonsEnabled[BUTTON_FOUR]);
        ((mButton)findViewById(R.id.button5)).mSetEnabled(buttonsEnabled[BUTTON_FIVE]);
        ((mButton)findViewById(R.id.button6)).mSetEnabled(buttonsEnabled[BUTTON_SIX]);
        ((mButton)findViewById(R.id.button7)).mSetEnabled(buttonsEnabled[BUTTON_SEVEN]);
        ((mButton)findViewById(R.id.button8)).mSetEnabled(buttonsEnabled[BUTTON_EIGHT]);
        ((mButton)findViewById(R.id.button9)).mSetEnabled(buttonsEnabled[BUTTON_NINE]);
        ((mButton)findViewById(R.id.button_chanp)).mSetEnabled(buttonsEnabled[BUTTON_CHANP]);
        ((mButton)findViewById(R.id.button_chanm)).mSetEnabled(buttonsEnabled[BUTTON_CHANM]);
        ((mButton)findViewById(R.id.button_power)).mSetEnabled(buttonsEnabled[BUTTON_POWER]);
        ((mButton)findViewById(R.id.button_volp)).mSetEnabled(buttonsEnabled[BUTTON_VOLP]);
        ((mButton)findViewById(R.id.button_volm)).mSetEnabled(buttonsEnabled[BUTTON_VOLM]);
        ((mButton)findViewById(R.id.button_source)).mSetEnabled(buttonsEnabled[BUTTON_SOURCE]);
        ((mButton)findViewById(R.id.button_mute)).mSetEnabled(buttonsEnabled[BUTTON_MUTE]);
    }

    void setButtonsProgrammed(boolean programmed, SharedPreferences.Editor editor){
        editor.putBoolean("button_0_programmed", programmed);
        editor.putBoolean("button_1_programmed", programmed);
        editor.putBoolean("button_2_programmed", programmed);
        editor.putBoolean("button_3_programmed", programmed);
        editor.putBoolean("button_4_programmed", programmed);
        editor.putBoolean("button_5_programmed", programmed);
        editor.putBoolean("button_6_programmed", programmed);
        editor.putBoolean("button_7_programmed", programmed);
        editor.putBoolean("button_8_programmed", programmed);
        editor.putBoolean("button_9_programmed", programmed);
        editor.putBoolean("button_volm_programmed", programmed);
        editor.putBoolean("button_volp_programmed", programmed);
        editor.putBoolean("button_chanm_programmed", programmed);
        editor.putBoolean("button_chanp_programmed", programmed);
        editor.putBoolean("button_source_programmed", programmed);
        editor.putBoolean("button_power_programmed", programmed);
        editor.putBoolean("button_mute_programmed", programmed);
        editor.commit();
    }    
        
    void setButtonsEnabled(boolean enabled){
        ((mButton) findViewById(R.id.button0)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button1)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button2)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button3)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button4)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button5)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button6)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button7)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button8)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button9)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button_chanp)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button_chanm)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button_power)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button_volp)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button_volm)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button_source)).mSetEnabled(enabled);
        ((mButton) findViewById(R.id.button_mute)).mSetEnabled(enabled);
    }
        
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button0:
                if(recordSwitchBool){
                    sendRecord(BUTTON_ZERO);
                    prefsEditor.putBoolean("button_0_programmed", true);
                } else {
                    sendButton(BUTTON_ZERO);
                }
                break;
            case R.id.button1:
                if(recordSwitchBool){
                    sendRecord(BUTTON_ONE);
                    prefsEditor.putBoolean("button_1_programmed", true);
                } else {
                    sendButton(BUTTON_ONE);
                }
                break;
            case R.id.button2:
                if(recordSwitchBool){
                    sendRecord(BUTTON_TWO);
                    prefsEditor.putBoolean("button_2_programmed", true);
                } else {
                    sendButton(BUTTON_TWO);
                }
                break;
            case R.id.button3:
                if(recordSwitchBool){
                    sendRecord(BUTTON_THREE);
                    prefsEditor.putBoolean("button_3_programmed", true);
                } else {
                    sendButton(BUTTON_THREE);
                }
                break;
            case R.id.button4:
                if(recordSwitchBool){
                    sendRecord(BUTTON_FOUR);
                    prefsEditor.putBoolean("button_4_programmed", true);
                } else {
                    sendButton(BUTTON_FOUR);
                }
                break;
            case R.id.button5:
                if(recordSwitchBool){
                    sendRecord(BUTTON_FIVE);
                    prefsEditor.putBoolean("button_5_programmed", true);
                } else {
                    sendButton(BUTTON_FIVE);
                }
                break;
            case R.id.button6:
                if(recordSwitchBool){
                    sendRecord(BUTTON_SIX);
                    prefsEditor.putBoolean("button_6_programmed", true);
                } else {
                    sendButton(BUTTON_SIX);
                }
                break;
            case R.id.button7:
                if(recordSwitchBool){
                    sendRecord(BUTTON_SEVEN);
                    prefsEditor.putBoolean("button_7_programmed", true);
                } else {
                    sendButton(BUTTON_SEVEN);
                }
                break;
            case R.id.button8:
                if(recordSwitchBool){
                    sendRecord(BUTTON_EIGHT);
                    prefsEditor.putBoolean("button_8_programmed", true);
                } else {
                    sendButton(BUTTON_EIGHT);
                }
                break;
            case R.id.button9:
                if(recordSwitchBool){
                    sendRecord(BUTTON_NINE);
                    prefsEditor.putBoolean("button_9_programmed", true);
                } else {
                    sendButton(BUTTON_NINE);
                }
                break;
            case R.id.button_chanm:
                if(recordSwitchBool){
                    sendRecord(BUTTON_CHANM);
                    prefsEditor.putBoolean("button_chanm_programmed", true);
                } else {
                    sendButton(BUTTON_CHANM);
                }
                break;
            case R.id.button_chanp:
                if(recordSwitchBool){
                    sendRecord(BUTTON_CHANP);
                    prefsEditor.putBoolean("button_chanp_programmed", true);
                } else {
                    sendButton(BUTTON_CHANP);
                }
                break;
            case R.id.button_mute:
                if(recordSwitchBool){
                    sendRecord(BUTTON_MUTE);
                    prefsEditor.putBoolean("button_mute_programmed", true);
                } else {
                    sendButton(BUTTON_MUTE);
                }
                break;
            case R.id.button_volm:
                if(recordSwitchBool){
                    sendRecord(BUTTON_VOLM);
                    prefsEditor.putBoolean("button_volm_programmed", true);
                } else {
                    sendButton(BUTTON_VOLM);
                }
                break;
            case R.id.button_volp:
                if(recordSwitchBool){
                    sendRecord(BUTTON_VOLP);
                    prefsEditor.putBoolean("button_volp_programmed", true);
                } else {
                    sendButton(BUTTON_VOLP);
                }
                break;
            case R.id.button_power:
                if(recordSwitchBool){
                    sendRecord(BUTTON_POWER);
                    prefsEditor.putBoolean("button_power_programmed", true);
                } else {
                    sendButton(BUTTON_POWER);
                }
                break;
            case R.id.button_source:
                if(recordSwitchBool){
                    sendRecord(BUTTON_SOURCE);
                    prefsEditor.putBoolean("button_source_programmed", true);
                } else {
                    sendButton(BUTTON_SOURCE);
                }
            case R.id.button_favs:
                popupMenu.show();
                break;
        }
        prefsEditor.commit();
    }

    private void sendRecord(int button){
        gpio.sendMacro("sendRecord/" + Integer.toString(button));
    }

    private void sendButton(int button){
        gpio.sendMacro("sendButton/" + Integer.toString(button));
    }
        
    private void sendReset(){
        gpio.sendMacro("sendReset");
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //TODO change to selected channel
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, UserSettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
