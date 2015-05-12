package com.sinch.messagingtutorialskeleton;

import android.app.Application;


import com.parse.Parse;
import com.parse.PushService;


/**
 * Main class for application, initialize Parse
 */
public class Heartbeat extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "LewmFIgnn4R9UZLRoFGh7vckZTBEXIwT7HBbADsl", "WtdcogVtW4gNmYfrFKeCLJTbUNxNZ9IT4Gd6Pf6p");


        PushService.setDefaultPushCallback(this, LoginActivity.class);
    }
}
