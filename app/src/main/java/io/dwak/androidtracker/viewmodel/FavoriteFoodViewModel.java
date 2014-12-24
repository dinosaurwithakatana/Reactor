package io.dwak.androidtracker.viewmodel;

import io.dwak.tracker.TrackerDependency;

/**
 * Created by vishnu on 12/23/14.
 */
public class FavoriteFoodViewModel {
    private String mFavoriteFood;
    private int mFavoritePercentage;
    private TrackerDependency mFavoriteFoodDep;
    private TrackerDependency mFavoritePercentageDep;

    public FavoriteFoodViewModel(String favoriteFood, int favoritePercentage) {
        mFavoriteFoodDep = new TrackerDependency();
        mFavoritePercentageDep = new TrackerDependency();
        mFavoriteFood = favoriteFood;
        mFavoritePercentage = favoritePercentage;
    }

    public int getFavoritePercentage() {
        mFavoritePercentageDep.depend();
        return mFavoritePercentage;
    }

    public void setFavoritePercentage(int favoritePercentage) {
        mFavoritePercentage = favoritePercentage;
        mFavoritePercentageDep.changed();
    }

    public String getFavoriteFood() {
        mFavoriteFoodDep.depend();
        return mFavoriteFood;
    }

    public void setFavoriteFood(String favoriteFood) {
        mFavoriteFood = favoriteFood;
        mFavoriteFoodDep.changed();
    }

}
