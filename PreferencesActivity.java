package com.channelvision.aria;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.bluecreation.aria.R;
import com.bluecreation.melodysmart.BLEError;
import com.bluecreation.melodysmart.DeviceDatabase;
import com.bluecreation.melodysmart.MelodySmartDevice;
import com.bluecreation.melodysmart.MelodySmartListener;
import com.bluecreation.melodysmart.RemoteCommandsService;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class PreferencesActivity extends AppCompatPreferenceActivity{

    /**
     * A preference value change listener that fires commands to BC-127 module


     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {


        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.aria_icon_top);
            actionBar.setDisplayUseLogoEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || MusicManagerFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, RemoteCommandsService.Listener  {

        // Macros for preference decision making
        public static final String BT_NAME = "example_text";
        public static final String PASSCODE = "pref_title_pc";
        public static final String Discoverable = "Discoverable";
        public static final String FIRMWARE = "pref_title_fu";
        public static final String RESET = "pref_title_reset";
        public static final String FACTORY_SETTINGS = "pref_title_fs";
        private Preference update;
        private Preference reset_pref;
        private Preference restore_pref;
        private RemoteCommandsService remoteCommandsService;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(false);

            update = findPreference(FIRMWARE);
            reset_pref = findPreference(RESET);
            restore_pref = findPreference(FACTORY_SETTINGS);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            enableRemoteCommands();

            // Reset, Update, and Restore  are custom preferences
            enableCustomPreferences();
        }

        public void enableCustomPreferences(){
            reset_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                public boolean onPreferenceClick(Preference preference) {

                    //open browser or intent here
                    Log.i("Preferences", "Device has been reset");
                    remoteCommandsService.send("RESET");
                    return true;
                }
            });

            restore_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


                public boolean onPreferenceClick(Preference preference) {

                    //open browser or intent here
                    Log.i("Preferences", "Device has been restored");
                    remoteCommandsService.send("RESTORE");
                    return true;
                }
            });

            update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i("Preferences", "Coming soon...");
                    return false;
                }
            });
        }

        public void enableRemoteCommands(){
            // RemoteService allows sending commands throughout the Setting Preference Activity
            remoteCommandsService = MelodySmartDevice.getInstance().getRemoteCommandsService();
            remoteCommandsService.registerListener(this);
            remoteCommandsService.enableNotifications(true);
        }

        /**
         * Used to inflate the Setting view
         * Code to register and unregister a preference change listener
         * for updating preferences which will fire commands to BC-127 module
         *
         */

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Log.i("Preferences", "Preference updated...");

            if(key.equals(BT_NAME)){
                // send command to module
                Log.i("Preferences", "Bluetooth device name updated");
                String bt_name = sharedPreferences.getString(BT_NAME,null);
                remoteCommandsService.send("SET NAME=" + bt_name);
                //Toast.makeText(getContext(),"Bluetooth device name has been updated to: " + bt_name,Toast.LENGTH_SHORT);
            }
            if(key.equals(PASSCODE)){
                // send passcode to module
                Log.i("Preferences", "Passcode udpated");
                String bt_pin = sharedPreferences.getString(PASSCODE,null);
                remoteCommandsService.send("SET PIN=" + bt_pin);
                //getPreferenceScreen().findPreference(key).;

            }
            if(key.equals(Discoverable)){
                // send Discoverable command to module
                Log.i("Preferences", "Discoverable updated");
                Boolean on = sharedPreferences.getBoolean(Discoverable,true);

                if(on) {
                    Log.i("Preference", "Switch is on");
                    remoteCommandsService.send("SET DISCOVERABLE 1 0");
                } else {
                    Log.i("Preference", "Switch is off");
                    remoteCommandsService.send("SET DISCOVERABLE 2 0");
                }
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), PreferencesActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onDestroy() {
            remoteCommandsService.unregisterListener(this);
            remoteCommandsService.enableNotifications(false);
            super.onDestroy();
        }

        @Override
        public void handleReply(final byte[] reply) {
            Log.d("TAG", "Got command response : " + new String(reply));
            /*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.remoteCommandResponseEditText.append(new String(reply) + "\n");
                }
            });
            */
        }

        @Override
        public void onNotificationsEnabled(boolean state) {

        }
    }

    /**
     * Used to inflate the preferences in the Music Manager view.
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MusicManagerFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, RemoteCommandsService.Listener, MelodySmartListener {
        private MelodySmartDevice device;
        //Macros for preference decision making
        public static final String THREE_D_SOUND = "notifications_new_message";
        public static final String BASS_BOOST = "enhancements_bass_boost";
        public static final String EQUALIZER = "enhancements_equali";
        private RemoteCommandsService remoteCommandsService;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_music_manager);
            setHasOptionsMenu(false);
            //this.getActivity().setTitle("Enhancements");

            device = MelodySmartDevice.getInstance();
            device.registerListener(this);

            Intent intent = super.getActivity().getIntent();
            String deviceAddress = intent.getStringExtra("getDeviceAddress");
            //String deviceName = intent.getStringExtra("deviceName");

            try {
                device.connect(deviceAddress);
            } catch (Exception e) {

            }
            // RemoteService allows sending commands throughout the Setting Preference Activity
            remoteCommandsService = MelodySmartDevice.getInstance().getRemoteCommandsService();
            remoteCommandsService.registerListener(this);
            remoteCommandsService.enableNotifications(true);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(EQUALIZER));
        }

        /**
         * Code to register and unregister a preference change listener
         * for updating preferences which will fire commands to BC-127 module
         *
         */

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            //Switch Preferences
            if(key.equals(THREE_D_SOUND)){
                //send 3D sound command to module
                Log.i("Preferences:", "3D Sound has been updated");
                Boolean on = sharedPreferences.getBoolean(THREE_D_SOUND,true);

                if(on) {
                    Log.i("Preference", "Switch is on");
                    device.getDataService().send("2F01F1N3F".getBytes());
                } else {
                    Log.i("Preference", "Switch is off");
                    device.getDataService().send("2F05F".getBytes());
                }

            }
            if(key.equals(BASS_BOOST)){
                //send Bass Boost command to module
                Log.i("Preferences:", "Bass Boost has been updated");
                Boolean on = sharedPreferences.getBoolean(BASS_BOOST,true);

                if(on) {
                    Log.i("Preference", "Switch is on");
                    device.getDataService().send("2F01N4F".getBytes());
                } else {
                    Log.i("Preference", "Switch is off");
                    device.getDataService().send("2F05F".getBytes());
                }
            }

            //List Preference
            if(key.equals(EQUALIZER)){
                //send Eqaulizer command to module through the chosen setting
                String eq_Pref = sharedPreferences.getString(EQUALIZER,null);
                Log.i("Preferences:", "Equalizer preference has been updated to:" + eq_Pref);

                if(eq_Pref.equals("Bass boost")){
                    device.getDataService().send("2F25F".getBytes());
                    Log.i("Preferences:", "BASS");
                }
                if(eq_Pref.equals("Treble boost")){
                    device.getDataService().send("2F35F".getBytes());
                    Log.i("Preferences:", "Treble boost");
                }
                if(eq_Pref.equals("Rock")){
                    device.getDataService().send("2F45F".getBytes());
                    Log.i("Preferences:", "ROCK");
                }
                if(eq_Pref.equals("Jazz")) {
                    device.getDataService().send("2F55F".getBytes());
                    Log.i("Preferences:", "JAZZ");
                }
                if(eq_Pref.equals("Default")){
                    device.getDataService().send("2F65F".getBytes());
                    Log.i("Preferences:", "DEFAULT");
                }
            }
        }
        @Override
        public void onDeviceConnected() {

        }

        @Override
        public void onReadRemoteRssi(int rssi) {
        }

        @Override
        public void onOtauAvailable() {

        }

        @Override
        public void onDeviceDisconnected(final BLEError error) {

        }
        @Override
        public void onOtauRecovery(DeviceDatabase.DeviceData deviceData) {
            // Automatically go to OTAU
            //startOtauActivity(true, deviceData);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            remoteCommandsService.unregisterListener(this);
            remoteCommandsService.enableNotifications(false);
            super.onDestroy();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), PreferencesActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void handleReply(final byte[] reply) {
            Log.d("TAG", "Got command response : " + new String(reply));
/*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.remoteCommandResponseEditText.append(new String(reply) + "\n");
                }
            });
            */
        }

        @Override
        public void onNotificationsEnabled(boolean state) {


        }
    }
}

