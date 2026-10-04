package com.generativimt12.myring;
import android.app.*;import android.content.*;import android.media.*;import android.net.Uri;import android.os.*;import java.io.File;
public class RingService extends Service{
 public static final String START="START",STOP="STOP",TEST="TEST";private MediaPlayer player;
 public static void startRinging(Context c){start(c,new Intent(c,RingService.class).setAction(START));}
 public static void stopRinging(Context c){start(c,new Intent(c,RingService.class).setAction(STOP));}
 public static void startTest(Context c){start(c,new Intent(c,RingService.class).setAction(TEST));}
 private static void start(Context c,Intent i){if(Build.VERSION.SDK_INT>=26)c.startForegroundService(i);else c.startService(i);}
 @Override public void onCreate(){super.onCreate();createChannel();startForeground(7,notification());}
 @Override public int onStartCommand(Intent i,int flags,int id){if(i!=null){String a=i.getAction();if(START.equals(a)||TEST.equals(a))play();else if(STOP.equals(a))stopAndExit();}return START_NOT_STICKY;}
 private void play(){if(player!=null&&player.isPlaying())return;String s=getSharedPreferences("myring",MODE_PRIVATE).getString("ring_file",null);if(s==null||!new File(s).exists()){stopSelf();return;}try{stopPlayer();player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());player.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);player.setDataSource(s);player.setLooping(true);player.setOnErrorListener((mp,w,e)->{stopAndExit();return true;});player.prepare();player.start();}catch(Exception e){stopAndExit();}}
 private void stopPlayer(){if(player!=null){try{player.stop();}catch(Exception ignored){}try{player.reset();}catch(Exception ignored){}try{player.release();}catch(Exception ignored){};player=null;}}
 private void stopAndExit(){stopPlayer();stopSelf();}
 private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel ch=new NotificationChannel("ring","Myring",NotificationManager.IMPORTANCE_LOW);ch.setSound(null,null);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);}}
 private Notification notification(){Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"ring"):new Notification.Builder(this);b.setSmallIcon(android.R.drawable.ic_lock_silent_mode_off).setContentTitle("Myring").setContentText("מטפל בצלצול השיחה הנכנסת").setOngoing(true);return b.build();}
 @Override public void onDestroy(){stopPlayer();super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}