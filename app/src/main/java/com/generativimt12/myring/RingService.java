package com.generativimt12.myring;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import java.io.File;

public class RingService extends Service {
    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final String TEST = "TEST";

    private MediaPlayer player;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    public static void startRinging(Context c) {
        start(c, new Intent(c, RingService.class).setAction(START));
    }

    public static void stopRinging(Context c) {
        start(c, new Intent(c, RingService.class).setAction(STOP));
    }

    public static void startTest(Context c) {
        start(c, new Intent(c, RingService.class).setAction(TEST));
    }

    private static void start(Context c, Intent i) {
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i);
        else c.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        createChannel();
        startForeground(7, notification());
    }

    @Override
    public int onStartCommand(Intent i, int flags, int id) {
        if (i != null) {
            String a = i.getAction();
            if (START.equals(a) || TEST.equals(a)) play();
            else if (STOP.equals(a)) stopAndExit();
        }
        return START_NOT_STICKY;
    }

    private void play() {
        stopPlayer();

        String path = getSharedPreferences("myring", MODE_PRIVATE)
                .getString("ring_file", null);

        if (path == null || !new File(path).isFile()) {
            stopSelf();
            return;
        }

        try {
            requestRingAudioFocus();

            player = new MediaPlayer();

            // Crucial change: route playback to the PHONE RING stream, not MEDIA.
            // On many phones the MEDIA stream is muted/suppressed during an incoming call.
            if (Build.VERSION.SDK_INT >= 21) {
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
            } else {
                player.setAudioStreamType(AudioManager.STREAM_RING);
            }

            player.setVolume(1.0f, 1.0f);
            player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            player.setDataSource(path);
            player.setLooping(true);

            player.setOnPreparedListener(mp -> {
                mp.setVolume(1.0f, 1.0f);
                mp.start();
            });

            player.setOnErrorListener((mp, what, extra) -> {
                stopAndExit();
                return true;
            });

            player.prepare();
        } catch (Exception e) {
            stopAndExit();
        }
    }

    private void requestRingAudioFocus() {
        if (audioManager == null) return;

        try {
            if (Build.VERSION.SDK_INT >= 26) {
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(attrs)
                        .setAcceptsDelayedFocusGain(false)
                        .build();

                audioManager.requestAudioFocus(audioFocusRequest);
            } else {
                audioManager.requestAudioFocus(
                        null,
                        AudioManager.STREAM_RING,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        } catch (Exception ignored) {
        }
    }

    private void abandonRingAudioFocus() {
        if (audioManager == null) return;

        try {
            if (Build.VERSION.SDK_INT >= 26 && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                audioFocusRequest = null;
            } else if (Build.VERSION.SDK_INT < 26) {
                audioManager.abandonAudioFocus(null);
            }
        } catch (Exception ignored) {
        }
    }

    private void stopPlayer() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            try { player.reset(); } catch (Exception ignored) {}
            try { player.release(); } catch (Exception ignored) {}
            player = null;
        }
        abandonRingAudioFocus();
    }

    private void stopAndExit() {
        stopPlayer();
        stopSelf();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    "ring", "Myring", NotificationManager.IMPORTANCE_LOW);
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
        stopPlayer();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }
}
