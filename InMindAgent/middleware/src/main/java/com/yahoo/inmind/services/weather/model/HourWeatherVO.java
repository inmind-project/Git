package com.yahoo.inmind.services.weather.model;

/**
 * Created by oscarr on 9/10/15.
 */
public class HourWeatherVO {
    private String lowTempMetric;
    private String lowTempEnglish;
    private String highTempMetric;
    private String highTempEnglish;
    private String feelslikeTempMetric;
    private String feelslikeTempEnglish;
    private String snowMetric;
    private String snowEnglish;
    private String year;
    private String month;
    private String day;
    private String hour;
    private String minute;
    private String condition;
    private String iconUrl;
    private String humidity;

    public Integer getLowTempMetric() {
        return Integer.valueOf(lowTempMetric);
    }

    public void setLowTempMetric(String lowTempMetric) {
        this.lowTempMetric = lowTempMetric;
    }

    public Integer getLowTempEnglish() {
        return Integer.valueOf(lowTempEnglish);
    }

    public void setLowTempEnglish(String lowTempEnglish) {
        this.lowTempEnglish = lowTempEnglish;
    }

    public Integer getHighTempMetric() {
        return Integer.valueOf(highTempMetric);
    }

    public void setHighTempMetric(String highTempMetric) {
        this.highTempMetric = highTempMetric;
    }

    public Integer getHighTempEnglish() {
        return Integer.valueOf(highTempEnglish);
    }

    public void setHighTempEnglish(String highTempEnglish) {
        this.highTempEnglish = highTempEnglish;
    }

    public Integer getFeelslikeTempMetric() {
        return Integer.valueOf(feelslikeTempMetric);
    }

    public void setFeelslikeTempMetric(String feelslikeTempMetric) {
        this.feelslikeTempMetric = feelslikeTempMetric;
    }

    public Integer getFeelslikeTempEnglish() {
        return Integer.valueOf(feelslikeTempEnglish);
    }

    public void setFeelslikeTempEnglish(String feelslikeTempEnglish) {
        this.feelslikeTempEnglish = feelslikeTempEnglish;
    }

    public Integer getSnowMetric() {
        return Integer.valueOf(snowMetric);
    }

    public void setSnowMetric(String snowMetric) {
        this.snowMetric = snowMetric;
    }

    public Integer getSnowEnglish() {
        return Integer.valueOf(snowEnglish);
    }

    public void setSnowEnglish(String snowEnglish) {
        this.snowEnglish = snowEnglish;
    }

    public Integer getYear() {
        return Integer.valueOf(year);
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Integer getMonth() {
        return Integer.valueOf(month);
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getDay() {
        return Integer.valueOf(day);
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getHour() {
        return Integer.valueOf(hour);
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getHumidity() {
        return Integer.valueOf(humidity);
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public Integer getMinute() {
        return Integer.valueOf(minute);
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    @Override
    public String toString() {
        return  year + "/" + month + "/" + day + " at " + hour +
                ":00 -> condition: " + condition +
                ", low: " + lowTempEnglish +
                "˚F, high: " + highTempEnglish +
                "˚F, feels like: " + feelslikeTempEnglish + "˚F.";
    }
}
