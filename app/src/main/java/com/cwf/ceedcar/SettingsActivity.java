package com.cwf.ceedcar;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        String ip = preferences.getString("ip", "192.168.1.100");
        final EditText editText = findViewById(R.id.edit_ip);
        editText.setText(ip);

        Button saveBtn = findViewById(R.id.button_save);
        Button cancelBtn = findViewById(R.id.button_cancel);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newIp = editText.getText().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ip", newIp);
                editor.commit();
                SettingsActivity.this.setResult(1);
                SettingsActivity.this.finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.setResult(0);
                SettingsActivity.this.finish();
            }
        });
    }
}
