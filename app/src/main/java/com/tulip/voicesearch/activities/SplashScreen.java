package com.tulip.voicesearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.tulip.voicesearch.R;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class SplashScreen extends AppCompatActivity {
    InterstitialAd ad;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ad = new InterstitialAd(this);
        ad.setAdUnitId(getString(R.string.interstitial));
        ad.loadAd(new AdRequest.Builder().build());
        ad.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });


        /*//Fabric.with(this, new Crashlytics());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        */

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(ad.isLoaded()) {
                    ad.show();
                    ad.setAdListener(new AdListener(){
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();

                        }
                    });
                }
                else
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }, 3000);



    }
}