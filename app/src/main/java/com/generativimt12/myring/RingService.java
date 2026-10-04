package com.generativimt12.myring;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import java.io.File;

public class RingService extends Service {
    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final String TEST = "TEST";

    private Ringtone ringtone;
    private AudioManager audioManager;

    public static void startRinging(Context c) {
        start(c, START);
    }

    public static void stopRinging(Context c) {
        start(c, STOP);
    }

    public static void startTest(Context c) {
        start(c, TEST);
    }

    private static void start(Context c, String action) {
        Intent i = new Intent(c, RingService.class);
        i.setAction(action);
        try {
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i);
            else c.startService(i);
        } catch (Exception e) {
            // The caller remains alive; no crash is allowed here.
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        createChannel();
        try {
            startForeground(7, notification());
        } catch (Exception ignored) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();

        if (START.equals(action) || TEST.equals(action)) {
            playRingtone();
        } else if (STOP.equals(action)) {
            stopRingtone();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void playRingtone() {
        stopRingtone();

        String path = getSharedPreferences("myring", MODE_PRIVATE)
                .getString("ring_file", null);

        if (path == null) {
            stopSelf();
            return;
        }

        File file = new File(path);
        if (!file.isFile() || file.length() == 0) {
            stopSelf();
            return;
        }

        try {
            Uri uri = Uri.fromFile(file);
            ringtone = RingtoneManager.getRingtone(this, uri);

            if (ringtone == null) {
                stopSelf();
                return;
            }

            if (Build.VERSION.SDK_INT >= 21) {
                ringtone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
            }

            if (Build.VERSION.SDK_INT >= 21) {
                try { ringtone.setLooping(true); } catch (Exception ignored) {}
            }

            // Explicitly use the ring stream on older devices.
            if (Build.VERSION.SDK_INT < 21) {
                try { ringtone.setStreamType(AudioManager.STREAM_RING); } catch (Exception ignored) {}
            }

            ringtone.play();

        } catch (Exception e) {
            stopRingtone();
            stopSelf();
        }
    }

    private void stopRingtone() {
        if (ringtone != null) {
            try {
                if (ringtone.isPlaying()) ringtone.stop();
            } catch (Exception ignored) {}
            ringtone = null;
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    "ring",
                    "Myring",
                    NotificationManager.IMPORTANCE_LOW);
            ch.setSound(null, null);
            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification notification() {
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "ring")
                : new Notification.Builder(this);

        return b.setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setContentTitle("Myring")
                .setContentText("מטפל בצלצול השיחה הנכנסת")
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        stopRingtone();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
