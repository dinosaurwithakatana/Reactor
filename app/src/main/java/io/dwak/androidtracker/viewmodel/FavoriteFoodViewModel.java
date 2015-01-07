package io.dwak.androidtracker.viewmodel;

import io.dwak.tracker.ReactorDependency;

/**
 * Created by vishnu on 12/23/14.
 */
public class FavoriteFoodViewModel {
    public static String PIZZA = "PIZZA";
    public static String MANGOES = "MANGOES";
    private boolean mIsPizza;
    private String mFavoriteFood;
    private int mFavoritePercentage;

    private String mEditTextValue;
    private ReactorDependency mIsPizzaDep;
    private ReactorDependency mFavoriteFoodDep;
    private ReactorDependency mFavoritePercentageDep;
    private ReactorDependency mEditTextValueDep;

    public FavoriteFoodViewModel(String favoriteFood, int favoritePercentage) {
        mIsPizzaDep = new ReactorDependency();
        mFavoriteFoodDep = new ReactorDependency();
        mFavoritePercentageDep = new ReactorDependency();
        mEditTextValueDep = new ReactorDependency();
        mIsPizza = PIZZA.equals(favoriteFood);
        mFavoriteFood = favoriteFood;
        mFavoritePercentage = favoritePercentage;
        mEditTextValue = "";
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

    public boolean isPizza() {
        mIsPizzaDep.depend();
        return mIsPizza;
    }

    public void setPizza(boolean isPizza) {
        mIsPizza = isPizza;
        mIsPizzaDep.changed();
    }

    public String getEditTextValue() {
        mEditTextValueDep.depend();
        return mEditTextValue;
    }

    public void setEditTextValue(String editTextValue) {
        mEditTextValue = editTextValue;
        mEditTextValueDep.changed();
    }
}
