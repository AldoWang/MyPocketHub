package com.hdsx.mypockethub;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static App instance;
    private static AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initAppComponent();
    }

    private void initAppComponent() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static Context getContext() {
        return instance.getBaseContext();
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }

}
