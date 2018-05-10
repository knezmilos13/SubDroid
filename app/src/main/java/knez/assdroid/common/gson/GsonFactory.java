package knez.assdroid.common.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import io.gsonfire.GsonFireBuilder;
import solid.collections.SolidList;

public class GsonFactory {

    public GsonBuilder getNewStandardGson() {
        return new GsonFireBuilder()
                .createGsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)

                .registerTypeAdapter(Instant.class, new GsonDeserializers.InstantDeserializer())
                .registerTypeAdapter(Instant.class, new GsonSerializers.InstantSerializer())

                .registerTypeAdapter(LocalDate.class, new GsonDeserializers.LocalDateDeserializer())
                .registerTypeAdapter(LocalDate.class, new GsonSerializers.LocalDateSerializer())

                // No serializer - works out of the box (since SolidList implements List)
                .registerTypeAdapter(SolidList.class, new GsonDeserializers.SolidListDeserializer());
    }

}
