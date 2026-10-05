package com.generativimt12.myring;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.os.SystemClock;
import android.widget.*;
import java.io.*;

public class MainActivity extends Activity {
    private static final int PICK_AUDIO=42, REQ_STORAGE=43, REQ_PHONE=44, PICK_CONTACT=45, PICK_PERSONAL_AUDIO=46, REQ_CONTACTS=47, PICK_UNKNOWN_AUDIO=48, REQ_NOTIFICATIONS=49;
    private SharedPreferences prefs;
    private TextView status, modeLabel;
    private Button testButton;
    private boolean testPlaying=false;
    private String pendingContactKey;
    private long poundDownAt=0;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("myring",MODE_PRIVATE);
        requestPermissionsIfNeeded();
        buildUi();
        if(prefs.getBoolean("armed",false)) RingService.ensurePersistent(this);
    }

    private void requestPermissionsIfNeeded(){
        if(android.os.Build.VERSION.SDK_INT>=23){
            if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},REQ_PHONE);
            else if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},REQ_CONTACTS);
            else if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIFICATIONS);
        }
    }

    private void buildUi(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32,28,32,28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title=new TextView(this);
        title.setText("Myring 2.0");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        modeLabel=new TextView(this);
        modeLabel.setGravity(Gravity.CENTER);
        modeLabel.setTextSize(16);
        modeLabel.setPadding(0,8,0,12);
        root.addView(modeLabel);

        Button mode=new Button(this);
        mode.setText("החלף בין מצב פשוט למתקדם");
        mode.setOnClickListener(v->{
            boolean advanced=!prefs.getBoolean("advanced_mode",false);
            prefs.edit().putBoolean("advanced_mode",advanced).apply();
            buildUi();
        });
        root.addView(mode);

        status=new TextView(this);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0,14,0,18);
        root.addView(status);

        if(!prefs.getBoolean("advanced_mode",false)) buildSimple(root);
        else buildAdvanced(root);

        setContentView(root);
        refreshStatus();
    }

    private void buildSimple(LinearLayout root){
        addButton(root,"בחר צלצול ברירת מחדל",v->pickAudio(PICK_AUDIO));
        addButton(root,"הוסף צלצול לאיש קשר",v->pickContact());
        addButton(root,"נהל צלצולים לאנשי קשר",v->showContactRingtones());
        addButton(root,"הפעל תיקון צלצול",v->armFix());
        addButton(root,"שחזר את הצלצול המקורי",v->restoreOriginal());
        testButton=new Button(this);
        testButton.setText("בדיקת הצלצול שנבחר");
        testButton.setOnClickListener(v->toggleTest());
        root.addView(testButton);
    }

    private void buildAdvanced(LinearLayout root){
        TextView h=new TextView(this);
        h.setText("מצב מתקדם — שליטה חכמה בצלצולים");
        h.setTextSize(21);
        h.setGravity(Gravity.CENTER);
        h.setPadding(0,8,0,16);
        root.addView(h);

        addButton(root,"🎵 צלצול ברירת מחדל",v->pickAudio(PICK_AUDIO));
        addButton(root,"👤 צלצולים לאנשי קשר",v->showContactRingtones());
        addButton(root,"➕ הוסף/החלף צלצול לאיש קשר",v->pickContact());
        addButton(root,"❓ צלצול למספרים לא מזוהים",v->pickAudio(PICK_UNKNOWN_AUDIO));
        addButton(root,"⚙ בדיקת תקינות מלאה",v->showDiagnostics());
        addButton(root,"▶ בדיקת הצלצול",v->toggleTest());
        addButton(root,prefs.getBoolean("armed",false)?"⏹ כבה את Myring":"▶ הפעל את Myring",v->{
            if(prefs.getBoolean("armed",false)) restoreOriginal(); else armFix();
        });
        addButton(root,"♻ שחזר צלצול מערכת מקורי",v->restoreOriginal());

        TextView info=new TextView(this);
        info.setText("כל התכונות של המצב הפשוט נשארות זמינות.\nהמצב המתקדם מוסיף כללים, אבחון ושליטה.");
        info.setGravity(Gravity.CENTER);
        info.setPadding(0,18,0,0);
        root.addView(info);
    }

    private void addButton(LinearLayout root,String text,View.OnClickListener l){
        Button b=new Button(this); b.setText(text); b.setOnClickListener(l); root.addView(b);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_POUND){
            if(event.getRepeatCount()==0)poundDownAt=SystemClock.uptimeMillis();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_POUND){
            long held=SystemClock.uptimeMillis()-poundDownAt;
            poundDownAt=0;
            if(held>=450 && RingService.isIncomingRinging()){
                RingService.muteIncoming();
                Toast.makeText(this,"הצלצול הושתק",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onKeyUp(keyCode,event);
    }

    private void toggleTest(){
        if(testPlaying){RingService.stopTest(this);testPlaying=false;if(testButton!=null)testButton.setText("בדיקת הצלצול שנבחר");}
        else{RingService.startTest(this);testPlaying=true;if(testButton!=null)testButton.setText("עצור את בדיקת הצלצול");}
    }

    private TextView settingLabel(String s){TextView t=new TextView(this);t.setText(s);t.setPadding(0,10,0,2);return t;}

    private void showAudioSettings(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(20,5,20,0);
        SeekBar vol=new SeekBar(this);vol.setMax(100);vol.setProgress(prefs.getInt("audio_volume",100));
        TextView volL=settingLabel("עוצמת הצלצול: "+vol.getProgress()+"%");
        SeekBar pitch=new SeekBar(this);pitch.setMax(150);pitch.setMin(50);pitch.setProgress(prefs.getInt("audio_pitch",100));
        TextView pitchL=settingLabel("גובה הצליל: "+String.format(java.util.Locale.US,"%.2fx",pitch.getProgress()/100f));
        SeekBar fade=new SeekBar(this);fade.setMax(3000);fade.setProgress(prefs.getInt("fade_in_ms",0));
        TextView fadeL=settingLabel("כניסה הדרגתית (Fade-in): "+fade.getProgress()+" ms");
        box.addView(volL);box.addView(vol);box.addView(pitchL);box.addView(pitch);box.addView(fadeL);box.addView(fade);
        vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){volL.setText("עוצמת הצלצול: "+p+"%");}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){pitchL.setText("גובה הצליל: "+String.format(java.util.Locale.US,"%.2fx",p/100f));}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        fade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){fadeL.setText("כניסה הדרגתית (Fade-in): "+p+" ms");}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        new AlertDialog.Builder(this).setTitle("שליטה מתקדמת באודיו").setView(box)
            .setPositiveButton("שמור",(d,w)->prefs.edit().putInt("audio_volume",vol.getProgress()).putInt("audio_pitch",pitch.getProgress()).putInt("fade_in_ms",fade.getProgress()).apply())
            .setNegativeButton("ביטול",null).show();
    }

    private void showRouteSettings(){
        String[] choices={"אוטומטי — Android יבחר","רק הרמקול של הטלפון","רק Bluetooth כשמחובר"};
        int checked=prefs.getInt("audio_route",0);
        new AlertDialog.Builder(this).setTitle("יציאת הצלצול").setSingleChoiceItems(choices,checked,(d,w)->{
            prefs.edit().putInt("audio_route",w).apply();
            d.dismiss();
            Toast.makeText(this,choices[w],Toast.LENGTH_SHORT).show();
        }).setNegativeButton("סגור",null).show();
    }

    private void pickAudio(int request){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("audio/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,request);
    }

    private void pickContact(){
        if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},REQ_CONTACTS); return;
        }
        startActivityForResult(new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI),PICK_CONTACT);
    }

    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);
        if(c!=RESULT_OK||d==null||d.getData()==null)return;
        if(r==PICK_AUDIO||r==PICK_PERSONAL_AUDIO||r==PICK_UNKNOWN_AUDIO){
            Uri u=d.getData();
            if(r==PICK_AUDIO) saveAudio(u,false,null);
            else if(r==PICK_UNKNOWN_AUDIO) saveAudio(u,false,"UNKNOWN");
            else saveAudio(u,true,pendingContactKey);
        } else if(r==PICK_CONTACT){
            Cursor x=null;
            try{
                x=getContentResolver().query(d.getData(),new String[]{ContactsContract.Contacts._ID,ContactsContract.Contacts.DISPLAY_NAME},null,null,null);
                if(x!=null&&x.moveToFirst()){
                    pendingContactKey=x.getString(0);
                    prefs.edit().putString("pending_name",x.getString(1)).apply();
                    pickAudio(PICK_PERSONAL_AUDIO);
                }
            }finally{if(x!=null)x.close();}
        }
    }

    private void saveAudio(Uri u,boolean personal,String key){
        try{
            String ext=".audio"; String mime=getContentResolver().getType(u);
            if(mime!=null){if(mime.contains("mpeg"))ext=".mp3";else if(mime.contains("wav"))ext=".wav";else if(mime.contains("ogg"))ext=".ogg";else if(mime.contains("aac"))ext=".aac";}
            String fileName;
            if("UNKNOWN".equals(key)) fileName="unknown_ringtone"+ext;
            else fileName=personal?"contact_ringtone_"+key+ext:"selected_ringtone"+ext;
            File f=new File(getFilesDir(),fileName);
            InputStream in=getContentResolver().openInputStream(u); if(in==null)throw new IOException("cannot read");
            OutputStream out=new FileOutputStream(f); byte[] b=new byte[8192]; int n;
            while((n=in.read(b))>0)out.write(b,0,n); in.close(); out.close();

            if(personal){
                String name=prefs.getString("pending_name","איש קשר");
                prefs.edit().putString("contact_"+key+"_file",f.getAbsolutePath()).putString("contact_"+key+"_name",name).remove("pending_name").apply();
                Toast.makeText(this,"הצלצול נשמר עבור "+name,Toast.LENGTH_SHORT).show();
            }else if("UNKNOWN".equals(key)){
                prefs.edit().putString("unknown_file",f.getAbsolutePath()).apply();
                Toast.makeText(this,"צלצול למספרים לא מזוהים נשמר",Toast.LENGTH_SHORT).show();
            }else{
                prefs.edit().putString("ring_file",f.getAbsolutePath()).putString("ring_uri",u.toString()).apply();
                Toast.makeText(this,"צלצול ברירת המחדל נשמר",Toast.LENGTH_SHORT).show();
            }
            refreshStatus();
        }catch(Exception e){Toast.makeText(this,"לא הצלחתי לקרוא את הצלצול: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    private void showContactRingtones(){
        StringBuilder s=new StringBuilder();
        for(String k:prefs.getAll().keySet())
            if(k.startsWith("contact_")&&k.endsWith("_name"))
                s.append("• ").append(prefs.getString(k,"איש קשר")).append("\n");
        if(s.length()==0){Toast.makeText(this,"עדיין לא הוגדרו צלצולים לאנשי קשר",Toast.LENGTH_SHORT).show();return;}
        new AlertDialog.Builder(this).setTitle("צלצולים לאנשי קשר").setMessage(s.toString()).setPositiveButton("סגור",null).show();
    }

    private void showDiagnostics(){
        StringBuilder s=new StringBuilder();
        boolean phone=android.os.Build.VERSION.SDK_INT<23||checkSelfPermission(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED;
        boolean contacts=android.os.Build.VERSION.SDK_INT<23||checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED;
        boolean ring=prefs.getString("ring_file",null)!=null&&new File(prefs.getString("ring_file","")).isFile();
        boolean armed=prefs.getBoolean("armed",false);
        s.append(phone?"✓ זיהוי שיחות":"✗ הרשאת זיהוי שיחות").append("\n");
        s.append(contacts?"✓ אנשי קשר":"✗ הרשאת אנשי קשר").append("\n");
        s.append(ring?"✓ צלצול ברירת מחדל":"✗ לא נבחר צלצול").append("\n");
        s.append(armed?"✓ Myring פעיל":"○ Myring כבוי").append("\n");
        s.append("✓ ללא Accessibility").append("\n");
        s.append("✓ שחזור לאחר אתחול המכשיר");
        new AlertDialog.Builder(this).setTitle("אבחון Myring 2.0").setMessage(s.toString()).setPositiveButton("סגור",null).show();
    }

    private void armFix(){
        if(android.os.Build.VERSION.SDK_INT>=23&&!Settings.System.canWrite(this)){
            try{startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+getPackageName())));}
            catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
            Toast.makeText(this,"אשר לאפליקציה שינוי הגדרות מערכת, ואז לחץ שוב",Toast.LENGTH_LONG).show(); return;
        }
        if(android.os.Build.VERSION.SDK_INT<=28&&checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQ_STORAGE); return;
        }
        try{
            Uri silent=SilentRingtone.ensure(this);
            Uri old=RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE);
            if(old!=null&&!silent.equals(old)&&!prefs.contains("original_uri"))prefs.edit().putString("original_uri",old.toString()).apply();
            RingtoneManager.setActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE,silent);
            prefs.edit().putBoolean("armed",true).apply();
            RingService.ensurePersistent(this); refreshStatus();
            Toast.makeText(this,"התיקון הופעל",Toast.LENGTH_SHORT).show();
        }catch(Exception e){Toast.makeText(this,"הפעלת התיקון נכשלה: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    private void restoreOriginal(){
        String s=prefs.getString("original_uri",null);
        if(s==null){Toast.makeText(this,"לא נשמר צלצול מקורי",Toast.LENGTH_SHORT).show();return;}
        try{
            RingtoneManager.setActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE,Uri.parse(s));
            prefs.edit().putBoolean("armed",false).apply();
            RingService.stopPersistent(this); RingService.stopIncomingDirect();
            refreshStatus();
            Toast.makeText(this,"הצלצול המקורי שוחזר",Toast.LENGTH_SHORT).show();
        }catch(Exception e){Toast.makeText(this,"שחזור נכשל: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    private void refreshStatus(){
        if(modeLabel!=null) modeLabel.setText(prefs.getBoolean("advanced_mode",false)?"מצב מתקדם":"מצב פשוט — בדיוק כמו הגרסה הקודמת");
        if(status!=null){
            boolean armed=prefs.getBoolean("armed",false);
            status.setText((armed?"✓ Myring פעיל":"○ Myring כבוי")+"\n"+(prefs.getString("ring_file",null)==null?"לא נבחר צלצול ברירת מחדל":"צלצול ברירת מחדל נבחר"));
        }
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_STORAGE&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)armFix();
        if(r==REQ_PHONE&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED&&android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},REQ_CONTACTS);
        if(r==REQ_CONTACTS&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED&&android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIFICATIONS);
    }
}
