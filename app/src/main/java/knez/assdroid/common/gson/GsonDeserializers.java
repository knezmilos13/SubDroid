package knez.assdroid.common.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import solid.collections.SolidList;

class GsonDeserializers {

    public static class InstantDeserializer implements JsonDeserializer<Instant> {
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Instant.ofEpochSecond(json.getAsJsonPrimitive().getAsLong());
        }
    }

    public static class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
        }
    }

    public static class SolidListDeserializer implements JsonDeserializer<SolidList<?>> {
        public SolidList<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            final Type valueType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];

            ArrayList<?> list = ctx.deserialize(json, new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() { return new Type[]{valueType}; }
                @Override
                public Type getRawType() { return ArrayList.class; }
                @Override
                public Type getOwnerType() { return null; }
            });

            return new SolidList<>(list);
        }
    }

}
