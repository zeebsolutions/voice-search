package com.combo.voiceassistant.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.combo.voiceassistant.R;
import com.combo.voiceassistant.helpers.DatabaseHelper;
import com.combo.voiceassistant.models.AppsInfo;
import com.combo.voiceassistant.models.ContactDetail;
import com.combo.voiceassistant.models.VoiceSearchResultModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class VoiceTasks {

    private Context context;
    private List<AppsInfo> appsInfos;
    private List<ContactDetail> contactDetails;
    private DatabaseHelper helper;

    public VoiceTasks(Context context) {
        this.context = context;
        retrieveApps();
        getContactList();
        helper = new DatabaseHelper(context);
    }

    public void checkAction(VoiceSearchResultModel model){
        String query;
        try {
            query = URLEncoder.encode(model.getQuery(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        if(model.getRequestCode()>2){
            helper.addSearchQuery(model);
        }
        switch (model.getRequestCode()) {
            case 0:
                contactsMatching(model.getQuery(),context.getString(R.string.call));
                break;
            case 1:
                contactsMatching(model.getQuery(),context.getString(R.string.sms));
                break;
            case 2:
                appsMatching(model.getQuery());
                break;
            case 3:
                performAction("https://www.google.com/search?q="+query);
                break;
            case 4:
                performAction("https://www.wikihow.com/wikiHowTo?search="+query);
                break;
            case 5:
                performAction("https://en.wikipedia.org/wiki/"+query);
                break;
            case 6:
                performAction("https://news.google.com/search?q="+query);
                break;
            case 7:
                performAction("http://maps.google.com/maps?daddr="+query);
                break;
            case 8:
                performAction("https://www.merriam-webster.com/dictionary/"+query);
                break;
            case 9:
                performAction("http://www.youtube.com/results?search_query=" + query);
                break;
            case 10:
                performAction("https://twitter.com/search?q="+query);
                break;
            case 11:
                performAction("https://www.facebook.com/public/"+query);
                break;
            case 12:
                performAction("https://play.google.com/store/search?q="+query);
                break;
            case 13:
                performAction("https://www.bing.com/search?q="+query);
                break;
            case 14:
                performAction("https://search.yahoo.com/search?p="+query);
                break;
            case 15:
                performAction("https://duckduckgo.com/?q="+query);
                break;
            case 16:
                performAction("https://www.ask.com/web?q="+query);
                break;
            case 17:
                performAction("https://search.aol.com/aol/search?q="+query);
                break;
            case 18:
                performAction("https://www.reddit.com/search?q="+query);
                break;
            case 19:
                performAction("https://www.dailymotion.com/search/"+query);
                break;
            case 20:
                performAction("http://www.metacafe.com/videos_about/"+query);
                break;
        }
    }
    private void call(ContactDetail detail){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + detail.getNumber()));
        helper.addSearchQuery(new VoiceSearchResultModel("Call",detail.getName(),0));
        context.startActivity(intent);

    }

    private void sms(ContactDetail detail) {

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"+detail.getNumber()));
        helper.addSearchQuery(new VoiceSearchResultModel("SMS",detail.getName(),1));
        context.startActivity(sendIntent);

    }
    private void contactsMatching(String query, String action){
        List<ContactDetail> foundContacts = new ArrayList<>();
        query = query.toLowerCase().replace("call ","");
        query = query.toLowerCase().replace("call","");
        query = query.toLowerCase().replace("sms ","");
        query = query.toLowerCase().replace("sms","");
        query = query.toLowerCase().replace("message ","");
        query = query.toLowerCase().replace("message","");

        for(ContactDetail detail: contactDetails){
            if(detail.getName().toLowerCase().equals(query.toLowerCase())){
                foundContacts.add(detail);
            }
        }
        if(foundContacts.size()==1){
            if(action.equals(context.getString(R.string.call)))
                call(foundContacts.get(0));
            else
                sms(foundContacts.get(0));
        }else if(foundContacts.size()>1){
            createAlertDialogForContacts(foundContacts,action);
        } else {
            for(ContactDetail detail: contactDetails){
                if(detail.getName().toLowerCase().contains(query.toLowerCase())){
                    foundContacts.add(detail);
                }
            }
            if(foundContacts.size()>0){
                createAlertDialogForContacts(foundContacts,action);
            }else {
                Toast.makeText(context, "No Contact Found", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void appsMatching(String query){
        List<AppsInfo> infos = new ArrayList<>();
        for(AppsInfo info: appsInfos){
            if(info.getName().toLowerCase().equals(query.toLowerCase())){
                infos.add(info);
            }
        }
        if(infos.size()==1){
                openApp(infos.get(0));
        }else if(infos.size()>1){
            createAlertDialogForApps(infos);
        } else {
            for(AppsInfo info: appsInfos){
                if(info.getName().toLowerCase().contains(query.toLowerCase())){
                    infos.add(info);
                }
            }
            if(infos.size()>0){
                createAlertDialogForApps(infos);
            }else {
                Toast.makeText(context, "No App Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createAlertDialogForApps(final List<AppsInfo> infos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Apps Found");
        String[] names = new String[infos.size()];
        for (int i = 0;i<infos.size();i++){
            names[i] = infos.get(i).getName();
        }
        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openApp(infos.get(i));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createAlertDialogForContacts(final List<ContactDetail> foundContacts,final String action){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Contacts Found");
        String[] names = new String[foundContacts.size()];
        for (int i = 0;i<foundContacts.size();i++){
            names[i] = foundContacts.get(i).getName();
        }
        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (action.equals(context.getString(R.string.call)))
                    call(foundContacts.get(i));
                else
                    sms(foundContacts.get(i));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openApp(AppsInfo info) {
        helper.addSearchQuery(new VoiceSearchResultModel("Apps",info.getName(),2));
        context.startActivity(info.getIntent());
    }

    private void performAction(String link){
        Uri uri = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
    private void retrieveApps(){
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        appsInfos = new ArrayList<>();
        for (ApplicationInfo packageInfo : packages) {
            if(context.getPackageManager().getLaunchIntentForPackage(packageInfo.packageName)!=null) {
                String name = packageInfo.loadLabel(context.getPackageManager()).toString();
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageInfo.packageName);
                appsInfos.add(new AppsInfo(name,intent));
            }
        }
    }

    public List<AppsInfo> getAppsInfos() {
        return appsInfos;
    }

    public List<ContactDetail> getContactDetails() {
        return contactDetails;
    }

    private void getContactList() {
        contactDetails = new ArrayList<>();
        Cursor cur = context.getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null,  ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        while (cur.moveToNext()) {
            String name = cur.getString(cur.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cur.getString(cur.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactDetails.add(new ContactDetail(name, number));
        }
        cur.close();
    }
}
