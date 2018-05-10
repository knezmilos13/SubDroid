package knez.assdroid.common.db;

import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

public class DbConverters {

    public static class LocalTimeConverter implements PropertyConverter<LocalTime, Long> {

        @Override
        public LocalTime convertToEntityProperty(Long databaseValue) {
            if(databaseValue == null) return null;
            return LocalTime.ofNanoOfDay(databaseValue);
        }

        @Override
        public Long convertToDatabaseValue(LocalTime entityProperty) {
            if(entityProperty == null) return null;
            return entityProperty.toNanoOfDay();
        }
    }

    public static class StringListConverter implements PropertyConverter<List<String>, String> {

        @Override
        public List<String> convertToEntityProperty(String databaseValue) {
            if(databaseValue == null) return null;
            return new ArrayList<>(Arrays.asList(databaseValue.split(",")));
        }

        @Override
        public String convertToDatabaseValue(List<String> entityProperty) {
            if(entityProperty == null) return null;
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i < entityProperty.size(); i++) {
                stringBuilder.append(entityProperty.get(i));
                if(i < entityProperty.size() - 1) stringBuilder.append(",");
            }
            return stringBuilder.toString();
        }

    }

}
