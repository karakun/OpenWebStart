package com.openwebstart.jvm.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;

import java.lang.reflect.Type;
import java.net.URL;

public class RemoteRuntimeSerializer implements JsonSerializer<RemoteJavaRuntime>, JsonDeserializer<RemoteJavaRuntime> {

    @Override
    public JsonElement serialize(final RemoteJavaRuntime remoteJavaRuntime, final Type type, final JsonSerializationContext jsonSerializationContext) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(JsonConstants.VERSION_PROPERTY, remoteJavaRuntime.getVersion().toString());
        jsonObject.addProperty(JsonConstants.VENDOR_PROPERTY, remoteJavaRuntime.getVendor().getName());
        jsonObject.addProperty(JsonConstants.OS_PROPERTY, remoteJavaRuntime.getOperationSystem().name());
        jsonObject.addProperty(JsonConstants.HREF_PROPERTY, remoteJavaRuntime.getHref());


        return jsonObject;
    }

    @Override
    public RemoteJavaRuntime deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            final String version = jsonObject.get(JsonConstants.VERSION_PROPERTY).getAsString();
            final String vendor = jsonObject.get(JsonConstants.VENDOR_PROPERTY).getAsString();
            final OperationSystem os = OperationSystem.valueOf(jsonObject.get(JsonConstants.OS_PROPERTY).getAsString());
            final String href = jsonObject.get(JsonConstants.HREF_PROPERTY).getAsString();


            return new RemoteJavaRuntime(version, os, vendor, href);

        } catch (final Exception e) {
            throw new JsonParseException("Cannot parse RemoteJavaRuntime", e);
        }
    }
}
