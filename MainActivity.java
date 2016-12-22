package com.channelvision.aria;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bluecreation.aria.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set logo in toolbar
        setUpToolbar();

        setContentView(R.layout.activity_main);
    }

    public void setUpToolbar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.aria_icon_top);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    public void startSettings(View view){
        Toast.makeText(this, "Connect Bluetooth device first!",Toast.LENGTH_LONG).show();
    }

    public void startMusicActivity(View view){
        Toast.makeText(this, "Connect Bluetooth device first!",Toast.LENGTH_LONG).show();
    }

    public void startEnhancementsActivity(View view){
        Toast.makeText(this, "Connect Bluetooth device first!",Toast.LENGTH_LONG).show();
    }

    public void startScanActivity(View view){
        Log.i("Activity", "Scan Activity has been started...");

        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                String bt_device_name = data.getStringExtra("bt_device_name");
                String bt_device_address = data.getStringExtra("bt_device_address");

                Intent intent = DeviceActivity.getIntent(MainActivity.this, bt_device_name, bt_device_address);
                startActivity(intent);
                Log.i("Device", "Success! Device Name: " + bt_device_name + " | " + "Device Address: " + bt_device_address);
            }
        }
    }
}
