package com.generativimt12.myring;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IncomingMuteActivity extends Activity {
    private final Handler handler = new Handler();
    private final Runnable watcher = new Runnable() {
        @Override public void run() {
            if (!RingService.isIncomingRinging()) { finish(); return; }
            handler.postDelayed(this, 250);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Window w = getWindow();
        w.setGravity(Gravity.TOP);
        w.setDimAmount(0f);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(28, 18, 18, 18);
        box.setBackgroundColor(Color.WHITE);

        TextView text = new TextView(this);
        text.setText("Myring  •  שיחה נכנסת");
        text.setTextSize(17);
        text.setTextColor(Color.DKGRAY);
        box.addView(text, new LinearLayout.LayoutParams(0, -2, 1f));

        Button mute = new Button(this);
        mute.setText("השתק");
        mute.setOnClickListener(v -> {
            RingService.muteIncoming();
            finish();
        });
        box.addView(mute, new LinearLayout.LayoutParams(-2, -2));

        setContentView(box);
        w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        handler.post(watcher);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(watcher);
        super.onDestroy();
    }
}
