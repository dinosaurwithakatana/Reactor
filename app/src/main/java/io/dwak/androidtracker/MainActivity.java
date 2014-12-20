package io.dwak.androidtracker;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import io.dwak.tracker.Tracker;
import io.dwak.tracker.TrackerComputationFunction;
import io.dwak.tracker.TrackerDependency;


public class MainActivity extends ActionBarActivity {
    public static final String PIZZA = "PIZZA";
    public static final String MANGOES = "MANGOES";
    private TrackerDependency mFavoriteFoodDep;
    private String mFavoriteFood;
    private TrackerDependency mSeekBarDep;
    private int mSeekBarProgress;
    private boolean mIsPizza = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.text);
        final Button button = (Button) findViewById(R.id.button);
        final TextView sliderValue = (TextView) findViewById(R.id.slider_value);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);

        mFavoriteFood = PIZZA;

        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekBarProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPizza = !mIsPizza;
                setFavoriteFood(mIsPizza ? PIZZA : MANGOES);
            }
        });
        setupDependencies(textView, sliderValue);
    }

    private void setupDependencies(final TextView textView, final TextView sliderValue) {
        mSeekBarDep = new TrackerDependency();
        mFavoriteFoodDep = new TrackerDependency();
        Tracker.getInstance().autoRun(new TrackerComputationFunction() {
            @Override
            public void callback() {
                sliderValue.setText("Seekbar progress: " + getSeekBarProgress());
            }
        });

        Tracker.getInstance().autoRun(new TrackerComputationFunction() {
            @Override
            public void callback() {
                textView.setText(getFavoriteFood());
            }
        });
    }

    private String getFavoriteFood() {
        mFavoriteFoodDep.depend();
        return mFavoriteFood;
    }

    private void setFavoriteFood(String favoriteFood) {
        mFavoriteFood = favoriteFood;
        mFavoriteFoodDep.changed();
    }

    public int getSeekBarProgress() {
        mSeekBarDep.depend();
        return mSeekBarProgress;
    }

    public void setSeekBarProgress(int seekBarProgress) {
        mSeekBarProgress = seekBarProgress;
        mSeekBarDep.changed();
    }
}
