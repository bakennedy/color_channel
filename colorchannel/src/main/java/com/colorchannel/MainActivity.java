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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import android.os.Handler;


public class MainActivity extends Activity {

    final String TAG = "MainActivity";
    int[] colorValues = {Color.RED, Color.GREEN, Color.BLUE,
            Color.CYAN, Color.YELLOW, Color.MAGENTA,
            Color.LTGRAY, Color.GRAY, Color.DKGRAY};
    String[] colorNames = {"Red", "Green", "Blue",
            "Cyan", "Yellow", "Magenta",
            "Light Gray", "Gray", "Dark Gray"};
    boolean[] colorsUsed = new boolean[9];
    int which = 0;
    int score = 0;
    int minDistance = 20, maxDistance = 120;
    PlaceholderFragment frag;

    SoundPool sound;
    int success;
    int fail;
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

    public ArrayList<Integer> boundedRandom(int color, int minRadius, int maxRadius) {
        ArrayList<Integer> colors = new ArrayList<Integer>(320);
        int rmin = minRadius * maxRadius;
        int rmax = maxRadius * maxRadius;
        int distance;
        int c, sred, sgreen, sblue;
        int dred, dgreen, dblue;
        int red, green, blue;
        for (int i=0; i<colorValues.length; i++)  {
            c = colorValues[i];
            sred = Color.red(color);
            sgreen = Color.green(color);
            sblue = Color.blue(color);
            dred = Color.red(c);
            dgreen = Color.green(c);
            dblue = Color.blue(c);
            red = dred - sred;
            green = dgreen - sgreen;
            blue = dblue - sblue;
            distance = red * red + green * green + blue * blue;
            if (distance <= rmax && distance >= rmin && c != color) {
                colors.add(i);
            }
        }
        return colors;
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
        Log.i(TAG, " == onCreate");
        if (savedInstanceState == null) {
            Log.i(TAG, " == onCreate (savedInstance == null)");
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        } else {
            which = savedInstanceState.getInt("which");
            score = savedInstanceState.getInt("score");
            minDistance = savedInstanceState.getInt("minDistance");
            maxDistance = savedInstanceState.getInt("maxDistance");
        }
        readColors();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, " == onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt("which", which);
        outState.putInt("score", score);
        outState.putInt("minDistance", minDistance);
        outState.putInt("maxDistance", maxDistance);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, " == onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
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
        playSound(success);
    }

    public void random_fail () {
        playSound(fail);
    }

    public void loadSounds () {
        success = this.sound.load(this, R.raw.success, 1);
        fail = this.sound.load(this, R.raw.fail, 1);
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
        if (id == R.id.action_reset) {
            score = 0;
            TextView scoreView = (TextView) findViewById(R.id.score);
            scoreView.setText("0");
            frag.colorAll();
            return true;
        } else if (id == R.id.action_easy) {
            minDistance = 60;
            maxDistance = 200;
            frag.colorAll();
            return true;
        } else if (id == R.id.action_normal) {
            minDistance = 20;
            maxDistance = 120;
            frag.colorAll();
            return true;
        }
        else if (id == R.id.action_hard) {
            minDistance = 0;
            maxDistance = 40;
            frag.colorAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<Integer> randomColors(ArrayList<Integer> colorIndices, int number) {
        ArrayList<Integer> colors = new ArrayList<Integer>(number);
        boolean[] colorsUsed = new boolean[colorIndices.size()];
        int pick, o = 0;
        for (int i = colorIndices.size() - number; i < colorIndices.size(); i++) {
            pick = (int) (Math.random() * (i + 1));
            if (colorsUsed[pick]) {
                pick = i;
            }
            colors.add(o, colorIndices.get(pick));
            colorsUsed[pick] = true;
            o++;
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
        MainActivity activity;
        Animation success_anim, fail_anim, score_success_anim, score_fail_anim;
        boolean canClick = true;
        final String TAG = "Fragment";


        public PlaceholderFragment() {
        }

        public void onAttach(Activity activity) {
            Log.i(TAG, " == onAttach");
            super.onAttach(activity);
            this.activity = (MainActivity) activity;
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

        void colorAll() {
            ArrayList<Integer> neighbors;
            int randomColor, randomColorsColor;
            do {
                randomColor = (int) (Math.random() * activity.colorValues.length);
                randomColorsColor = activity.colorValues[randomColor];
                neighbors = activity.boundedRandom(randomColorsColor, activity.minDistance, activity.maxDistance);
            } while (neighbors.size() < 8);
            activity.which = (int) (Math.random() * 9);
            ArrayList<Integer> colors = activity.randomColors(neighbors, 8);
            int c;
            for (int i=0, o=0; i<9; i++) {
                if (i==activity.which) {
                    setColorName(activity.colorNames[randomColor]);
                    setButtonColor(i, randomColorsColor);
                } else {
                    c = colors.get(o++);
                    setButtonColor(i, activity.colorValues[c]);
                }
            }
        }

        public void onClick(View button) {
            if (canClick) {
                canClick = false;
            } else {
                return;
            }
            success_anim.reset();
            final Animation score_anim;
            fail_anim.reset();
            Handler handler = new Handler();
            int next_delay;
            if ((Integer) button.getTag() == activity.which) {
                activity.score++;
                activity.random_success();
                button.startAnimation(success_anim);
                score_anim = score_success_anim;
                next_delay = 0;
            } else {
                activity.score--;
                activity.random_fail();
                button.startAnimation(fail_anim);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buttons[activity.which].startAnimation(success_anim);
                    }
                }, 500);
                score_anim = score_fail_anim;
                next_delay = 500;
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scoreView.setText(Integer.toString(activity.score));
                    scoreView.startAnimation(score_anim);
                }
            }, 500 + next_delay);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    colorAll();
                    canClick = true;
                }
            }, 1000 + next_delay);
        }

        public void reset() {
            scoreView.setText(Integer.toString(activity.score));
            colorAll();
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
            Log.i(TAG, " == onCreateView");
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle saved) {
            Log.i(TAG, " == onActivityCreated");
            super.onActivityCreated(saved);
            colorName = (TextView) rootView.findViewById(R.id.textView);
            scoreView = (TextView) rootView.findViewById(R.id.score);
            getButtons();
            activity.frag = this;
            success_anim = AnimationUtils.loadAnimation(activity, R.anim.tile_success);
            fail_anim = AnimationUtils.loadAnimation(activity, R.anim.tile_fail);
            score_success_anim = AnimationUtils.loadAnimation(activity, R.anim.score_success);
            score_fail_anim = AnimationUtils.loadAnimation(activity, R.anim.score_fail);
            for(int i=0; i<9; i++) {
                buttons[i].setOnClickListener(this);
            }
            colorAll();
        }
    }

}
