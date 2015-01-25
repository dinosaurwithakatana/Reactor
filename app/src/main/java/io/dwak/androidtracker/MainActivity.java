package io.dwak.androidtracker;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.dwak.androidtracker.viewmodel.FavoriteFoodViewModel;
import io.dwak.reactor.Reactor;
import io.dwak.reactor.ReactorComputation;
import io.dwak.reactor.ReactorComputationFunction;


public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.text);
        final Button button = (Button) findViewById(R.id.button);
        final TextView sliderValue = (TextView) findViewById(R.id.slider_value);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        final EditText editText = (EditText) findViewById(R.id.edit_text);
        final TextView editTextDisplay = (TextView) findViewById(R.id.edit_text_display);
        final FavoriteFoodViewModel viewModel = new FavoriteFoodViewModel("PIZZA", 100);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setEditTextValue(s.toString());
            }
        });
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

        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                editTextDisplay.setText(viewModel.getEditTextValue());
            }
        });
        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                textView.setText(viewModel.getFavoriteFood());
            }
        });

        final ReactorComputation favoriteFoodPercentageComputation = Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                seekBar.setProgress(viewModel.getFavoritePercentage());
                sliderValue.setText("Percent favorite: " + String.valueOf(viewModel.getFavoritePercentage()));

                if (viewModel.getFavoritePercentage() == 0) {
                    Toast.makeText(MainActivity.this, "You don't like this food at all!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react() {
                viewModel.setFavoriteFood(viewModel.isPizza() ? FavoriteFoodViewModel.PIZZA : FavoriteFoodViewModel.MANGOES);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.setPizza(!viewModel.isPizza());
            }
        });
    }
}

