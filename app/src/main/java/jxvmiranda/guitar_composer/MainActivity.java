package jxvmiranda.guitar_composer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    int resource_up = R.raw.gu;
    int resource_down = R.raw.gd;
    long startTime;
    long time;
    boolean timerOn = false;
    boolean record = false;
    AudioTrack previousTrack = null;
    private FileOutputStream fs;
    boolean fileCreated = false;
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
    private void wait_s(long time){
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
        if (record){
            String TAG = "TAG";
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d("T","Permission is granted");
                } else {
                    Log.d("T","Permission is revoked");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
            else { //permission is automatically granted on sdk<23 upon installation
                Log.d("T","Permission is granted");
            }
            String fileName = "test1.txt";
            String folder_name = "music-maker";

            File folder = new File(Environment.getExternalStorageDirectory(), folder_name);
            File file = new File(Environment.getExternalStorageDirectory() + "/" + folder_name, fileName);

            fs = null;
            try {
                fs = new FileOutputStream(file);
                fileCreated = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        startTime = System.currentTimeMillis();
        String chordToSet = chords[0];
        setResource(chordToSet);
        wait_s(time);
        chordToSet = chords[1];
        setResource(chordToSet);
        wait_s(10000);
        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                    long wait_time = System.currentTimeMillis() - startTime;
                    Log.d("TAG", "difference in time " + Long.toString(wait_time));
                    startTime = System.currentTimeMillis();
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
                        if(fileCreated) {
                            try {
                                fs.write(Long.toString(wait_time).getBytes());
                                fs.write(" ".getBytes());
                                fs.write(Integer.toString(resource_up).getBytes());
                                fs.write(" ".getBytes());
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
            Log.d("TAG", "On Click Starts");
            Runnable r = new Runnable(){
                @Override
                public void run() {
                    Log.d("TAG", "Thread START");

                    long wait_time = System.currentTimeMillis() - startTime;
                    Log.d("TAG", "difference in time " + Long.toString(wait_time));
                    startTime = System.currentTimeMillis();

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
                        if(fileCreated) {
                            try {
                                fs.write(Long.toString(wait_time).getBytes());
                                fs.write(" ".getBytes());
                                fs.write(Integer.toString(resource_up).getBytes());
                                fs.write(" ".getBytes());
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
                    Log.d("TAG", "Thread END");


                }

            };
            Thread t = new Thread(r);
            t.start();
            Log.d("TAG", "On Click Ends");

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mt = (Button) findViewById(R.id.metronome);
        final ImageView m1 = (ImageView) findViewById(R.id.m1);
        mt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                Drawable image=(Drawable)getResources().getDrawable(R.drawable.custom);
                m1.setBackground(image);
            }
        });
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

        final TextView bpmtext = (TextView)findViewById(R.id.bpmtext);
        bpmtext.setText("BPM");
        SeekBar bpm =(SeekBar)findViewById(R.id.bpm);
        bpm.setMax(300);
        bpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpmtext.setText(progress + "BPM");
                time = 240000 / progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}

//            Runnable r = new Runnable(){
//                @Override
//                public void run() {
//                    resource = R.raw.test2;
//                    if (track != null) {
//                        track.reset();
//                    }
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