package io.dwak.reactiveviews;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import io.dwak.reactiveviews.viewmodel.FavoriteFoodViewModel;
import io.dwak.reactiveviews.widget.MainView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainView mainView = (MainView) findViewById(R.id.main);
        FavoriteFoodViewModel favoriteFoodViewModel = new FavoriteFoodViewModel(FavoriteFoodViewModel.PIZZA, 100);
        mainView.setViewModel(favoriteFoodViewModel);
    }
}
