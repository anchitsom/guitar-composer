package jxvmiranda.guitar_composer;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class MainActivity2 extends AppCompatActivity {

    private ArrayList<Integer> resources = new ArrayList<>();
    private ArrayList<Integer> times = new ArrayList<>();
    private ArrayList<InputStream> streams = new ArrayList<>();

    private FileInputStream fs;

    private String getData(File myfile) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(myfile);
            int i = -1;
            StringBuffer buffer = new StringBuffer();
            while ((i = fileInputStream.read()) != -1) {
                buffer.append((char) i);
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void createStreams(){
        for (int resource : resources){
            streams.add(getResources().openRawResource(resource));
        }
    }

    private void play(){
        for (int i = 0; i < streams.size(); i++) {
            final InputStream stream = streams.get(i);
            int time = times.get(i);
            Log.d("TAG", "Start Thread " + i);
            Runnable r = new Runnable(){
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                    AudioTrack track = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(44100)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                    .build())
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .setBufferSizeInBytes(minBufferSize)
                            .build();
                    byte[] music = null;
                    int i = 0;
                    try{
                        music = new byte[512];
                        track.play();
                        while((i = stream.read(music)) != -1) {
                            track.write(music, 0, i);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    track.release();
                }
            };
            Thread t = new Thread(r);
            t.start();
            wait(time);
        }
    }

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

    class PlayOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            String fileName = "test.txt";
            String folder_name = "music-maker";
            File file = new File(Environment.getExternalStorageDirectory() + "/" + folder_name, fileName);
            String text = getData(file);

            String[] parts = text.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (i % 2 == 0) {
                    resources.add(Integer.parseInt(parts[i]));
                }
                else{
                    times.add(Integer.parseInt(parts[i]));
                }
            }
            Log.d("tag", "File reading done");
            createStreams();
            play();
        }
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button play = (Button) findViewById(R.id.play);
        play.setOnClickListener(new PlayOnClickListener());
    }
}


  /*  public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {


        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(lines.get(position));
            } catch (FileNotFoundException e) {
            e.printStackTrace();
          }


    }
*/
//
//
//    File dir = new File(filePath);
//
//    File[] list = dir.listFiles();
//
//    String[] theNamesOfFiles = new String[list.length];
//            for (int i = 0; i < theNamesOfFiles.length; i++) {
//        theNamesOfFiles[i] = list[i].getName();
//        }
//
//
//        ArrayAdapter<String> ab=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, theNamesOfFiles);
//




