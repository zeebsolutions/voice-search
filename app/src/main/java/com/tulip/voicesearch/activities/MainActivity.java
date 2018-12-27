package com.tulip.voicesearch.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
    public void rattingDialog()    {
        final Dialog view = new Dialog(this);
        view.requestWindowFeature(Window.FEATURE_NO_TITLE);
        view.setContentView(R.layout.ranking_app_dialog_box);
        Button Exit=view.findViewById(R.id.Exit);
        Button Feedback=view.findViewById(R.id.feedback);
        Button Cancel=view.findViewById(R.id.Cancel);
       /*
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog view = builder.create();
        final View view = LayoutInflater.from(this).inflate(R.layout.ranking_app_dialog_box,null);
        Button Exit=view.findViewById(R.id.Exit);
        Button Feedback=view.findViewById(R.id.feedback);
        Button Cancel=view.findViewById(R.id.Cancel);
*/
        ImageButton adButton0=view.findViewById(R.id.ad_button0);
        adButton0.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tulip.routefinder")));}});
        ImageButton adButton1=view.findViewById(R.id.ad_button1);
        adButton1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.combo.voiceassistant")));}});
        ImageButton adButton2=view.findViewById(R.id.ad_button2);
        adButton2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.combo.navigation")));}});
        ImageButton adButton3=view.findViewById(R.id.ad_button3);
        adButton3.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.combo.tourtheworld")));}});
        ImageButton adButton4=view.findViewById(R.id.ad_button4);
        adButton4.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sparrow.quiztrivia")));}});
        ImageButton adButton5=view.findViewById(R.id.ad_button5);
        adButton5.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.appsstandard.unitconverter")));}});
        ImageButton adButton6=view.findViewById(R.id.ad_button6);
        adButton6.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sparrow.fun")));}});

        Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                MainActivity.this.finish();
                System.exit(0);
            }
        });
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.cancel();
            }
        });        Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" +
                                    MainActivity.this.getPackageName())));
                }
            }
        });
/*
        builder.setView(view);
        view.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
*/
        view.show();
    }

    @Override
    public void onBackPressed() {
        rattingDialog();
    }


    private void policyDialog() {
        final Dialog view = new Dialog(this);
        view.requestWindowFeature(Window.FEATURE_NO_TITLE);
        view.setContentView(R.layout.app_policy);
        Button Exit=view.findViewById(R.id.Exit);
        Button Feedback=view.findViewById(R.id.feedback);
        Button Cancel=view.findViewById(R.id.Cancel);
        TextView appPolicy=view.findViewById(R.id.app_policy);
        appPolicy.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.freeprivacypolicy.com/privacy/view/525aeb460d6c6e930ea8b4f10ced4718")));}});

       /*
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog view = builder.create();
        final View view = LayoutInflater.from(this).inflate(R.layout.ranking_app_dialog_box,null);
        Button Exit=view.findViewById(R.id.Exit);
        Button Feedback=view.findViewById(R.id.feedback);
        Button Cancel=view.findViewById(R.id.Cancel);
*/
        ImageButton adButton0=view.findViewById(R.id.ad_button0);
        adButton0.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tulip.routefinder")));}});
        ImageButton adButton1=view.findViewById(R.id.ad_button1);
        adButton1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.combo.voiceassistant")));}});
        ImageButton adButton2=view.findViewById(R.id.ad_button2);
        adButton2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.combo.navigation")));}});
        ImageButton adButton3=view.findViewById(R.id.ad_button3);
        adButton3.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.combo.tourtheworld")));}});
        ImageButton adButton4=view.findViewById(R.id.ad_button4);
        adButton4.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sparrow.quiztrivia")));}});
        ImageButton adButton5=view.findViewById(R.id.ad_button5);
        adButton5.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.appsstandard.unitconverter")));}});
        ImageButton adButton6=view.findViewById(R.id.ad_button6);
        adButton6.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sparrow.fun")));}});

        Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                MainActivity.this.finish();
                System.exit(0);
            }
        });
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.cancel();
            }
        });        Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" +
                                    MainActivity.this.getPackageName())));
                }
            }
        });
/*
        builder.setView(view);
        view.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
*/
        view.show();

    }

    public void onPolicyClicked(View view) { policyDialog();
    }

}
