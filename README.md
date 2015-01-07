Reactor
==============

Port of meteor's transparent reactive framework for android

Meteor's [tracker](https://github.com/meteor/meteor/blob/devel/packages/tracker/tracker.js)

How to use:
-----------

###Example

Field dependecies in an Activity:

```java 
    private String getFavoriteFood() {
        mFavoriteFoodDep.depend();  // when this getter is called, the dependecy is added to the tracker
        return mFavoriteFood;
    }

    private void setFavoriteFood(String favoriteFood) {
        mFavoriteFood = favoriteFood;
        mFavoriteFoodDep.changed(); // when this setter is called, the dependecy is notified that the property has changed
    }
```

Changing and reacting to property change:
    
```java 
    mFavoriteFoodDep = new TrackerDependency();
    mFavoriteFood = "PIZZA";

    Tracker.getInstance().autoRun(new TrackerComputationFunction() {
        @Override
        public void callback() {
            Log.d("TAG", getFavoriteFood());
        }
    });

    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
        @Override
        public void run() {
            setFavoriteFood("MANGOES");
        }
    }, 1000);
```

with the above code block, you would see `PIZZA` in your logs, and in 1 second, would see `MANGOES`

###Intellij/Android Studio plugin

Use this plugin to help generate some of the boilerplate: https://github.com/dinosaurwithakatana/android-tracker-intellij-plugin
