package com.openwebstart.jvm.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;

public class JsonHandler {

    private static final JsonHandler INSTANCE = new JsonHandler();

    private final Gson gson;

    private JsonHandler() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalJavaRuntime.class, new LocalRuntimeSerializer())
                .registerTypeAdapter(RemoteJavaRuntime.class, new RemoteRuntimeSerializer())
                .registerTypeAdapter(RemoteRuntimeList.class, new RemoteRuntimeListSerializer())
                .setPrettyPrinting()
                .create();
    }

    public String toJson(final Object src) {
        try {
            return gson.toJson(src);
        } catch (final Exception e) {
            throw new JsonSyntaxException("Error in JSON conversion", e);
        }
    }

    public <T> T fromJson(String json, Class<T> cls) throws JsonSyntaxException {
        try {
            return gson.fromJson(json, cls);
        } catch (final Exception e) {
            throw new JsonSyntaxException("Error in JSON conversion", e);
        }
    }

    public static JsonHandler getInstance() {
        return INSTANCE;
    }
}
