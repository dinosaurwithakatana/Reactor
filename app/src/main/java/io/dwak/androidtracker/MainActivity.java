package io.dwak.androidtracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.dwak.tracker.Tracker;
import io.dwak.tracker.TrackerComputationFunction;


public class MainActivity extends ActionBarActivity {

    private Tracker.Dependency mFavoriteFoodDep;
    private String mFavoriteFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.text);
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavoriteFood(getFavoriteFood() + "a");
            }
        });
        mFavoriteFoodDep = new Tracker.Dependency();
        mFavoriteFood = "TEST";

        Tracker.getInstance().autoRun(new TrackerComputationFunction() {
            @Override
            public void callback() {
                textView.setText(getFavoriteFood());
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                setFavoriteFood("MANGOES");
            }
        }, 1000);
    }

    private String getFavoriteFood() {
        mFavoriteFoodDep.depend();
        return mFavoriteFood;
    }

    private void setFavoriteFood(String favoriteFood) {
        mFavoriteFood = favoriteFood;
        mFavoriteFoodDep.changed();
    }
}
