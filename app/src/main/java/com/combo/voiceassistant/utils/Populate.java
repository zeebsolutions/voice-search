package com.combo.voiceassistant.utils;

public class Populate {
    
    public static String getAction(int code) {
        switch (code) {
            case 0:
                return "Call";
            case 1:
                return "Message";
            case 2:
                return "Apps";
            case 3:
                return "Google";
            case 4:
                return "WikiHow";
            case 5:
                return "Wikipedia";
            case 6:
                return "News";
            case 7:
                return "Directions";
            case 8:
                return "Dictionary";
            case 9:
                return "Youtube";
            case 10:
                return "Twitter";
            case 11:
                return "Facebook";
            case 12:
                return "Playstore";
            case 13:
                return "Bing";
            case 14:
                return "Yahoo";
            case 15:
                return "DuckDuckGo";
            case 16:
                return "Ask";
            case 17:
                return "Aol";
            case 18:
                return "Reddit";
            case 19:
                return "Dailymotion";
            case 20:
                return "Meta Cafe";

        }
        return "Voice Search";
    }

}
