package com.combo.voiceassistant.interfaces;

import com.combo.voiceassistant.models.WitModels.Wit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

public interface ExtractWitAi {

    @Headers("Authorization: Bearer Y7EFZZL2JDQEMMLFG6OHCXRQLO2CVGB5")

    @GET
    Call<Wit> getWitAi(
            @Url String url
    );

}
