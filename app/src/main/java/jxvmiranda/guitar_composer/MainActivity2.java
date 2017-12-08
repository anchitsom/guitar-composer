package jxvmiranda.guitar_composer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by abhinavgoyal on 19/11/2017.
 */

public class MainActivity2 extends AppCompatActivity {
    // an array for storing the selected files
    ArrayList <File> selected_files = new ArrayList<File>();
    // a string for storing the user inputted file name
    private String m_Text = "";
    // an arraylist for storing all the files in the memory location
    ArrayList<File> files = new ArrayList<>();
    // an arraylist for getting file names of all the files in the memory location
    ArrayList<String> file_names = new ArrayList<>();
    // an arraylist for storing time
    ArrayList <Integer> times = new ArrayList<>();
    // an arraylist for sound id
    ArrayList <Integer> soundIds = new ArrayList<>();

    SoundPool pool;
    ListView record;
    CustomAdapter customAdapter;
    FileInputStream fs = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recordings);
        // stitch button
        final Button stitchButton = (Button) findViewById(R.id.Stitch);
        // delete button
        final Button deleteButton = (Button) findViewById(R.id.Delete);
        // play button
        final Button playButton = (Button) findViewById(R.id.play);

        pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        int soundID[] = new int[14];

        soundID[0] = pool.load(MainActivity2.this, R.raw.cup, 1);
        soundID[1] = pool.load(MainActivity2.this, R.raw.cdown, 1);

        soundID[2] = pool.load(MainActivity2.this, R.raw.dminorup, 1);
        soundID[3] = pool.load(MainActivity2.this, R.raw.dminordown, 1);

        soundID[4] = pool.load(MainActivity2.this, R.raw.eminorup, 1);
        soundID[5] = pool.load(MainActivity2.this, R.raw.eminordown, 1);

        soundID[6] = pool.load(MainActivity2.this, R.raw.fup, 1);
        soundID[7] = pool.load(MainActivity2.this, R.raw.fdown, 1);

        soundID[8] = pool.load(MainActivity2.this, R.raw.gup, 1);
        soundID[9] = pool.load(MainActivity2.this, R.raw.gdown, 1);

        soundID[10] = pool.load(MainActivity2.this, R.raw.aminorup, 1);
        soundID[11] = pool.load(MainActivity2.this, R.raw.aminordown, 1);

        soundID[12] = pool.load(MainActivity2.this, R.raw.chunkdown, 1);
        soundID[13] = pool.load(MainActivity2.this, R.raw.chunkdownup, 1);
        try {
//            String filePath = "/storage/emulated/0/music-maker";
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.v("j","Permission is granted");
                } else {
                    Log.v("hu","Permission is revoked");
                    ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
            else {
                // permission is automatically granted on sdk<23 upon installation
                Log.v("tg","Permission is granted");
            }
            // ListView chordlist;
            String file_path = Environment.getExternalStorageDirectory().toString() + "/music-maker";
            File f = new File(file_path);
            files.addAll(Arrays.asList(f.listFiles()));

            for (int i = 0; i < files.size(); i++) {
                file_names.add(files.get(i).getName());
            }
            ListView listView = (ListView) findViewById(R.id.simpleListView);
            customAdapter = new CustomAdapter(getApplicationContext(), file_names.toArray(new String[file_names.size()]));
            listView.setAdapter(customAdapter);
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

        // on click listener for STITCH
        stitchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                /* create the dialog box */
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                builder.setTitle("Enter the name of the File : ");
                // set up the input
                final EditText input = new EditText(MainActivity2.this);
                // specify the type of the input
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                // set up the button
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        // rename merged file
                        File mergedFile = new File(Environment.getExternalStorageDirectory().toString() + "/music-maker/" + m_Text + ".txt");
                        // looping through `checked=true` file_names to add them to selected_files arraylist
                        for (int i = 0; i < customAdapter.file_list.size(); ++i) {
                            String file_name = customAdapter.file_list.get(i);
                            // Log.d("wew", "onClick: file_size is " + customAdapter.file_list.size());
                            for (int j = 0; j < files.size(); ++j) {
                                // Log.d("wew", "onClick: file is appended is : " + files.get(j).getName());
                                if (file_name.equals(files.get(j).getName())) {
                                    selected_files.add(files.get(j));
                                }
                            }
                        }
                        mergeFiles(selected_files, mergedFile);
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
        });

        // on click listener for DELETE
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity2.this)
                        .setTitle("Confirmation")
                        .setMessage("Do you wanna delete all the selected files?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // looping through `checked=true` file_names to add them to selected_files arraylist
                                for (int i = 0; i < customAdapter.file_list.size(); ++i) {
                                    String file_name = customAdapter.file_list.get(i);
                                    // Log.d("wew", "onClick: file_size is " + customAdapter.file_list.size());
                                    for (int j = 0; j < files.size(); ++j) {
                                        // Log.d("wew", "onClick: file is appended is : " + files.get(j).getName());
                                        if (file_name.equals(files.get(j).getName())) {
                                            selected_files.add(files.get(j));
                                        }
                                    }
                                }
                                for (int i = 0; i < selected_files.size(); ++i) {
                                    selected_files.get(i).delete();
                                }

                                /* REFRESHING THE VIEW */
                                String file_path = Environment.getExternalStorageDirectory().toString() + "/music-maker";
                                File f = new File(file_path);
                                files = new ArrayList<>();
                                file_names = new ArrayList<>();
                                files.addAll(Arrays.asList(f.listFiles()));

                                for (int i = 0; i < files.size(); i++) {
                                    file_names.add(files.get(i).getName());
                                }
                                ListView listView = (ListView) findViewById(R.id.simpleListView);

                                customAdapter = new CustomAdapter(getApplicationContext(), file_names.toArray(new String[file_names.size()]));
                                listView.setAdapter(customAdapter);
                                /* END REFRESHING THE VIEW */

                                Toast.makeText(MainActivity2.this, "Files Deleted!", Toast.LENGTH_SHORT).show();

                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        // on click listener for PLAY
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // looping through `checked=true` file_names to add them to selected_files arraylist
                for (int i = 0; i < customAdapter.file_list.size(); ++i) {
                    String file_name = customAdapter.file_list.get(i);
                    // Log.d("wew", "onClick: file_size is " + customAdapter.file_list.size());
                    for (int j = 0; j < files.size(); ++j) {
                        // Log.d("wew", "onClick: file is appended is : " + files.get(j).getName());
                        if (file_name.equals(files.get(j).getName())) {
                            selected_files.add(files.get(j));
                        }
                    }
                }
                String text = getdata(selected_files.get(0));
                String text_array[] = text.split(" ");
                for (int i = 0; i < text_array.length; i++){
                    int temp = Integer.parseInt(text_array[i]);
                    if (i % 2 == 0){
                        times.add(temp);
                    }
                    else{
                        soundIds.add(temp);
                    }
                }
                // Log.d("TAG_DEBUG", text_array.toString());
                // add code for play button here
                int stream = 0;
                for (int i = 0; i < soundIds.size(); i++){
                    long futureTime = System.currentTimeMillis() + times.get(i);
                    while (System.currentTimeMillis() < futureTime) {
                        synchronized (this) {
                            try {
                                wait(futureTime - System.currentTimeMillis());
                            } catch (Exception e) {

                            }
                        }
                    }
                    Log.d("SF", "Check " + soundIds.get(i));
                    pool.stop(stream);
                    stream = pool.play(soundIds.get(i), 1.0f, 1.0f, 1, 0, 1);
                }
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
                Intent homeintent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(homeintent, 0);
        }
        return true;

    }

    public void mergeFiles(ArrayList<File> files_t, File mergedFile) {

        /* add selected files to an array */

        Log.d("wew", "mergeFiles: file size is " + files_t.size());
        FileWriter fstream = null;
        BufferedWriter out = null;

        // start merge
        try {
            fstream = new FileWriter(mergedFile, true);
            out = new BufferedWriter(fstream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        for (File f : files_t) {
            System.out.println("merging: " + f.getName());
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
                BufferedReader in = new BufferedReader(new InputStreamReader(fis));
                String aLine;
                while ((aLine = in.readLine())!=null) {
                    out.write(aLine);
                    out.newLine();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // end merge

        String file_path = Environment.getExternalStorageDirectory().toString() + "/music-maker";
        File f = new File(file_path);
        files = new ArrayList<>();
        file_names = new ArrayList<>();
        files.addAll(Arrays.asList(f.listFiles()));

        for (int i = 0; i < files.size(); i++) {
            file_names.add(files.get(i).getName());
        }
        ListView listView = (ListView) findViewById(R.id.simpleListView);

        customAdapter = new CustomAdapter(getApplicationContext(), file_names.toArray(new String[file_names.size()]));
        listView.setAdapter(customAdapter);
    }

    // play file logic
    // read file logic
    private String getdata(File myfile) {
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
}