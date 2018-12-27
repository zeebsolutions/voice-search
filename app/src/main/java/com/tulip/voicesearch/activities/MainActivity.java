package com.tulip.voicesearch.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.tulip.voicesearch.R;
import com.tulip.voicesearch.adapters.HistoryAdapter;
import com.tulip.voicesearch.helpers.DatabaseHelper;
import com.tulip.voicesearch.interfaces.OnHistoryClickListener;
import com.tulip.voicesearch.models.VoiceSearchHistoryModel;
import com.tulip.voicesearch.models.VoiceSearchResultModel;
import com.tulip.voicesearch.utils.VoiceTasks;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.List;

import static com.tulip.voicesearch.activities.VoiceSearch.PERMISSION_REQUEST_CODE;
import static com.tulip.voicesearch.activities.VoiceSearch.permissions;

public class MainActivity extends AppCompatActivity {

    InterstitialAd ad;

    RecyclerView recyclerView;
    List<VoiceSearchHistoryModel> models;
    HistoryAdapter adapter;

    DatabaseHelper helper;

    VoiceTasks tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (permissions(this))
                tasks = new VoiceTasks(this);
        }else
            tasks = new VoiceTasks(this);

        ad = new InterstitialAd(this);
        ad.setAdUnitId(getString(R.string.interstitial));
        ad.loadAd(new AdRequest.Builder().build());
        ad.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                startActivity(new Intent(getApplicationContext(),VoiceSearch.class));
            }
        });

        AdView banner = findViewById(R.id.banner);
        banner.loadAd(new AdRequest.Builder().build());

        findViewById(R.id.voice_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ad.isLoaded()) {
                    ad.show();
                    ad.setAdListener(new AdListener(){
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            startActivity(new Intent(getApplicationContext(),VoiceSearch.class));
                        }
                    });
                }
                else
                    startActivity(new Intent(getApplicationContext(),VoiceSearch.class));

            }
        });

        helper = new DatabaseHelper(this);
        models = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                        this,LinearLayoutManager.HORIZONTAL,false
                )
        );
        adapter = new HistoryAdapter(models);
        recyclerView.setAdapter(adapter);
        adapter.setListener(new OnHistoryClickListener() {
            @Override
            public void onClickListener(final int position) {
                if(ad.isLoaded()){
                    ad.show();
                    ad.setAdListener(new AdListener(){
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                                if (permissions(MainActivity.this))
                                    checkAction(models.get(position));
                            }
                            else
                                checkAction(models.get(position));
                        }
                    });
                }else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (permissions(MainActivity.this))
                            checkAction(models.get(position));
                    } else
                        checkAction(models.get(position));
                }
            }

            @Override
            public void onLongClickListener(int position) {
                final VoiceSearchHistoryModel model = models.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete")
                        .setMessage("Would you like to delete this item from history")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(helper.deleteHistoryItem(model.getId())){
                                    Toast.makeText(
                                            MainActivity.this,
                                            "Item Deleted",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    models.clear();
                                    models.addAll(helper.getSearchQueries());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("No",null)
                        .create()
                        .show();
            }
        });
    }

    public void checkAction(VoiceSearchHistoryModel model){
        tasks.checkAction(
                new VoiceSearchResultModel(
                        model.getAction(),
                        model.getQuery(),
                        model.getRequestCode()
                )
        );
    }




    @Override
    protected void onResume() {
        super.onResume();
        models.clear();
        models.addAll(helper.getSearchQueries());
        adapter.notifyDataSetChanged();
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String link = "http://play.google.com/store/apps/details?id=" +
                        getPackageName();
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                break;
            case R.id.rate_us:
                try {
                    startActivity(
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + getPackageName())
                            )
                    );
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" +
                                            getPackageName())
                            )
                    );
                }
                break;
            case R.id.delete_all:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete All History")
                        .setMessage("Would you like to delete all items from history")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(helper.deleteAll()){
                                    Toast.makeText(
                                            MainActivity.this,
                                            "All History Deleted",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    models.clear();
                                    models.addAll(helper.getSearchQueries());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("No",null)
                        .create()
                        .show();
                break;
        }
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
            tasks = new VoiceTasks(this);
        }
    }
}
