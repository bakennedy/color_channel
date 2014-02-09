package com.colorchannel;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import android.view.View.OnClickListener;
import android.media.SoundPool;
import android.media.AudioManager;

public class MainActivity extends Activity {

    final String TAG = "MainActivity";
    int[] colorValues = {Color.RED, Color.GREEN, Color.BLUE,
            Color.CYAN, Color.YELLOW, Color.MAGENTA};
    String[] colorNames = {"Red", "Green", "Blue",
            "Cyan", "Yellow", "Magenta"};
    boolean[] colorsUsed = new boolean[6];
    int which = 0;
    int score = 0;

    SoundPool sound;
    int[] success = new int[6];
    int[] fail = new int[1];
    boolean loaded = false;

    private static String getStringFromInputStream(InputStream inFile) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(inFile));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    void readColors() {
        Log.i(TAG, "Reading Colors");
        InputStream inFile = getResources().openRawResource(R.raw.colors);
        String jsonContents = getStringFromInputStream(inFile);
        JSONTokener tokenizer = new JSONTokener(jsonContents);
        JSONObject colorDict;
        try {
            colorDict = (JSONObject) tokenizer.nextValue();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return;
        }
        int length = colorDict.length();
        int i=0;
        colorValues = new int[length];
        colorNames = new String[length];
        colorsUsed = new boolean[length];
        Iterator<String> keys = colorDict.keys();
        String name;
        while(keys.hasNext()) {
            name = keys.next();
            colorNames[i] = name;
            try {
                colorValues[i] = Color.parseColor(colorDict.getString(name));
            } catch (JSONException e) {
                Log.e(TAG, "Failed to read color: " + name);
            }
            i++;
        }
        Log.i(TAG, "Finished reading " + Integer.toString(i) + " colors");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "Whoa");
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
            readColors();
        }
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.sound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        this.sound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        loadSounds();
    }

    public void playSound (int soundId) {
        if (loaded) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = actualVolume / maxVolume;
            this.sound.play(soundId, volume, volume, 1, 0, 1f);
        }
    }

    public void random_success () {
        int soundId = (int) (Math.random() * success.length);
        playSound(success[soundId]);
    }

    public void random_fail () {
        int soundId = (int) (Math.random() * fail.length);
        playSound(fail[soundId]);
    }

    public void loadSounds () {
        this.success[0] = this.sound.load(this, R.raw.coin01, 1);
        this.success[1] = this.sound.load(this, R.raw.coin02, 1);
        this.success[2] = this.sound.load(this, R.raw.coin03, 1);
        this.success[3] = this.sound.load(this, R.raw.coin04, 1);
        this.success[4] = this.sound.load(this, R.raw.coin05, 1);
        this.success[5] = this.sound.load(this, R.raw.coin06, 1);

        this.fail[0] = this.sound.load(this, R.raw.fail, 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            score = 0;
            TextView scoreView = (TextView) findViewById(R.id.score);
            scoreView.setText("0");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<Integer> randomColors(int number) {
        ArrayList<Integer> colors = new ArrayList<Integer>(number);
        int pick, o = 0;
        for (int i = colorValues.length - number; i < colorValues.length; i++) {
            pick = (int) (Math.random() * (i + 1));
            if (colorsUsed[pick]) {
                pick = i;
            }
            colors.add(o, pick);
            colorsUsed[pick] = true;
            o++;
        }
        for (o=0;o<number;o++) {
            colorsUsed[colors.get(o)] = false;
        }
        Collections.shuffle(colors);
        return colors;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnClickListener {

        Button[] buttons = new Button[9];
        TextView colorName;
        View rootView;
        TextView scoreView;

        public PlaceholderFragment() {
        }

        private void getButtons() {
            buttons[0] = (Button) rootView.findViewById(R.id.button1);
            buttons[1] = (Button) rootView.findViewById(R.id.button2);
            buttons[2] = (Button) rootView.findViewById(R.id.button3);
            buttons[3] = (Button) rootView.findViewById(R.id.button4);
            buttons[4] = (Button) rootView.findViewById(R.id.button5);
            buttons[5] = (Button) rootView.findViewById(R.id.button6);
            buttons[6] = (Button) rootView.findViewById(R.id.button7);
            buttons[7] = (Button) rootView.findViewById(R.id.button8);
            buttons[8] = (Button) rootView.findViewById(R.id.button9);
            for(int i=0;i<9;i++){
                buttons[i].setTag(i);
                buttons[i].setSoundEffectsEnabled(false);
            }
        }

        void colorAll( MainActivity main ) {
            main.which = (int) (Math.random() * 9);
            ArrayList<Integer> colors = main.randomColors(9);
            int c;
            for (int i=0; i<9; i++) {
                c = colors.get(i);
                if (i==main.which) setColorName(main.colorNames[c]);
                setButtonColor(i, main.colorValues[c]);
            }
        }

        public void onClick(View button) {
            MainActivity main = (MainActivity)getActivity();
            if ((Integer) button.getTag() == main.which) {
                main.score++;
                main.random_success();
            } else {
                main.score--;
                main.random_fail();
            }
            reset(main);
        }

        public void reset(MainActivity main) {
            scoreView.setText(Integer.toString(main.score));
            colorAll(main);
        }

        public void setButtonColor(int button, int color) {
            buttons[button].setBackgroundColor(color);
        }

        public void setColorName(String name) {
            colorName.setText(name);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            colorName = (TextView) rootView.findViewById(R.id.textView);
                scoreView = (TextView) rootView.findViewById(R.id.score);
                getButtons();
                return rootView;
            }

            @Override
            public void onActivityCreated(Bundle saved) {
                super.onActivityCreated(saved);
                MainActivity main = (MainActivity) getActivity();
                colorAll(main);
                for(int i=0; i<9; i++) {
                    buttons[i].setOnClickListener(this);
            }
        }
    }

}
