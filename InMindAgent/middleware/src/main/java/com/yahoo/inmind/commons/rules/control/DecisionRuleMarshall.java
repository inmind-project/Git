package com.yahoo.inmind.commons.rules.control;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;
import com.yahoo.inmind.sensors.accelerometer.model.AccelerometerProposition;
import com.yahoo.inmind.sensors.phonecall.model.PhoneCallProposition;
import com.yahoo.inmind.services.calendar.control.CalendarProposition;
import com.yahoo.inmind.services.news.model.vo.FilterVO;
import com.yahoo.inmind.services.weather.control.WeatherProposition;

import java.lang.reflect.Type;

/**
 * Created by oscarr on 10/16/15.
 */
public class DecisionRuleMarshall implements JsonDeserializer<Object> {

    @Override
    //TODO: finish it
    public Object deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if( typeOfT.equals( PropositionalStatement.class) ) {
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            String type = jsonObj.get("componentName").getAsString();
            Class<? extends PropositionalStatement> clazz = null;
            if (type.equals(Constants.CALENDAR)) {
                clazz = CalendarProposition.class;
            } else if (type.equals(Constants.NEWS)) {
                clazz = FilterVO.class;
            } else if (type.equals(Constants.WEATHER)) {
                clazz = WeatherProposition.class;
            } else if (type.equals(Constants.PHONECALL)) {
                clazz = PhoneCallProposition.class;
            } else if (type.equals(Constants.ACCELEROMETER)) {
                clazz = AccelerometerProposition.class;
            }

            if (clazz == null) {
                return null;
            }
            PropositionalStatement proposition = context.deserialize(jsonElement, clazz);
            proposition.subscribe();
            return proposition;
        }
        return null;
    }
}
