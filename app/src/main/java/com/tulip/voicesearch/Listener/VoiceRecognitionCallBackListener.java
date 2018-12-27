package com.tulip.voicesearch.Listener;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import com.tulip.voicesearch.interfaces.OnVoiceRecognitionListener;

import java.util.List;

public class VoiceRecognitionCallBackListener implements RecognitionListener {

    private OnVoiceRecognitionListener voiceRecognitionListener;

    public void setVoiceRecognitionListener(OnVoiceRecognitionListener voiceRecognitionListener) {
        this.voiceRecognitionListener = voiceRecognitionListener;
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {
        List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        /*FilterTask task = new FilterTask();
        task.translateMeaning(results.get(0));*/
        voiceRecognitionListener.onVoiceRecognitionListener(results.get(0));

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}
