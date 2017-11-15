package com.cwf.ceedcar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_SETTINGS = 100;
    private String ip;
    private SharedPreferences preferences;

    private String direction = "stop";

    private TextView joystickInfo;

    private int updateInterval = 100;
    private Handler mHandler;
    private HandlerThread commsThread;
    private DatagramSocket ds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commsThread = new HandlerThread("commsThread");
        commsThread.start();
        mHandler = new Handler(commsThread.getLooper());

        joystickInfo = findViewById(R.id.joystick_info);
        joystickInfo.setText("Hello!");

        preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        if(!preferences.contains("ip")) {
            Toast.makeText(this, R.string.initial_ip, Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ip", "192.168.1.100");
            editor.commit();
            ip = "192.168.1.100";
        } else {
            ip = preferences.getString("ip", "192.168.1.100");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        Joystick joystick = (Joystick) findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(float degrees, float offset) {
                if(offset >= 0.4) {
                    if(degrees >= 45 && degrees < 135) {
                        MainActivity.this.direction = "forward";
                    } else if(degrees >= 135 || degrees <= -135) {
                        MainActivity.this.direction = "left";
                    } else if(degrees > -135 && degrees <= -45) {
                        MainActivity.this.direction = "back";
                    } else {
                        MainActivity.this.direction = "right";
                    }
                } else {
                    MainActivity.this.direction = "stop";
                }
                String info = String.format(Locale.US, "Degrees: %f\nOffset: %f\nDirection: %s", degrees, offset, MainActivity.this.direction);
                joystickInfo.setText(info);
            }

            @Override
            public void onUp() {
                MainActivity.this.direction = "stop";
                String info = String.format(Locale.US, "Degrees: %f\nOffset: %f\nDirection: %s", 0.0, 0.0, MainActivity.this.direction);
                joystickInfo.setText(info);
            }
        });

        try {
            ds = new DatagramSocket();
            startCommunicationTask();
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCommunicationTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCommunicationTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCommunicationTask();
    }

    private Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            String dir = MainActivity.this.direction;
            try {
                InetAddress addr = InetAddress.getByName(MainActivity.this.ip);
                DatagramPacket dp = new DatagramPacket(dir.getBytes(), dir.length(), addr, 8008);
                ds.setBroadcast(false);
                ds.send(dp);
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                mHandler.postDelayed(mUpdater, updateInterval);
            }
        }
    };

    private void startCommunicationTask() {
        mHandler.post(mUpdater);
    }

    private void stopCommunicationTask() {
        mHandler.removeCallbacks(mUpdater);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, REQ_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQ_SETTINGS:
                if(resultCode == 1) {
                    // Let's refresh the cam
                    Toast.makeText(MainActivity.this, R.string.saved_msg, Toast.LENGTH_SHORT).show();
                    ip = preferences.getString("ip", "192.168.1.100");
                    Log.d("CEED", "New IP is: " + ip);
                    stopCommunicationTask();
                    startCommunicationTask();
                }
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
