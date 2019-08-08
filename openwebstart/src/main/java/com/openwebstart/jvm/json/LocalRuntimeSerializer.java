package com.openwebstart.jvm.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class LocalRuntimeSerializer implements JsonSerializer<LocalJavaRuntime>, JsonDeserializer<LocalJavaRuntime> {

    @Override
    public JsonElement serialize(final LocalJavaRuntime localJavaRuntime, final Type type, final JsonSerializationContext jsonSerializationContext) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(JsonConstants.VERSION_PROPERTY, localJavaRuntime.getVersion());
        jsonObject.addProperty(JsonConstants.VENDOR_PROPERTY, localJavaRuntime.getVendor());
        jsonObject.addProperty(JsonConstants.JAVA_HOME_PROPERTY, localJavaRuntime.getJavaHome().toUri().toString());
        jsonObject.addProperty(JsonConstants.ACTIVE_PROPERTY, localJavaRuntime.isActive());
        jsonObject.addProperty(JsonConstants.OS_PROPERTY, localJavaRuntime.getOperationSystem().name());
        jsonObject.addProperty(JsonConstants.MANAGED_PROPERTY, localJavaRuntime.isManaged());

        final long millis = Optional.ofNullable(localJavaRuntime.getLastUsage())
                .map(t -> t.atZone(ZoneId.of(JsonConstants.TIMEZONE)))
                .map(t -> t.toInstant())
                .map(t -> t.toEpochMilli())
                .orElse(-1L);
        jsonObject.addProperty(JsonConstants.LAST_USAGE_PROPERTY, millis);

        return jsonObject;
    }

    @Override
    public LocalJavaRuntime deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            final String version = jsonObject.get(JsonConstants.VERSION_PROPERTY).getAsString();
            final String vendor = jsonObject.get(JsonConstants.VENDOR_PROPERTY).getAsString();
            final Path javaHome = Paths.get(new URI(jsonObject.get(JsonConstants.JAVA_HOME_PROPERTY).getAsString()));
            final boolean active = jsonObject.get(JsonConstants.ACTIVE_PROPERTY).getAsBoolean();
            final boolean managed = jsonObject.get(JsonConstants.MANAGED_PROPERTY).getAsBoolean();


            final OperationSystem os = OperationSystem.valueOf(jsonObject.get(JsonConstants.OS_PROPERTY).getAsString());
            final LocalDateTime lastUsage = LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonObject.get(JsonConstants.LAST_USAGE_PROPERTY).getAsLong()), ZoneId.of(JsonConstants.TIMEZONE));

            return new LocalJavaRuntime(version, os, vendor, javaHome, lastUsage, active, managed);

        } catch (final Exception e) {
            throw new JsonParseException("Can not parse LocalJavaRuntime", e);
        }
    }
}
