package com.generativimt12.myring;
import android.content.*;import android.telephony.TelephonyManager;import android.provider.ContactsContract;import android.database.Cursor;
public class PhoneStateReceiver extends BroadcastReceiver{
 @Override public void onReceive(Context c,Intent i){
  if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())){RingService.ensurePersistent(c);return;}if(!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(i.getAction()))return;
  String s=i.getStringExtra(TelephonyManager.EXTRA_STATE);
  if(TelephonyManager.EXTRA_STATE_RINGING.equals(s)){String number=i.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);String file=findContactRingtone(c,number);RingService.playIncomingDirect(c,file);}
  else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(s)||TelephonyManager.EXTRA_STATE_IDLE.equals(s))RingService.stopIncomingDirect();
 }
 private String findContactRingtone(Context c,String number){if(number==null||number.length()==0)return null;Cursor x=null;try{x=c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,ContactsContract.CommonDataKinds.Phone.NUMBER},null,null,null);if(x==null)return null;while(x.moveToNext()){String n=x.getString(1);if(normalize(number).equals(normalize(n))){String id=x.getString(0);return c.getSharedPreferences("myring",Context.MODE_PRIVATE).getString("contact_"+id+"_file",null);}}}catch(Exception ignored){}finally{if(x!=null)x.close();}return null;}
 private String normalize(String n){return n.replaceAll("[^0-9]","").replaceFirst("^972","0");}
}