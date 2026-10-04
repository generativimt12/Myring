package com.generativimt12.myring;
import android.content.*;import android.telephony.TelephonyManager;
public class PhoneStateReceiver extends BroadcastReceiver{
 @Override public void onReceive(Context c,Intent i){if(!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(i.getAction()))return;String s=i.getStringExtra(TelephonyManager.EXTRA_STATE);
 if(TelephonyManager.EXTRA_STATE_RINGING.equals(s))RingService.startRinging(c);else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(s)||TelephonyManager.EXTRA_STATE_IDLE.equals(s))RingService.stopRinging(c);}
}