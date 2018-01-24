1. Interface (Anchit Som):

Layouts - The application is designed using linearlayouts to make the design modular and reproducible in all device sizes. 











The Chord Views

Select a chord menu - The menu to select chords is made using a Gridview. The gridview is attached to an ImageAdapter.java file such that the chord name as well as its musical representation can be shown on the gridview. 



The Adapter has listeners which associate each item in the gridview to an array of chords. Everytime a user clicks the menuitem, the specific item is added to an array which maintains the chord queue. The adapter for the 

addchord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
   @Override
   public void onItemClick(AdapterView<?> parent, View v, 
                           int i, long id) {
       if (i == 0) {
           exchord.add((String) Chords[i]);
           aa.notifyDataSetChanged();
       }
   }
}

Chord Queue menu - The queue menu is made using a listview and is attached to a basic ArrayAdapter aa.


Whenever a new item is selected from the Select menu, the notifyDataSetChanged method is called on the adapter to tell the listview that the array has been updated. 

Control Buttons:

The application consists of play, reset and clear buttons to control the music being played. 




Play: The play listener takes in the size of the chords in the queue array into a global variable and calls the handler for performing the main playing function. 

play.setOnClickListener(new View.OnClickListener() {
   @Override
   public void onClick(View view) {
	
tempChords.addAll(exchord);
       size = exchord.size();
       met_is_playing = true;
       handler.removeCallbacks(run_mt);
       handler.postDelayed(run_mt, 2000);
   }
});

Reset:  In the reset listener we have added a tempChords such that if the user hits reset, the exchord which is empty at the end of the playback can be filled with the initial chords entered by the user. 

exchord.clear();
exchord.addAll(tempChords);
tempChords.clear();
aa.notifyDataSetChanged();

Clear: The clear listener, deletes all the chords entered by user returns the app to the initial state. 

exchord.clear();
tempChords.clear();
aa.notifyDataSetChanged();


BPM Bar : The seekbar is used to set the beats per minute for playing the song. 



It sets the global time variable and also the textview on the side to show what the BPM count actually is. 

public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
   bpmtext.setText(progress + "BPM");
   time = 240000 / progress;
}

Metronome : The metronome is represented by four dots which light up according to the beat being played.
   

Each of the metronome symbols are drawables with id’s m1, m2, m3, m4. The symbols light up according to the logic in runnable

if (whichBeat == 2) {
   m1.setBackground(outline);
   m2.setBackground(image);
}




if (count == size ){
   met_is_playing=false;
}

When the count becomes equal to the size of the chord queue it means that all the chords have been executed and the met_is_playing is set to false to stop execution. 

if (whichBeat == 1) {
    m4.setBackground(outline);
    m1.setBackground(image);
    temp = exchord.get(0);
    setResource(temp);
    change = true;
    chordtext.setText(exchord.get(0));
    exchord.remove(0);
    aa.notifyDataSetChanged();
}


When one time cycle completes, the loop returns to beat 1, and the next chord in the chord queue (exchord) plays. The next chord is saved in a temp variable such that the setResource function can access it and the text on the top changes to the next chord using setText. 



2. SoundPool (Vinit & Aditya):

MC/BC required the use of the Audio Library SoundPool for setting the resources and playing the sounds

Setting the resources: in the MainActivity’s onCreate method, the sounds are being loaded into a double dimensional integer array

int soundID[numberOfChords][2];

The second dimension is 2 as that stores the Up strum and Down strum sound of the Chord. Each soundID is set by calling the SoundPool’s load function. For example,
soundID[0][0] = pool.load(this, R.raw.cup, 1);

The interface has an up button and a down button. Whenever the listener of the up button is activated, the up sound of the chordIndex is played. Similarly, when the down listener is activated, the down sound corresponding to the chordIndex is played. 

streamID = pool.play(soundID[chordIndex][0], 1.0f, 1.0f, 1, 0, 1); // for playing the up chord

The previous sounds that were playing were stopped. This is implemented by storing the streamID that is currently playing and stopping that stream, by calling 

pool.stop(streamID);

Just playing two sounds is not what our end-goal is. We want to change the chordIndex after a specific interval of time. The interval depends on what the beats per minute is set to. This dynamic change of chordIndex and playing of the metronome track is implemented with the use of Handler

3. Threads & Handler (Vinit & Aditya):

To implement the button to be clicked before either the up or down listener completes, the listener code is put inside a runnable and is executed by a thread.

For timed events as mentioned above, we are using a Handler. When Play is clicked on by the user, the handler sends a post message to the Runnable that is responsible for the metronome beat and the dynamic change of chords. The runnable does the following:
If metronome option is checked it plays the sound of the metronome in the background.

At the first beat of the measure, chordIndex is set to whichever chord is in the start of the list. This takes place in the function setResource(String chord)
if (chord.equals("Cmajor"))
   chordIndex = 0;
	}

At the end of the handler, it sends a message to itself after an interval so that it executes with the next beat of the measure. A temporary variable count is used to keep track of the chords left in the chord queue.  Also in each time/4 iteration the whichBeat variable is updated specifying which metronome symbol needs to light up. 
	if (met_is_playing){
   whichBeat++;
   if (whichBeat == 5){
       whichBeat = 1;
       count++;
   }
   handler.postDelayed(this, (time/4));
}

When the measure finishes the chordIndex is again set to the next chord of the list
if (chord.equals("Dminor")){
   chordIndex = 1;
}

The handler keeps calling itself while there is some chord in the chordlist. Once the last chord is removed from the chordlist, it sets a flag variable to false and finished the run() function of the Runnable
The same Runnable’s run method also contains the dynamic change of the metronome component (how different beats correspond to different beats of the metronome being lit up)

4. File Handling (Abhinav):

Recording (or writing to a file): ©

Since android does not support a use of an internal mic, we had to think of something different to add in a recording and playback feature.
We used file writing and file reading to implement this. Each file is a sequence of soundIDs and time intervals arranged one after another. We write the current soundID that is being played and then it’s time interval between two presses of any of the up, down, chunk up, chunk down buttons. In this way we capture the sounds they are playing and in what time order they’re playing the sounds. 

The code below is in our button listeners. If the record option is selected (fileCreated is true), whenever a sound is played, the required content is written to the file

if(fileCreated) {
   try {
       // time between two clicks of buttons
       fs.write(Long.toString(wait_time).getBytes());
       fs.write(" ".getBytes());
       //current soundID being played
	  fs.write(Integer.toString(soundID[chordIndex][0])
.getBytes());
       fs.write(" ".getBytes());
   }
   catch (IOException e) {
       e.printStackTrace();
   }
}



File Handling (in the Second Activity)

Our mobile application required File Handling for three major functionalities

Play: If a user clicks the Play button, the application plays the selected file. We achieve this by reading the resource ids and timestamps for each resource in stored in the text file that the user has selected. It involves the use of simple fstreams in a read-only mode. From the file contents, it gets the soundIDs and time intervals and plays them in the exact same way the user played it.

//text_array is the text from a file split into an array of words
for (int i = 0; i < text_array.length; i++){
   int temp = Integer.parseInt(text_array[i]);
   if (i % 2 == 0){
       //getting the interval
       times.add(temp);
   }
   else{
       //getting the sound
       soundIds.add(temp);
   }
}

for (int i = 0; i < soundIds.size(); i++){
   long futureTime = System.currentTimeMillis() + times.get(i);
   //wait for futureTime ms
   while (System.currentTimeMillis() < futureTime) {
       synchronized (this) {
           try {
               wait(futureTime - System.currentTimeMillis());
           } catch (Exception e) {
           }
       }
   }
   pool.stop(stream);
   //play the soundId
   stream = pool.play(soundIds.get(i), 1.0f, 1.0f, 1, 0, 1);
}

Stitch : If a user clicks the Stitch button, the application stitches all the selected files into a single text file and prompts the user to name the newly created file. This is achieved by opening the files in a read/write mode. We get a list of selected filename from the checked list view and then search for those files using the default file path set by the application. We then create an arraylist of files to store all the retrieved (checked) files. After that, we create an empty text file with a name inputted by the user and open it in write mode. We then loop through the arraylist and keep on appending all the selected files to the empty text file. On a successful append we close all the streams and the list view gets updated to reflect the newly created file. The main intention behind adding a stitch option is to allow the user to stitch multiple sound files created in different sessions into a single music file, thus, in a way providing a user to take breaks in-between recording a long melody.

Delete : If a user clicks the delete button, a prompt is thrown to confirm if the user actually intends to delete the selected file. On a negative response (No-response) a toast is thrown alerting that file deletion has been cancelled. On a positive response (Yes-response) all the selected files are removed. We get the selected files through the checked listview and simply call the remove command to delete the selected files.



















