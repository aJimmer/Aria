package com.channelvision.aria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bluecreation.aria.R;
import com.bluecreation.melodysmart.MelodySmartDevice;
import com.bluecreation.melodysmart.RemoteCommandsService;

public class MusicManagerActivity extends AppCompatActivity implements RemoteCommandsService.Listener  {
    private RemoteCommandsService remoteCommandsService;

    /* ImageButton Resources */
    private ImageButton pauseButton;
    private ImageButton playButton;
    private ImageButton nextButton;
    private ImageButton backButton;
    private ImageButton pandoraButton;
    private ImageButton googlePlayButton;
    private ImageButton spotifyButton;
    private PackageManager pm;

    SeekBar music = null;
    AudioManager mgr = null;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            TextView artist_tv = (TextView) findViewById(R.id.textView4);
            TextView track_tv = (TextView) findViewById(R.id.textView5);

            artist_tv.setText(intent.getStringExtra("artist"));
            track_tv.setText(intent.getStringExtra("track"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_manager);

        /* ImageButton Resources */
        pauseButton = (ImageButton) findViewById(R.id.imageButton17);
        playButton = (ImageButton) findViewById(R.id.imageButton14);
        nextButton = (ImageButton) findViewById(R.id.imageButton16);
        backButton = (ImageButton) findViewById(R.id.imageButton15);
        pandoraButton = (ImageButton) findViewById(R.id.imageView2);
        googlePlayButton = (ImageButton) findViewById(R.id.imageView3);
        spotifyButton = (ImageButton) findViewById(R.id.imageView);
        pm = getPackageManager();

        /*current song receiver*/
        setUpSongInfoReceiver();

        /* remote commands */
        enableRemoteCommands();

        /* get name and title of song currently playing */
        getCurrentSong();

        /* volume slider */
        setUpVolumeSeekbar();

        /* action bar set up */
        setUpActionbar();

        /* enable pause button */
        enableLayoutButtons();

    }
    public void enableLayoutButtons(){
        /* Pause button */
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remoteCommandsService.send("MUSIC 10 PAUSE");
                remoteCommandsService.send("MUSIC 21 PAUSE");
                Log.i("Debug", "pause button clicked");

                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
            }
        });

        /* Play button */
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Debug", "play button clicked");
                remoteCommandsService.send("MUSIC 10 PLAY");
                remoteCommandsService.send("MUSIC 21 PLAY");

                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
            }
        });

        /* Next button */
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Debug", "Next button clicked");
                remoteCommandsService.send("MUSIC 10 FORWARD");
                remoteCommandsService.send("MUSIC 21 FORWARD");
            }
        });

        /* Backward button */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remoteCommandsService.send("MUSIC 10 BACKWARD");
                remoteCommandsService.send("MUSIC 21 BACKWARD");
            }
        });

        /* Open pandora */
        pandoraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean installed = false;
                try {
                    startActivities(new Intent[] {
                            pm.getLaunchIntentForPackage("com.pandora.android"),
                            //pm.getLaunchIntentForPackage("com.your.p")
                    });
                    installed = true;
                } catch (final Exception ignored) {
                    // Nothing to do

                } finally {
                    finish();
                }
                if (!installed) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.pandora.android")));

                }
            }
        });

        /* Open Google Play */
        googlePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    startActivities(new Intent[] {
                            pm.getLaunchIntentForPackage("com.google.android.music"),
                            //pm.getLaunchIntentForPackage("com.your.p")
                    });

                } catch (final Exception ignored) {
                    // Nothing to do

                } finally {
                    finish();
                }
            }
        });

        /* Open Spotify */
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean installed = false;
                try {
                    startActivities(new Intent[] {
                            pm.getLaunchIntentForPackage("com.spotify.music"),
                            //pm.getLaunchIntentForPackage("com.your.p")
                    });
                    installed = true;
                } catch (final Exception ignored) {
                    // Nothing to do


                } finally {
                    finish();
                }
                if (!installed) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")));

                }
            }
        });
    }
    public void setUpActionbar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.aria_icon_top);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    public void setUpVolumeSeekbar(){
        mgr=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        music=(SeekBar)findViewById(R.id.seekBar3);
        initBar(music, AudioManager.STREAM_MUSIC);
    }

    public void enableRemoteCommands(){
        remoteCommandsService = MelodySmartDevice.getInstance().getRemoteCommandsService();
        remoteCommandsService.registerListener(this);
        remoteCommandsService.enableNotifications(true);
    }

    public void setUpSongInfoReceiver(){
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");
        registerReceiver(mReceiver, iF);
    }

    private void initBar(SeekBar bar, final int stream) {
        bar.setMax(mgr.getStreamMaxVolume(stream));
        bar.setProgress(mgr.getStreamVolume(stream));

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress,
                                          boolean fromUser) {
                mgr.setStreamVolume(stream, progress,
                        AudioManager.FLAG_PLAY_SOUND);
            }

            public void onStartTrackingTouch(SeekBar bar) {
                // no-op
            }

            public void onStopTrackingTouch(SeekBar bar) {
                // no-op
            }
        });
    }

    @Override
    public void onDestroy() {
        remoteCommandsService.unregisterListener(this);
        remoteCommandsService.enableNotifications(false);
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    public void handleReply(final byte[] reply) {
        Log.d("TAG", "Got command response : " + new String(reply));

    }

    @Override
    public void onNotificationsEnabled(boolean state) {


    }

    public void getCurrentSong() {
        remoteCommandsService.send("MUSIC 10 PAUSE");
        remoteCommandsService.send("MUSIC 21 PAUSE");
        remoteCommandsService.send("MUSIC 10 PLAY");
        remoteCommandsService.send("MUSIC 21 PLAY");

    }

    public void startEnhancementsActivity(View view){
        Log.i("Activity", "Enhancements Acvitity has been started...");

        Intent intent = new Intent(this, PreferencesActivity.class);
        intent.putExtra( PreferencesActivity.EXTRA_SHOW_FRAGMENT, PreferencesActivity.MusicManagerFragment.class.getName() );
        intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
        startActivity(intent);
    }
}
