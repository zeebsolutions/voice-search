package com.combo.voiceassistant.models.WitModels;

import com.google.gson.annotations.SerializedName;

public class Wit {
    @SerializedName("_text")
    private String text;

    public Wit(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
