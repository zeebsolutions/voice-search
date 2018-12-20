package com.combo.voiceassistant.models;

public class ContactDetail {
    String name;
    String number;

    public ContactDetail(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
