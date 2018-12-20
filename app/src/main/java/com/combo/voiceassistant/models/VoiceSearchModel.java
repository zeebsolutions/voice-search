package com.combo.voiceassistant.models;

public class VoiceSearchModel {
    private int image;
    private String name;
    private int requestCode;

    public VoiceSearchModel(int image, String name, int requestCode) {
        this.image = image;
        this.name = name;
        this.requestCode = requestCode;
    }

    public String getName() {
        return name;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getImage() {
        return image;
    }
}
