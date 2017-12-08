package jxvmiranda.guitar_composer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.view.animation.Animation.INFINITE;
import static android.view.animation.Animation.RESTART;

public class MainActivity extends AppCompatActivity {
    // InputStream stream;
    private int resources;
    private MediaPlayer player;
    private AssetFileDescriptor afd_up;
    private AssetFileDescriptor afd_down;
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<String> exchord = new ArrayList<>();
    private ArrayList<String> copy;
    private ListView chordlist;
    private static final String TAG = "MainActivity";
    private int size;
    private boolean met_is_playing = false;
    private TextView chordtext;
    private ArrayAdapter<String> aa;
    private int count = 0;

    private GridView addchord;
    private ArrayList<String> tempChords = new ArrayList<>();
    private CheckBox metronome;
    private ImageView m1;
    private ImageView m2;
    private ImageView m3;
    private ImageView m4;
    private Drawable image;
    private Drawable outline;

    private int[] imageId = {
            R.drawable.c, R.drawable.dm,
            R.drawable.em, R.drawable.f,R.drawable.g,R.drawable.am

    };
    private long startTime;
    private long time;
    private Button up;
    private Button down;
    private Button play;
    private ToggleButton record;
    private Button reset;
    private Button clear;
    private boolean fileCreated = false;

    private FileOutputStream fs;
    private TextView bpmtext;
    private String Chords[] = {
            "Cmajor",
            "Dminor",
            "Eminor",
            "Fmajor",
            "Gmajor",
            "Aminor"
    };
    private String keys[] = {"C", "G"};

    private void setResource(String chord){
        if (chord.equals("Cmajor")){
            chordIndex = 0;
        }
        else if (chord.equals("Dminor")){
            chordIndex = 1;
        }
        else if (chord.equals("Eminor")){
            chordIndex = 2;
        }
        else if (chord.equals("Fmajor")){
            chordIndex = 3;
        }
        else if (chord.equals("Gmajor")){
            chordIndex = 4;
        }
        else if (chord.equals("Aminor")){
            chordIndex = 5;
        }
        Log.d("DS", chord);
    }
    private Handler handler = new Handler();
    private Runnable run_mt= new Runnable() {
        private int whichBeat = 1;

        private boolean temp_flag = false;
        String temp;
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {

            if (metronome.isChecked()){
                metro_track.seekTo(0);
                metro_track.start();
            }
            if (count == size ){
                Log.d("TAG", Integer.toString(count));
                met_is_playing=false;
            }
            else {
                if (whichBeat == 1) {
                    m4.setBackground(outline);
                    m1.setBackground(image);
                    temp = exchord.get(0);
                    setResource(temp);
                    chordtext.setText(exchord.get(0));
                    exchord.remove(0);
                    aa.notifyDataSetChanged();
                }
                else if (whichBeat == 2) {
                    m1.setBackground(outline);
                    m2.setBackground(image);
                }
                else if (whichBeat == 3) {
                    m2.setBackground(outline);
                    m3.setBackground(image);
                }
                else if (whichBeat == 4) {
                    if (temp_flag)
                        met_is_playing = false;
                    m3.setBackground(outline);
                    m4.setBackground(image);
                }
            }
            if (met_is_playing){
                whichBeat++;
                if (whichBeat == 5){
                    whichBeat = 1;
                    count++;
                }
                handler.postDelayed(this, (time/4));
            }
            else{
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                m1.setBackground(outline);
                m2.setBackground(outline);
                m3.setBackground(outline);
                m4.setBackground(outline);
                chordtext.setText("");
            }
        }
    };

    private MediaPlayer metro_track;

    SoundPool pool = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
    int[][] soundID = new int[7][2];
    int[][] streamID = new int[7][2];
    int chordIndex = 6;

    boolean isRecord = false;
    public class UpOnClickListener implements View.OnClickListener{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    pool.stop(streamID[chordIndex][0]);
                    pool.stop(streamID[chordIndex][1]);
                    streamID[chordIndex][0] = pool.play(soundID[chordIndex][0], 1.0f, 1.0f, 1, 0, 1);
                    try {
                        long wait_time = System.currentTimeMillis() - startTime;
                        Log.d("TAG", "difference in time " + Long.toString(wait_time));
                        startTime = System.currentTimeMillis();
                        if(fileCreated) {
                            try {
                                fs.write(Long.toString(wait_time).getBytes());
                                fs.write(" ".getBytes());
                                fs.write(Integer.toString(soundID[chordIndex][0]).getBytes());
                                fs.write(" ".getBytes());
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e("DS", "Error loading audio to bytes", e);
                    }
                }
            }).start();
        }
    }

    public class DownOnClickListener implements View.OnClickListener{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Log.d("TAG", "On Click Starts");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    pool.stop(streamID[0][0]);
                    pool.stop(streamID[0][1]);
                    streamID[chordIndex][1] = pool.play(soundID[chordIndex][1], 1.0f, 1.0f, 1, 0, 1);
                    try {
                        long wait_time = System.currentTimeMillis() - startTime;
                        Log.d("TAG", "difference in time " + Long.toString(wait_time));
                        startTime = System.currentTimeMillis();
                        if(fileCreated) {
                            try {
                                fs.write(Long.toString(wait_time).getBytes());
                                fs.write(" ".getBytes());
                                fs.write(Integer.toString(soundID[chordIndex][1]).getBytes());
                                fs.write(" ".getBytes());
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e("DS", "Error loading audio to bytes", e);
                    }
                }

            }).start();
        };
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        play = findViewById(R.id.play);
        record = findViewById(R.id.record);
        reset = findViewById(R.id.reset);
        addchord = findViewById(R.id.addchord);
        clear = findViewById(R.id.clear);

        ArrayList<Long> times = new ArrayList<>();
        ArrayList<Integer> soundIDs = new ArrayList<>();
        Log.d(TAG, "Resource ID is " +  Integer.toString(R.raw.test1));
        soundID[0][0] = pool.load(this, R.raw.cup, 1);
        soundID[0][1] = pool.load(this, R.raw.cdown, 1);

        soundID[1][0] = pool.load(this, R.raw.dminorup, 1);
        soundID[1][1] = pool.load(this, R.raw.dminordown, 1);

        soundID[2][0] = pool.load(this, R.raw.eminorup, 1);
        soundID[2][1] = pool.load(this, R.raw.eminordown, 1);

        soundID[3][0] = pool.load(this, R.raw.fup, 1);
        soundID[3][1] = pool.load(this, R.raw.fdown, 1);

        soundID[4][0] = pool.load(this, R.raw.gup, 1);
        soundID[4][1] = pool.load(this, R.raw.gdown, 1);

        soundID[5][0] = pool.load(this, R.raw.aminorup, 1);
        soundID[5][1] = pool.load(this, R.raw.aminordown, 1);

        soundID[6][0] = pool.load(this, R.raw.gu, 1);
        soundID[6][1] = pool.load(this, R.raw.gd, 1);

        chordtext = findViewById(R.id.chordtext);
        chordtext.setGravity(Gravity.CENTER);
        chordtext.setTextSize(20);

        m1 = (ImageView) findViewById(R.id.m1);
        m2 = (ImageView) findViewById(R.id.m2);
        m3 = (ImageView) findViewById(R.id.m3);
        m4 = (ImageView) findViewById(R.id.m4);
        image = (Drawable) getResources().getDrawable(R.drawable.custom);
        outline = (Drawable) getResources().getDrawable(R.drawable.outline);

        metro_track = MediaPlayer.create(this, R.raw.metronome);
        bpmtext = (TextView) findViewById(R.id.bpmtext);
        bpmtext.setText("BPM");
        SeekBar bpm = (SeekBar) findViewById(R.id.bpm);
        bpm.setMax(300);
        bpm.setProgress(120);
        time = 240000 / 120;
        bpmtext.setText(120 + "BPM");
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
        metronome = findViewById(R.id.metronome);
        chordlist = findViewById(R.id.chordlist);
        aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, exchord);
        chordlist.setAdapter(aa);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        up.setOnClickListener(new UpOnClickListener());
        down.setOnClickListener(new DownOnClickListener());



        ImageAdapter listofchords = new ImageAdapter(MainActivity.this,Chords, imageId);
        addchord.setAdapter(listofchords);
        //ArrayAdapter<String> listofchords=new ArrayAdapter<String>(this, android.R.layout.simple_gallery_item, Chords);

        addchord.setAdapter(listofchords);

        addchord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int i, long id) {
                if (i == 0) {
                    exchord.add((String)Chords[i]);
                    aa.notifyDataSetChanged();
                }
                if (i == 1) {
                    exchord.add((String) Chords[i]);
                    aa.notifyDataSetChanged();
                }

                if (i == 2) {
                    exchord.add((String) Chords[i]);
                    aa.notifyDataSetChanged();
                }

                if (i == 3) {
                    exchord.add((String)Chords[i]);
                    aa.notifyDataSetChanged();
                }

                if (i == 4) {
                    exchord.add((String) Chords[i]);
                    aa.notifyDataSetChanged();
                }

                if (i == 5) {
                    exchord.add((String) Chords[i]);
                    aa.notifyDataSetChanged();
                }

            }
        });

        // get your ToggleButton
        record = (ToggleButton) findViewById(R.id.record);
        record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    isRecord = true;
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
                    /* create the dialog box */
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Enter the name of the File : ");
                    // set up the input
                    final EditText input = new EditText(MainActivity.this);
                    // specify the type of thes input
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    // set up the button
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String fileName = input.getText().toString();
                            String folder_name = "music-maker";
                            File file = new File(Environment.getExternalStorageDirectory() + "/" + folder_name, fileName);
                            fs = null;
                            try {
                                fs = new FileOutputStream(file);
                                fileCreated = true;
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                    /* end dialog box */
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempChords.addAll(exchord);
                Log.d("TAG", tempChords.get(0));
                size = exchord.size();
                met_is_playing = true;
                startTime = System.currentTimeMillis();
                // start the music
                handler.removeCallbacks(run_mt);
                handler.postDelayed(run_mt, 2000);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exchord.clear();
                exchord.addAll(tempChords);
                Log.d("TAG", exchord.get(0));
                tempChords.clear();
                m1.setBackground(outline);
                m2.setBackground(outline);
                m3.setBackground(outline);
                m4.setBackground(outline);
                chordtext.setText("");
                count = 0;
                aa.notifyDataSetChanged();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navmenu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.recordings:
                Intent recintent = new Intent(getApplicationContext(), MainActivity2.class);
                startActivityForResult(recintent, 0);
            case R.id.home:
                Intent homeintent = new Intent(getApplicationContext(), MainActivity2.class);
                startActivityForResult(homeintent, 0);
        }
        return true;

    }
}

//                copy = new ArrayList<String>(exchord);
//
//                count = exchord.size();
//
//                Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1000, 0, 0);
//                animation.setDuration((long) time);
//                animation.setFillAfter(true);
//                animation.setRepeatCount(count - 1);
//
//                animation.setAnimationListener(new Animation.AnimationListener() {

//                    public void onAnimationStart(Animation animation) {
//                        chordtext.setText(exchord.get(0));
//                        exchord.remove(0);
//                        aa.notifyDataSetChanged();
//
//                    }
//
//                    public void onAnimationRepeat(Animation animation) {
//
//                        chordtext.setText(exchord.get(0));
//                        exchord.remove(0);
//                        aa.notifyDataSetChanged();
//
//                    }
//
//
//                    public void onAnimationEnd(Animation animation) {
//                    }
//                });
//
//                ticker.startAnimation(animation);
//
//
//            }
//        });
//
//
//        reset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ticker.clearAnimation();
//
//                exchord = new ArrayList<String>(copy);
//                aa.notifyDataSetChanged();
//            }
//        });
//
//        clear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                exchord.clear();
//                copy.clear();
//                aa.notifyDataSetChanged();
//            }
//        });