package com.combo.voiceassistant.models;

public class VoiceSearchHistoryModel {
    private int id;
    private String action;
    private String query;
    private int requestCode;

    public VoiceSearchHistoryModel(int id, String action, String query, int requestCode) {
        this.id = id;
        this.action = action;
        this.query = query;
        this.requestCode = requestCode;
    }

    public int getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public String getAction() {
        return action;
    }
}
