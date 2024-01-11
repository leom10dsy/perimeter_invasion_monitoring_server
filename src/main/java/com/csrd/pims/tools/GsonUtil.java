package com.csrd.pims.tools;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;

public class GsonUtil {
    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        gson = builder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .create();
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String jsonStr, Class<T> type) {
        return gson.fromJson(jsonStr, type);
    }
}
