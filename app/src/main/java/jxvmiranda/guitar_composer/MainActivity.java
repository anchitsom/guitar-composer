package jxvmiranda.guitar_composer;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    int resource_up = R.raw.gu;
    int resource_down = R.raw.gd;

    boolean record;
    AudioTrack previousTrack = null;
    @RequiresApi(api = Build.VERSION_CODES.M)
    String[] chords = {"G", "F"};
    private void setResource(String chord){
        if (chord.equals("G")){
            resource_up = R.raw.gu;
            resource_down = R.raw.gd;
        }
        else if (chord.equals("F")){
            resource_up = R.raw.test1;
            resource_down = R.raw.test2;
        }
    }
    private void time(){
    }
    private void record(){
    }
    long startTime;
    boolean timerOn = false;

    private void wait(int time){
        long futureTime = System.currentTimeMillis() + time;
        while (System.currentTimeMillis() < futureTime) {
            synchronized (this) {
                try {
                    wait(futureTime - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void play(){
        String chordToSet = chords[0];
        setResource(chordToSet);
        wait(10000);
        chordToSet = chords[1];
        setResource(chordToSet);
    }

    public class PlayOnClickListener implements View.OnClickListener{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Runnable r = new Runnable(){

                @Override
                public void run() {
                    play();
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
    }
    public class UpOnClickListener implements View.OnClickListener{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Runnable r = new Runnable(){
                @Override
                public void run() {
                    if (previousTrack != null && previousTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        Log.d("TAG", "Pausing");
                        previousTrack.pause();
                        previousTrack.flush();
                        previousTrack.release();
                    }
                    try {
                        long totalAudioLen = 0;
                        InputStream inputStream = getResources().openRawResource(resource_up); // open the file
                        totalAudioLen = inputStream.available();
                        byte[] rawBytes = new byte[(int) totalAudioLen];
                        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                                44100,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                (int) totalAudioLen,
                                AudioTrack.MODE_STATIC);
                        int offset = 0;
                        int numRead = 0;
                        previousTrack = track;
                        track.setPlaybackHeadPosition(100); // IMPORTANT to skip the click
                        while (offset < rawBytes.length
                                && (numRead = inputStream.read(rawBytes, offset, rawBytes.length - offset)) >= 0) {
                            offset += numRead;
                        } //don't really know why it works, it reads the file
                        track.write(rawBytes, 0, (int) totalAudioLen); //write it in the buffer?
                        track.play();
                        track.setPlaybackRate(44100);
                        inputStream.close();
                    } catch (FileNotFoundException e) {
                        Log.e("SD", "Error loading audio to bytes", e);
                    } catch (IOException e) {
                        Log.e("sf", "Error loading audio to bytes", e);
                    } catch (IllegalArgumentException e) {
                        Log.e("DS", "Error loading audio to bytes", e);
                    }

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
                    if (previousTrack != null && previousTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        Log.d("TAG", "Pausing");
                        previousTrack.pause();
                        previousTrack.flush();
                        previousTrack.release();
                    }
                    try {
                        long totalAudioLen = 0;
                        InputStream inputStream = getResources().openRawResource(resource_down); // open the file
                        totalAudioLen = inputStream.available();
                        byte[] rawBytes = new byte[(int) totalAudioLen];
                        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                                44100,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                (int) totalAudioLen,
                                AudioTrack.MODE_STATIC);
                        int offset = 0;
                        int numRead = 0;
                        previousTrack = track;
                        track.setPlaybackHeadPosition(100); // IMPORTANT to skip the click
                        while (offset < rawBytes.length
                                && (numRead = inputStream.read(rawBytes, offset, rawBytes.length - offset)) >= 0) {
                            offset += numRead;
                        } //don't really know why it works, it reads the file
                        track.write(rawBytes, 0, (int) totalAudioLen); //write it in the buffer?
                        track.play();  // launch the play
                        track.setPlaybackRate(44100);
                        inputStream.close();
                    } catch (FileNotFoundException e) {
                        Log.e("SD", "Error loading audio to bytes", e);
                    } catch (IOException e) {
                        Log.e("sf", "Error loading audio to bytes", e);
                    } catch (IllegalArgumentException e) {
                        Log.e("DS", "Error loading audio to bytes", e);
                    }
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
        Button play = (Button) findViewById(R.id.play);

        play.setOnClickListener(new PlayOnClickListener());
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

//            Runnable r = new Runnable(){
//                @Override
//                public void run() {
//                    resource = R.raw.test2;
////                    if (track != null) {
////                        track.reset();
////                    }
//                    if(!timerOn) {
//                        startTime = System.currentTimeMillis();
//                        timerOn = true;
//                    }
//                    else{
//                        Log.d("TAG", "difference in time " + Long.toString(System.currentTimeMillis() - startTime));
//                        timerOn = false;
//                    }
//                    play();
//                }
//            };
//            Thread t = new Thread(r);
//            t.start();