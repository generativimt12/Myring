package com.generativimt12.myring;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import java.io.File;

public class RingService extends Service {
    public static final String START="START",STOP="STOP",TEST="TEST",TEST_STOP="TEST_STOP",MUTE="MUTE";
    private static final int INCOMING_NOTIFICATION_ID=8;
    private static final String INCOMING_CHANNEL="incoming_call_v3";
    private static MediaPlayer directPlayer;
    private static volatile boolean incomingRinging=false;
    private MediaPlayer player;

    public static void playIncomingDirect(Context c){ playIncomingDirect(c,null); }

    public static void playIncomingDirect(Context c,String preferredPath){
        stopIncomingDirect();
        incomingRinging=true;
        MediaPlayer p=createPlayer(c, preferredPath);
        if(p==null)return;
        directPlayer=p;
        showIncomingNotification(c);
        try{ p.start(); startFadeIn(p,c); }catch(Exception e){ try{p.release();}catch(Exception ignored){} directPlayer=null; }
    }

    public static void stopIncomingDirect(){
        incomingRinging=false;
        MediaPlayer p=directPlayer;
        directPlayer=null;
        if(p!=null) stopPlayer(p);
        cancelIncomingNotification();
    }

    public static boolean isIncomingRinging(){ return incomingRinging; }

    public static void muteIncoming(){
        incomingRinging=false;
        MediaPlayer p=directPlayer;
        directPlayer=null;
        if(p!=null)stopPlayer(p);
        cancelIncomingNotification();
    }

    public static void startRinging(Context c){start(c,START);}
    public static void stopRinging(Context c){start(c,STOP);}
    public static void startTest(Context c){start(c,TEST);}
    public static void stopTest(Context c){start(c,TEST_STOP);}
    public static void ensurePersistent(Context c){start(c,null);}
    public static void stopPersistent(Context c){try{c.stopService(new Intent(c,RingService.class));}catch(Exception ignored){}}

    private static void start(Context c,String action){
        Intent i=new Intent(c,RingService.class);
        i.setAction(action);
        try{if(Build.VERSION.SDK_INT>=26)c.startForegroundService(i);else c.startService(i);}catch(Exception ignored){}
    }

    @Override public void onCreate(){super.onCreate();lastContext=getApplicationContext();createChannel();try{startForeground(7,notification());}catch(Exception ignored){}}

    @Override public int onStartCommand(Intent i,int f,int id){
        String a=i==null?null:i.getAction();
        if(START.equals(a)||TEST.equals(a))playRingtone();
        else if(STOP.equals(a)||TEST_STOP.equals(a))stopRingtone();
        else if(MUTE.equals(a))muteIncoming();
        return START_STICKY;
    }

    private void playRingtone(){
        stopRingtone();
        String p=getSharedPreferences("myring",MODE_PRIVATE).getString("ring_file",null);
        player=createPlayer(this,p);
        if(player==null)return;
        try{player.start();startFadeIn(player,this);}catch(Exception e){stopRingtone();}
    }

    private static MediaPlayer createPlayer(Context c,String path){
        try{
            if(path==null)path=c.getSharedPreferences("myring",MODE_PRIVATE).getString("ring_file",null);
            if(path==null)return null;
            File f=new File(path);
            if(!f.isFile()||f.length()==0)return null;

            MediaPlayer p=new MediaPlayer();
            p.setDataSource(f.getAbsolutePath());
            AudioAttributes attrs=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            if(Build.VERSION.SDK_INT>=21)p.setAudioAttributes(attrs);
            else p.setAudioStreamType(AudioManager.STREAM_RING);
            p.setLooping(true);
            p.setOnCompletionListener(mp->{});
            p.prepare();

            android.content.SharedPreferences sp=c.getSharedPreferences("myring",MODE_PRIVATE);
            float volume=Math.max(0f,Math.min(1f,sp.getInt("audio_volume",100)/100f));
            p.setVolume(0f,0f);
            if(Build.VERSION.SDK_INT>=23){
                float pitch=Math.max(0.5f,Math.min(2.0f,sp.getInt("audio_pitch",100)/100f));
                try{
                    PlaybackParams pp=p.getPlaybackParams();
                    pp.setSpeed(1.0f);
                    pp.setPitch(pitch);
                    p.setPlaybackParams(pp);
                }catch(Exception ignored){}
            }
            p.setVolume(volume,volume);
            routePlayer(c,p,sp.getInt("audio_route",0));
            return p;
        }catch(Exception e){return null;}
    }

    private static void routePlayer(Context c,MediaPlayer p,int route){
        if(Build.VERSION.SDK_INT<28||route==0)return;
        try{
            AudioManager am=(AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
            if(am==null)return;
            AudioDeviceInfo[] devices=am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            AudioDeviceInfo target=null;
            for(AudioDeviceInfo d:devices){
                int t=d.getType();
                if(route==1&&t==AudioDeviceInfo.TYPE_BUILTIN_SPEAKER){target=d;break;}
                if(route==2&&(t==AudioDeviceInfo.TYPE_BLUETOOTH_A2DP||t==AudioDeviceInfo.TYPE_BLUETOOTH_SCO||t==AudioDeviceInfo.TYPE_BLE_HEADSET)){target=d;break;}
            }
            if(target!=null)p.setPreferredDevice(target);
        }catch(Exception ignored){}
    }

    private static void startFadeIn(MediaPlayer p,Context c){
        int ms=c.getSharedPreferences("myring",MODE_PRIVATE).getInt("fade_in_ms",0);
        if(ms<=0){
            float v=c.getSharedPreferences("myring",MODE_PRIVATE).getInt("audio_volume",100)/100f;
            try{p.setVolume(v,v);}catch(Exception ignored){}
            return;
        }
        final float target=c.getSharedPreferences("myring",MODE_PRIVATE).getInt("audio_volume",100)/100f;
        final long start=SystemClock.uptimeMillis();
        final Handler h=new Handler(Looper.getMainLooper());
        Runnable r=new Runnable(){public void run(){
            float x=Math.min(1f,(SystemClock.uptimeMillis()-start)/(float)ms);
            float v=target*x;
            try{p.setVolume(v,v);}catch(Exception ignored){}
            if(x<1f)h.postDelayed(this,30);
        }};
        h.post(r);
    }

    
    private static void stopPlayer(MediaPlayer p){
        try{
            int ms=0;
            // Immediate stop is the safe default; the user can enable a fade-in/out profile later.
            if(p.isPlaying())p.stop();
        }catch(Exception ignored){}
        try{p.release();}catch(Exception ignored){}
    }

    private void stopRingtone(){MediaPlayer p=player;player=null;if(p!=null)stopPlayer(p);}

    private void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel("ring","Myring",NotificationManager.IMPORTANCE_LOW);
            ch.setSound(null,null);
            NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if(n!=null){
                n.createNotificationChannel(ch);
                NotificationChannel incoming=new NotificationChannel(INCOMING_CHANNEL,"Myring — שיחה נכנסת",NotificationManager.IMPORTANCE_HIGH);
                incoming.setDescription("באנר מהיר להשתקת צלצול Myring בזמן שיחה נכנסת");
                incoming.setSound(null,null);
                incoming.enableVibration(false);
                n.createNotificationChannel(incoming);
            }
        }
    }

    private static void showIncomingNotification(Context c){
        if(Build.VERSION.SDK_INT>=33 && c.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)return;
        NotificationManager n=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(n==null)return;
        Intent mute=new Intent(c,RingService.class).setAction(MUTE);
        PendingIntent pi=PendingIntent.getService(c,19,mute,Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE:PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,INCOMING_CHANNEL):new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setContentTitle("Myring — שיחה נכנסת")
                .setContentText("הצלצול פעיל · השתקה זמינה כאן")
                .setCategory(Notification.CATEGORY_CALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(new Notification.Action.Builder(null,"השתק",pi).build());
        n.notify(INCOMING_NOTIFICATION_ID,b.build());
    }

    private static void cancelIncomingNotification(){
        // NotificationManager is obtained from the service instance when possible.
        // The direct player can be stopped from a receiver without a service instance,
        // so use the application context saved by the running service when available.
        if(lastContext!=null){
            NotificationManager n=(NotificationManager)lastContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if(n!=null)n.cancel(INCOMING_NOTIFICATION_ID);
        }
    }

    private static Context lastContext;

    private Notification notification(){
        Intent mute=new Intent(this,RingService.class).setAction(MUTE);
        PendingIntent pi=PendingIntent.getService(this,19,mute,Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE:PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"ring"):new Notification.Builder(this);
        return b.setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setContentTitle("Myring")
                .setContentText("מטפל בצלצול השיחה הנכנסת")
                .setOngoing(true)
                .addAction(new Notification.Action.Builder(null,"השתק צלצול",pi).build())
                .build();
    }

    @Override public void onDestroy(){stopRingtone();cancelIncomingNotification();lastContext=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}