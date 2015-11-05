package com.yahoo.inmind.comm.weather.model;

import com.yahoo.inmind.comm.generic.model.BaseEvent;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.services.weather.model.DayWeatherVO;
import com.yahoo.inmind.services.weather.model.HourWeatherVO;

import java.util.List;

/**
 * Created by oscarr on 9/15/15.
 */
public class WeatherEvent extends BaseEvent {
    private String place;
    private List<DayWeatherVO> dailyWeather;
    private List<HourWeatherVO> hourlyWeather;
    private boolean isError = false;
    private String errorMessage;
    private int forecastMode;

    private WeatherEvent(){ super(); }
    private WeatherEvent(int mbRequestId){ super( mbRequestId); }

    public static WeatherEvent build(){
        return new WeatherEvent();
    }
    public static WeatherEvent build(int mbRequestId){
        return new WeatherEvent(mbRequestId);
    }

    public String getPlace() {
        return place;
    }

    public WeatherEvent setPlace(String place) {
        this.place = place;
        return this;
    }

    public List<DayWeatherVO> getDailyWeather() {
        return  Util.clone( dailyWeather );
    }

    public WeatherEvent setDailyWeather(List<DayWeatherVO> dailyWeather) {
        this.dailyWeather = dailyWeather;
        return this;
    }

    public List<HourWeatherVO> getHourlyWeather() {
        return  Util.clone( hourlyWeather );
    }

    public WeatherEvent setHourlyWeather(List<HourWeatherVO> hourlyWeather) {
        this.hourlyWeather = Util.clone( hourlyWeather );
        return this;
    }

    public boolean isError() {
        return isError;
    }

    public WeatherEvent setIsError(boolean isError) {
        this.isError = isError;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public WeatherEvent setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public int getForecastMode() {
        return forecastMode;
    }

    public WeatherEvent setForecastMode(int forecastMode) {
        this.forecastMode = forecastMode;
        return this;
    }
}
