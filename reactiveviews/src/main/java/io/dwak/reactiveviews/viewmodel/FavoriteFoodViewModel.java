package io.dwak.reactiveviews.viewmodel;

import io.dwak.reactor.ReactorVar;

/**
 * Created by vishnu on 12/23/14.
 */
public class FavoriteFoodViewModel {
    public static String PIZZA = "PIZZA";
    public static String MANGOES = "MANGOES";
    private ReactorVar<Boolean> mIsPizza;
    private ReactorVar<String> mFavoriteFood;
    private ReactorVar<Integer> mFavoritePercentage;
    private ReactorVar<String> mEditTextValue;

    public FavoriteFoodViewModel(String favoriteFood, int favoritePercentage) {
        mIsPizza = new ReactorVar<Boolean>(PIZZA.equals(favoriteFood));
        mFavoriteFood = new ReactorVar<String>(favoriteFood);
        mFavoritePercentage = new ReactorVar<Integer>(favoritePercentage);
        mEditTextValue = new ReactorVar<String>("");
    }

    public Boolean getPizza() {
        return mIsPizza.getValue();
    }

    public void setIsPizza(Boolean isPizza) {
        this.mIsPizza.setValue(isPizza);
    }

    public String getFavoriteFood() {
        return mFavoriteFood.getValue();
    }

    public void setFavoriteFood(String favoriteFood) {
        this.mFavoriteFood.setValue(favoriteFood);
    }

    public Integer getFavoritePercentage() {
        return mFavoritePercentage.getValue();
    }

    public void setFavoritePercentage(Integer favoritePercentage) {
        this.mFavoritePercentage.setValue(favoritePercentage);
    }

    public String getEditTextValue() {
        return mEditTextValue.getValue();
    }

    public void setEditTextValue(String editTextValue) {
        this.mEditTextValue.setValue(editTextValue);
    }
}
