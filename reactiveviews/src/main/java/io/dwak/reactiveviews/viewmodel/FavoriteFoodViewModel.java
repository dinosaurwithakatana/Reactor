package io.dwak.reactiveviews.viewmodel;

import io.dwak.reactor.ReactorDependency;

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
    private ReactorDependency mIsPizzaDep = new ReactorDependency();
    private ReactorDependency mFavoriteFoodDep = new ReactorDependency();
    private ReactorDependency mFavoritePercentageDep = new ReactorDependency();
    private ReactorDependency mEditTextValueDep = new ReactorDependency();

    public FavoriteFoodViewModel(String favoriteFood, int favoritePercentage) {
        mIsPizza = PIZZA.equals(favoriteFood);
        mFavoriteFood = favoriteFood;
        mFavoritePercentage = favoritePercentage;
        mEditTextValue = "";
    }

    public boolean isPizza() {
        mIsPizzaDep.depend();
        return mIsPizza;
    }

    public void setIsPizza(boolean isPizza) {
        this.mIsPizza = isPizza;
        mIsPizzaDep.changed();
    }

    public String getFavoriteFood() {
        mFavoriteFoodDep.depend();
        return mFavoriteFood;
    }

    public void setFavoriteFood(String favoriteFood) {
        this.mFavoriteFood = favoriteFood;
        mFavoriteFoodDep.changed();
    }

    public int getFavoritePercentage() {
        mFavoritePercentageDep.depend();
        return mFavoritePercentage;
    }

    public void setFavoritePercentage(int favoritePercentage) {
        this.mFavoritePercentage = favoritePercentage;
        mFavoritePercentageDep.changed();
    }

    public String getEditTextValue() {
        mEditTextValueDep.depend();
        return mEditTextValue;
    }

    public void setEditTextValue(String editTextValue) {
        this.mEditTextValue = editTextValue;
        mEditTextValueDep.changed();
    }
}
