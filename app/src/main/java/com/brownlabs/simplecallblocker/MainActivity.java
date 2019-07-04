package com.brownlabs.simplecallblocker;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;

    private static MainActivity mainActivityRunningInstance;
    public static MainActivity  getInstace(){
        return mainActivityRunningInstance;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        TextView tv = findViewById(R.id.textBlockedInfo);
        int blockedCount = getIntPref("blockedCount");
        String blockedSinceDate = getStringPref("BlockedSinceDate");
        tv.setText(blockedCount +" calls blocked since "+blockedSinceDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityRunningInstance=this;
        setContentView(R.layout.activity_main);

        TextView tv1 = findViewById(R.id.textView2);
        tv1.setMovementMethod(new ScrollingMovementMethod());

        Log.d("SCB","Started the app");

        String blockedSinceDate = getStringPref("BlockedSinceDate");
        if (blockedSinceDate == null || blockedSinceDate.trim().length()==0) {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
            Calendar cal = Calendar.getInstance();
            saveStringPref("BlockedSinceDate", sdf.format(cal.getTime()));
        }

        boolean enabled = getBoolPref("enabled");

        Button button = findViewById(R.id.buttonEnable);
        TextView textView = findViewById(R.id.textCurrent);

        if (enabled) {
            button.setText("Tap to Allow All Calls");
            textView.setText("Call blocking is currently enabled");
        } else {
            button.setText("Tap to Enable Call Blocking");
            textView.setText("Call blocking is currently disabled");
        }

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean e = getBoolPref("enabled");

                Button b = findViewById(R.id.buttonEnable);
                TextView t = findViewById(R.id.textCurrent);

                if (e) {
                    b.setText("Tap to Enable Call Blocking");
                    t.setText("Call blocking is currently disabled");
                    Toast.makeText(MainActivity.getInstace(), "Allowing calls from any number", Toast.LENGTH_SHORT).show();
                    saveBoolPref("enabled", false);

                    Log.d("SCB","set enabled = false");


                } else {
                    b.setText("Tap to Allow All Calls");
                    t.setText("Call blocking is currently enabled");
                    Toast.makeText(MainActivity.getInstace(), "Allowing calls from contacts only", Toast.LENGTH_SHORT).show();
                    saveBoolPref("enabled", true);

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
                boolean grantsOkay = true;
                for (int i=0; i< grantResults.length;i++) {
                    if (grantsOkay) {
                        grantsOkay = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    }
                }
                if (grantsOkay) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission(s) NOT granted.  App won't work. Exiting", Toast.LENGTH_LONG).show();
                    this.closeNow();

                }

                return;
            }
        }
    }

    private void saveBoolPref (String key, boolean val) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }
    private boolean getBoolPref (String key) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        return settings.getBoolean (key, true);
    }
    private int getIntPref (String key) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        return settings.getInt (key, 0);
    }
    private void saveIntPref (String key, int val) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, val);
        editor.commit();
    }
    private String getStringPref (String key) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        return settings.getString (key, "");
    }
    private void saveStringPref (String key, String val) {
        SharedPreferences settings = getSharedPreferences("SCB", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, val);
        editor.commit();
    }
    private void closeNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
    }

}