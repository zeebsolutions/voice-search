package com.tulip.voicesearch.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.tulip.voicesearch.R;
import com.tulip.voicesearch.adapters.VoiceSearchListAdapter;
import com.tulip.voicesearch.interfaces.ExtractWitAi;
import com.tulip.voicesearch.interfaces.OnClickListener;
import com.tulip.voicesearch.models.VoiceSearchModel;
import com.tulip.voicesearch.models.VoiceSearchResultModel;
import com.tulip.voicesearch.models.WitModels.Wit;
import com.tulip.voicesearch.utils.Populate;
import com.tulip.voicesearch.utils.VoiceTasks;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VoiceSearch extends AppCompatActivity {

    public static final int PERMISSION_REQUEST_CODE = 100;

    VoiceSearchListAdapter adapter;
    List<VoiceSearchModel> searchModels;
    VoiceTasks voiceTasks;
    Switch switchSuggestion;
    Boolean suggestion;

    InterstitialAd ad;
    RewardedVideoAd videoAd;

    public void loadRewardVideoAd(){
        videoAd = MobileAds.getRewardedVideoAdInstance(this);
        videoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {

            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                startAssistant();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }

            @Override
            public void onRewardedVideoCompleted() {

            }
        });
        videoAd.loadAd(getString(R.string.reward),
                new AdRequest.Builder().build());
    }

    public void startAssistant(){
        try{
            startActivity(
                    new Intent(
                            Intent.ACTION_VOICE_COMMAND
                    ).setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    )
            );
        }catch (Exception e){
            Toast.makeText(this, "You device does not support assistant", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_search);

        loadRewardVideoAd();
        ad = new InterstitialAd(this);
        ad.setAdUnitId(getString(R.string.interstitial));
        ad.loadAd(new AdRequest.Builder().build());

        AdView banner = findViewById(R.id.banner);
        banner.loadAd(new AdRequest.Builder().build());

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (permissions(this))
                voiceTasks = new VoiceTasks(this);
        } else
            voiceTasks = new VoiceTasks(this);


        switchSuggestion= findViewById(R.id.switch_suggestion);
        SharedPreferences preferences = getSharedPreferences(
                getString(
                        R.string.app_name
                ),
                MODE_PRIVATE
        );
        suggestion = preferences.getBoolean(getString(R.string.switch_suggestion),true);
        switchSuggestion.setChecked(suggestion);
        switchSuggestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(ad.isLoaded())
                    ad.show();
                suggestion = b;
                switchSuggestion.setChecked(suggestion);
                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name),
                        MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.switch_suggestion),b);
                editor.apply();
            }
        });

        populateModels();
        adapter = new VoiceSearchListAdapter(searchModels);
        adapter.setOnClickListener(new OnClickListener() {
            @Override
            public void setOnItemClickListener(int position) {
                voiceRecognitionIntent(searchModels.get(position));
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(videoAd.isLoaded())
                    videoAd.show();
                else if(ad.isLoaded()){
                    ad.show();
                    ad.setAdListener(new AdListener(){
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            startAssistant();
                        }
                    });
                }else
                {
                    startAssistant();
                }
            }
        });
    }

    static boolean permissions(Activity activity){
        List<String> listOfPermissions = new ArrayList<>();
        if(ActivityCompat.checkSelfPermission(activity,Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            listOfPermissions.add(Manifest.permission.READ_CONTACTS);
        }
        if(ActivityCompat.checkSelfPermission(activity,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            listOfPermissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if(ActivityCompat.checkSelfPermission(activity,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
            listOfPermissions.add(Manifest.permission.CALL_PHONE);
        }
        if(listOfPermissions.size()==0)
            return true;
        else{
            String[] permissions = new String[listOfPermissions.size()];
            permissions = listOfPermissions.toArray(permissions);
            ActivityCompat.requestPermissions(activity,permissions,PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    public void voiceRecognitionIntent(VoiceSearchModel model){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                model.getName());
        try {
            if(permissions(this))
                startActivityForResult(intent, model.getRequestCode());
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void populateModels(){
        searchModels = new ArrayList<>();
        searchModels.add(new VoiceSearchModel(R.drawable.call_search,getString(R.string.speak_contact_name),0));
        searchModels.add(new VoiceSearchModel(R.drawable.sms_search,getString(R.string.speak_contact_name),1));
        searchModels.add(new VoiceSearchModel(R.drawable.apps_search,getString(R.string.speak_app_name),2));
        searchModels.add(new VoiceSearchModel(R.drawable.google_search,getString(R.string.google_search),3));
        searchModels.add(new VoiceSearchModel(R.drawable.wiki_how,getString(R.string.wiki_how),4));
        searchModels.add(new VoiceSearchModel(R.drawable.wikipedia_search,getString(R.string.wikipedia_search),5));
        searchModels.add(new VoiceSearchModel(R.drawable.news_search,getString(R.string.new_search),6));
        searchModels.add(new VoiceSearchModel(R.drawable.maps_search,getString(R.string.speak_place_name),7));
        searchModels.add(new VoiceSearchModel(R.drawable.merriam_webster,getString(R.string.merriam_webster),8));
        searchModels.add(new VoiceSearchModel(R.drawable.youtube_search,getString(R.string.youtube_search),9));
        searchModels.add(new VoiceSearchModel(R.drawable.twitter,getString(R.string.twitter),10));
        searchModels.add(new VoiceSearchModel(R.drawable.facebook,getString(R.string.facebook),11));
        searchModels.add(new VoiceSearchModel(R.drawable.playstore_search,getString(R.string.speak_app_name_playstore),12));
        searchModels.add(new VoiceSearchModel(R.drawable.bing_search,getString(R.string.bing),13));
        searchModels.add(new VoiceSearchModel(R.drawable.yahoo_search,getString(R.string.yahoo_search),14));
        searchModels.add(new VoiceSearchModel(R.drawable.duckduckgo_search,getString(R.string.duck_duck_go_search),15));
        searchModels.add(new VoiceSearchModel(R.drawable.ask_search,getString(R.string.ask_search),16));
        searchModels.add(new VoiceSearchModel(R.drawable.aol_search,getString(R.string.aol_search),17));
        searchModels.add(new VoiceSearchModel(R.drawable.reddit_search,getString(R.string.reddit_search),18));
        searchModels.add(new VoiceSearchModel(R.drawable.dailymotion,getString(R.string.dailymotion),19));
        searchModels.add(new VoiceSearchModel(R.drawable.meta_cafe,getString(R.string.speak_meta_cafe),20));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE&&grantResults.length>0){
            for (int result: grantResults){
                if(result == PackageManager.PERMISSION_DENIED)
                {
                    Toast.makeText(this, "Please allow all permissions for Voice Search", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            voiceTasks = new VoiceTasks(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK&&data!=null) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (suggestion) {
                populateSuggestions(results, requestCode);
            } else {
                voiceTasks.checkAction(new VoiceSearchResultModel(Populate.getAction(requestCode), results.get(0), requestCode));
                try{
                    //extractTextMeaning(Populate.getAction(requestCode)+" "+results.get(0));
                }catch (Exception ignored){

                }
            }
        }
    }

    private void populateSuggestions(final List<String> results, final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suggestions");
        builder.setItems(results.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                voiceTasks.checkAction(new VoiceSearchResultModel(Populate.getAction(requestCode),results.get(i),requestCode));
                //extractTextMeaning(Populate.getAction(requestCode)+" "+results.get(i));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void extractTextMeaning(String query){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.wit.ai/")
                .build();

        ExtractWitAi witAi = retrofit.create(ExtractWitAi.class);

        Call<Wit> call = witAi
                .getWitAi("https://api.wit.ai/message?v=1946182168793170&" +
                        "q="+query);
        call.enqueue(new Callback<Wit>() {
            @Override
            public void onResponse(Call<Wit> call, Response<Wit> response) {

            }

            @Override
            public void onFailure(Call<Wit> call, Throwable t) {

            }
        });

    }
}