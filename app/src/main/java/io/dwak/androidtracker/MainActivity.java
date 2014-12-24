package io.dwak.androidtracker;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.dwak.androidtracker.viewmodel.FavoriteFoodViewModel;
import io.dwak.tracker.Tracker;
import io.dwak.tracker.TrackerComputation;
import io.dwak.tracker.TrackerComputationFunction;


public class MainActivity extends ActionBarActivity {
    public static final String PIZZA = "PIZZA";
    public static final String MANGOES = "MANGOES";
    public boolean mIsPizza = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.text);
        final Button button = (Button) findViewById(R.id.button);
        final TextView sliderValue = (TextView) findViewById(R.id.slider_value);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);

        final FavoriteFoodViewModel viewModel = new FavoriteFoodViewModel("PIZZA", 100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                viewModel.setFavoritePercentage(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Tracker.getInstance().autoRun(new TrackerComputationFunction() {
            @Override
            public void callback() {
                textView.setText(viewModel.getFavoriteFood());
            }
        });

        final TrackerComputation favoriteFoodPercentageComputation = Tracker.getInstance().autoRun(new TrackerComputationFunction() {
            @Override
            public void callback() {
                seekBar.setProgress(viewModel.getFavoritePercentage());
                sliderValue.setText("Percent favorite: " + String.valueOf(viewModel.getFavoritePercentage()));

                if (viewModel.getFavoritePercentage() == 0) {
                    Toast.makeText(MainActivity.this, "You don't like this food at all!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPizza = !mIsPizza;
                viewModel.setFavoriteFood(mIsPizza ? PIZZA : MANGOES);
            }
        });
    }
}

