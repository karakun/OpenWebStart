package com.openwebstart.jvm.json;

import com.google.gson.JsonArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RemoteRuntimeListSerializer implements JsonSerializer<RemoteRuntimeList>, JsonDeserializer<RemoteRuntimeList> {

    @Override
    public JsonElement serialize(final RemoteRuntimeList remoteRuntimeList, final Type type, final JsonSerializationContext jsonSerializationContext) {
        final JsonObject jsonObject = new JsonObject();

        final JsonArray runtimes = new JsonArray();
        remoteRuntimeList.getRuntimes().forEach(r -> runtimes.add(jsonSerializationContext.serialize(r)));

        jsonObject.add(JsonConstants.RUNTIMES_PROPERTY, runtimes);

        jsonObject.addProperty(JsonConstants.CACHE_TIME_PROPERTY, remoteRuntimeList.getCacheTimeInMillis());

        return jsonObject;
    }

    @Override
    public RemoteRuntimeList deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        final long cacheTime = jsonObject.getAsJsonPrimitive(JsonConstants.CACHE_TIME_PROPERTY).getAsLong();

        final JsonArray jsonArray = jsonObject.getAsJsonArray(JsonConstants.RUNTIMES_PROPERTY);
        final JsonArray jsonArray_1_6 = jsonObject.getAsJsonArray(JsonConstants.RUNTIMES_1_6_PROPERTY);

        final List<RemoteJavaRuntime> runtimes = new ArrayList<>();
        Optional.ofNullable(jsonArray).ifPresent(a -> a.forEach(e -> {
            final RemoteJavaRuntime deserialize = jsonDeserializationContext.deserialize(e, RemoteJavaRuntime.class);
            if (deserialize.getOperationSystem() != OperationSystem.UNKNOWN) {
                runtimes.add(deserialize);
            }
        }));

        Optional.ofNullable(jsonArray_1_6).ifPresent(a -> a.forEach(e -> {
            final RemoteJavaRuntime deserialize = jsonDeserializationContext.deserialize(e, RemoteJavaRuntime.class);
            if (deserialize.getOperationSystem() != OperationSystem.UNKNOWN) {
                runtimes.add(deserialize);
            }
        }));

        return new RemoteRuntimeList(runtimes, cacheTime);
    }
}
