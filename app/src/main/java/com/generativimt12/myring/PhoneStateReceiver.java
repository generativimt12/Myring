package com.generativimt12.myring;

import android.content.*;
import android.telephony.TelephonyManager;
import android.provider.ContactsContract;
import android.database.Cursor;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static String lastNumber, lastFile;

    @Override public void onReceive(Context c,Intent i){
        if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())){ RingService.ensurePersistent(c); return; }
        if(!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(i.getAction()))return;
        String s=i.getStringExtra(TelephonyManager.EXTRA_STATE);
        if(TelephonyManager.EXTRA_STATE_RINGING.equals(s)){
            String number=i.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            String file=findContactRingtone(c,number);
            if(number!=null&&number.equals(lastNumber)&&String.valueOf(file).equals(String.valueOf(lastFile)))return;
            lastNumber=number; lastFile=file;
            RingService.playIncomingDirect(c,file);
        } else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(s)||TelephonyManager.EXTRA_STATE_IDLE.equals(s)){
            lastNumber=null; lastFile=null; RingService.stopIncomingDirect();
        }
    }

    private String findContactRingtone(Context c,String number){
        android.content.SharedPreferences p=c.getSharedPreferences("myring",Context.MODE_PRIVATE);
        if(number==null||number.length()==0)return p.getString("unknown_file",null);
        Cursor x=null;
        try{
            x=c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,ContactsContract.CommonDataKinds.Phone.NUMBER},
                null,null,null);
            if(x!=null)while(x.moveToNext()){
                String n=x.getString(1);
                if(normalize(number).equals(normalize(n))){
                    String id=x.getString(0);
                    String personal=p.getString("contact_"+id+"_file",null);
                    return personal!=null?personal:p.getString("ring_file",null);
                }
            }
        }catch(Exception ignored){}finally{if(x!=null)x.close();}
        return p.getString("unknown_file",null)!=null?p.getString("unknown_file",null):p.getString("ring_file",null);
    }

    private String normalize(String n){return n.replaceAll("[^0-9]","").replaceFirst("^972","0");}
}
