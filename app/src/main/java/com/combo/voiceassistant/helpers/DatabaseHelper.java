package com.combo.voiceassistant.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.combo.voiceassistant.models.VoiceSearchHistoryModel;
import com.combo.voiceassistant.models.VoiceSearchResultModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Muhammad on 9/23/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASENAME="VoiceSearchDB";
    private static final String TABLENAME="SearchData";
    private static final int DATABASEVERSION=1;

    public DatabaseHelper(Context context)
    {
        super(context,DATABASENAME,null,DATABASEVERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLENAME+"(id integer primary key autoincrement, search_action text, search_query text, request_code int);");
    }

    public boolean addSearchQuery(VoiceSearchResultModel model)
    {
        SQLiteDatabase db=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
            contentValues.put("search_action",model.getAction());
            contentValues.put("search_query",model.getQuery());
            contentValues.put("request_code",model.getRequestCode());
            db.insert(TABLENAME,null,contentValues);
        db.close();
        return true;
    }
    public List<VoiceSearchHistoryModel> getSearchQueries()
    {
        List<VoiceSearchHistoryModel> models = new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor cursor=db.query(TABLENAME,new String[]{"id","search_action","search_query","request_code"},
                null,null,null,null,null);
        while (cursor.moveToNext()){
            models.add(
                    new VoiceSearchHistoryModel(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getInt(3)
                    )
            );
        }
        db.close();
        cursor.close();
        Collections.reverse(models);
        return models;
    }
    public void update(Map<String, Double> rates)
    {
        SQLiteDatabase db=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        for(Map.Entry<String, Double> rate: rates.entrySet())
        {
            contentValues.put("rate",rate.getValue());
            db.update(TABLENAME,contentValues,"country=?",new String[]{rate.getKey()});
        }
        db.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean deleteHistoryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLENAME,"id=?",new String[]{id+""})>0;
    }


    public boolean deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLENAME,null,null)>0;
    }
}
