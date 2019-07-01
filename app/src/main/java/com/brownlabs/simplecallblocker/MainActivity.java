package com.brownlabs.simplecallblocker;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("SCB","Started the app");

        boolean enabled = getBool("enabled");

        Button button = findViewById(R.id.buttonEnable);
        TextView textView = findViewById(R.id.textCurrent);

        if (enabled) {
            button.setText("Allow All Calls");
            textView.setText("Call blocking is currently enabled");
        } else {
            button.setText("Enable Call Blocking");
            textView.setText("Call blocking is currently disabled");
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean e = getBool("enabled");

                Button b = findViewById(R.id.buttonEnable);
                TextView t = findViewById(R.id.textCurrent);

                if (e) {
                    b.setText("Enable Call Blocking");
                    t.setText("Call blocking is currently disabled");
                    saveBool("enabled", false);

                    Log.d("SCB","set enabled = false");


                } else {
                    b.setText("Allow All Calls");
                    t.setText("Call blocking is currently enabled");
                    saveBool("enabled", true);

                    Log.d("SCB","set enabled = true");

                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
            {
                String[] permissions = {Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CONTACTS};
                requestPermissions(permissions, PERMISSION_REQUEST_READ_PHONE_STATE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission(s) NOT granted.  App won't work.", Toast.LENGTH_SHORT).show();
                }

                return;
            }
        }
    }

    private void saveBool (String key, boolean val) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    private boolean getBool (String key) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        return settings.getBoolean (key, true);
    }
}