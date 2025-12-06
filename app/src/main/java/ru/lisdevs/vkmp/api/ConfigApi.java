package ru.lisdevs.vkmp.api;

import retrofit2.Call;
import retrofit2.http.GET;
import ru.lisdevs.vkmp.api.model.AppConfig;

public interface ConfigApi {
    @GET("config.json")
    Call<AppConfig> getConfig();
}