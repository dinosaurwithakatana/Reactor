package io.dwak.reactiveviews.viewmodel;

import io.dwak.reactor.ReactorVar;

/**
 * Created by vishnu on 12/23/14.
 */
public class FavoriteFoodViewModel {
    public static String PIZZA = "PIZZA";
    public static String MANGOES = "MANGOES";
    private ReactorVar<Boolean> mIsPizza = new ReactorVar<Boolean>();
    private ReactorVar<String> mFavoriteFood = new ReactorVar<String>();
    private ReactorVar<Integer> mFavoritePercentage = new ReactorVar<Integer>();
    private ReactorVar<String> mEditTextValue = new ReactorVar<String>();

    public FavoriteFoodViewModel(String favoriteFood, int favoritePercentage) {
        mIsPizza.setValue(PIZZA.equals(favoriteFood));
        mFavoriteFood.setValue(favoriteFood);
        mFavoritePercentage.setValue(favoritePercentage);
        mEditTextValue.setValue("");
    }

    public Boolean getIsPizza() {
        return mIsPizza.getValue();
    }

    public void setIsPizza(Boolean isPizza) {
        mIsPizza.setValue(isPizza);
    }

    public String getFavoriteFood() {
        return mFavoriteFood.getValue();
    }

    public void setFavoriteFood(String favoriteFood) {
        mFavoriteFood.setValue(favoriteFood);
    }

    public Integer getFavoritePercentage() {
        return mFavoritePercentage.getValue();
    }

    public void setFavoritePercentage(Integer favoritePercentage) {
        mFavoritePercentage.setValue(favoritePercentage);
    }

    public String getEditTextValue() {
        return mEditTextValue.getValue();
    }

    public void setEditTextValue(String editTextValue) {
        mEditTextValue.setValue(editTextValue);
    }
}
