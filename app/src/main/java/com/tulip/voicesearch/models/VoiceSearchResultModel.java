package com.tulip.voicesearch.models;

public class VoiceSearchResultModel {
    private String action;
    private String query;
    private int requestCode;

    public VoiceSearchResultModel(String action, String query, int requestCode) {
        this.action = action;
        this.query = query;
        this.requestCode = requestCode;
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
