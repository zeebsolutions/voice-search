package com.tulip.voicesearch.models;

import android.content.Intent;

public class AppsInfo {
    String name;
    Intent intent;

    public AppsInfo(String name, Intent intent) {
        this.name = name;
        this.intent = intent;
    }

    public String getName() {
        return name;
    }

    public Intent getIntent() {
        return intent;
    }
}
