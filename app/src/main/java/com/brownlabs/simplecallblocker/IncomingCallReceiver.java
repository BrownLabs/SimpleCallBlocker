package com.brownlabs.simplecallblocker;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class IncomingCallReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (!getBoolPref("enabled", context)) {
            return;
        }
        MainActivity mainActivity = null;

        if(MainActivity.getInstace()!=null) {
            mainActivity = MainActivity.getInstace();
        }

        ITelephony telephonyService;
        boolean foundContact = false;

        Log.d("SCB", "Got the call");

        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (number != null) {

                Log.d("SCB", "Call from " + number);

                Log.d("SCB", "Getting Contact for " + number);
                String contactName = getContactDisplayNameByNumber(number, context);

                if (contactName != null) {
                    Log.d("SCB", "Contact found: " + contactName);
                    foundContact = true;
                } else {
                    Log.d("SCB", "Contact NOT found.");
                    foundContact = false;
                }

                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Log.d("SCB", "Phone state changed to: " + tm.getCallState());

                if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                    try {

                        Class clazz = Class.forName(tm.getClass().getName());
                        Method method = clazz.getDeclaredMethod("getITelephony");
                        method.setAccessible(true);
                        telephonyService = (ITelephony) method.invoke(tm);

                        if (foundContact) {
                            Log.d("SCB", "Ringing");
                        } else {

                            telephonyService.endCall();
                            Log.d("SCB", "Contact not found. Call Blocked.");
                            Toast.makeText(context, "Contact not found. Call Blocked.", Toast.LENGTH_SHORT).show();
                            int blockedCount = getIntPref("blockedCount", context);
                            blockedCount++;
                            saveIntPref("blockedCount", blockedCount, context);
                            String blockedSinceDate = getStringPref("BlockedSinceDate", context);

                            TextView tv = mainActivity.findViewById(R.id.textBlockedInfo);
                            tv.setText(blockedCount +" calls blocked since "+blockedSinceDate);

                        }

                    } catch (Exception e) {
                        Log.e("SCB", "Exception occurred", e);
                        e.printStackTrace();
                    }
                }
                if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    Log.d("SCB", "Answered " + number);
                }
                if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                    Log.d("SCB", "Idle");
                }
            }

        } catch (Exception e) {
            Log.e("SCB", "Exception occurred", e);
            e.printStackTrace();
        }
    }

    public String getContactDisplayNameByNumber(String number, Context ctx) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(number));
        String name = null;

        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return name;
    }


    private boolean getBoolPref (String key, Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences("SCB", ctx.MODE_PRIVATE);
        return settings.getBoolean (key, true);
    }
    private int getIntPref (String key, Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences("SCB", ctx.MODE_PRIVATE);
        return settings.getInt (key, 0);
    }
    private void saveIntPref (String key, int val, Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences("SCB", ctx.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, val);
        editor.commit();
    }
    private String getStringPref (String key, Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences("SCB", ctx.MODE_PRIVATE);
        return settings.getString (key, "");
    }

}