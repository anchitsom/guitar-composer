package jxvmiranda.guitar_composer;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    InputStream stream;
    int resource;
    MediaPlayer track;
    boolean record;
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void play(){
        track = MediaPlayer.create(this, resource);
        track.start();
    }
    private void setResource(){

    }
    private void record(){

    }
    long startTime;
    boolean timerOn = false;
    public class UpOnClickListener implements View.OnClickListener{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Runnable r = new Runnable(){
                @Override
                public void run() {
                    resource = R.raw.test1;
                    if (track != null) {
                        track.reset();
                    }
                    if(!timerOn) {
                        startTime = System.currentTimeMillis();
                        timerOn = true;
                    }
                    else{
                        Log.d("TAG", "difference in time " + Long.toString(System.currentTimeMillis() - startTime));
                        timerOn = false;
                    }
                    play();
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
    }
    public class DownOnClickListener implements View.OnClickListener{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Runnable r = new Runnable(){
                @Override
                public void run() {
                    resource = R.raw.test2;
                    if (track != null) {
                        track.reset();
                    }
                    if(!timerOn) {
                        startTime = System.currentTimeMillis();
                        timerOn = true;
                    }
                    else{
                        Log.d("TAG", "difference in time " + Long.toString(System.currentTimeMillis() - startTime));
                        timerOn = false;
                    }
                    play();
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button up = (Button) findViewById(R.id.up);
        Button down = (Button) findViewById(R.id.down);
        up.setOnClickListener(new UpOnClickListener());
        down.setOnClickListener(new DownOnClickListener());
        ToggleButton record_toggle = (ToggleButton) findViewById(R.id.record_toggle);
        record_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    record = true;
                } else {
                    record = false;
                }
            }
        });
    }


}
