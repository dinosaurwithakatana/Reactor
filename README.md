Reactor
==============

Port of meteor's transparent reactive framework for android

Meteor's [tracker](https://github.com/meteor/meteor/blob/devel/packages/tracker/tracker.js)

[Tracker docs](https://github.com/meteor/meteor/wiki/Tracker-Manual)

How to use:
-----------

###Example

Field dependecies:

```java 

    //This wraps your variable in a reactive object
    private ReactorVar<String> mFavoriteFood = new ReactorVar<String>();

    public String getFavoriteFood() { 
        return mFavoriteFood.getValue(); // this binds the dependency
    }

    public void setFavoriteFood(String favoriteFood) {
        mFavoriteFood.setValue(favoriteFood); //this lets the ReactiveVar know the dep has changed
    }
```


Changing and reacting to property change:
    
```java 
    Reactor.getInstance().autoRun(new ReactorComputationFunction() {
        @Override
        public void react() {
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

Use this plugin to help generate some of the boilerplate: https://github.com/dinosaurwithakatana/android-reactor-intellij-plugin
