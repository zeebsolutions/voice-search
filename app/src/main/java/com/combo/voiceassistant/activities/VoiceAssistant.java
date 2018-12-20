package com.combo.voiceassistant.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.combo.voiceassistant.Listener.VoiceRecognitionCallBackListener;
import com.combo.voiceassistant.R;
import com.combo.voiceassistant.interfaces.ExtractWitAi;
import com.combo.voiceassistant.interfaces.OnVoiceRecognitionListener;
import com.combo.voiceassistant.models.WitModels.Wit;
import com.combo.voiceassistant.utils.VoiceTasks;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.combo.voiceassistant.activities.VoiceSearch.permissions;

public class VoiceAssistant extends AppCompatActivity {

    SpeechRecognizer speechRecognizer;

    VoiceRecognitionCallBackListener callBackListener;

    VoiceTasks voiceTasks;

    ImageButton startListening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_assistant);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            if(permissions(this))
                voiceTasks = new VoiceTasks(this);

        callBackListener = new VoiceRecognitionCallBackListener();
        callBackListener.setVoiceRecognitionListener(new OnVoiceRecognitionListener() {
            @Override
            public void onVoiceRecognitionListener(String query) {
                //extractTextMeaning(query);
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(callBackListener);

        startListening = findViewById(R.id.start_listening);
        startListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startListening.setClickable(false);
                    extractTextMeaning(0);
                /*if(permissions(VoiceAssistant.this))
                    startRecognition();*/
            }
        });
    }

    private void startRecognition(){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS,5);
        speechRecognizer.startListening(intent);

    }

    private void extractTextMeaning(final int position){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.wit.ai/")
                .build();

        ExtractWitAi witAi = retrofit.create(ExtractWitAi.class);

        Call<Wit> call = witAi
                .getWitAi("https://api.wit.ai/message?v=2128632000494415&" +
                        "q=open "+voiceTasks.getAppsInfos().get(position).getName());
        call.enqueue(new Callback<Wit>() {
            @Override
            public void onResponse(Call<Wit> call, Response<Wit> response) {
                if(position+1<voiceTasks.getAppsInfos().size()){
                    Log.i("Position",position+"");
                    extractTextMeaning(position+1);
                }
                else {
                    startListening.setClickable(true);
                    Toast.makeText(VoiceAssistant.this, "Over", Toast.LENGTH_SHORT).show();
                }/*Wit wit = response.body();
                Log.i("Response",wit.getText());
                Toast.makeText(VoiceAssistant.this, wit.getText(), Toast.LENGTH_SHORT).show();
*/            }

            @Override
            public void onFailure(Call<Wit> call, Throwable t) {
                startListening.setClickable(true);
                t.printStackTrace();
                Toast.makeText(VoiceAssistant.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
}
