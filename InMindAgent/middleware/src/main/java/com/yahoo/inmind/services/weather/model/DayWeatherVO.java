package com.yahoo.inmind.services.weather.model;

/**
 * Created by oscarr on 9/10/15.
 */
public class DayWeatherVO {

    private String year;
    private String month;
    private String day;
    private String lowTemp;
    private String highTemp;
    private String condition;
    private String dayOfWeek;


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
        if( month.equals("Jan") ){
            this.month = "01";
        }else if( month.equals("Feb") ){
            this.month = "02";
        }else if( month.equals("Mar") ){
            this.month = "03";
        }else if( month.equals("Apr") ){
            this.month = "04";
        }else if( month.equals("May") ){
            this.month = "05";
        }else if( month.equals("Jun") ){
            this.month = "06";
        }else if( month.equals("Jul") ){
            this.month = "07";
        }else if( month.equals("Aug") ){
            this.month = "08";
        }else if( month.equals("Sep") ){
            this.month = "09";
        }else if( month.equals("Oct") ){
            this.month = "10";
        }else if( month.equals("Nov") ){
            this.month = "11";
        }else if( month.equals("Dec") ){
            this.month = "12";
        }else {
            this.month = month;
        }
    }

    public Integer getDay() {
        return Integer.valueOf(day);
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getLowTemp() {
        return Integer.valueOf(lowTemp);
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    public Integer getHighTemp() {
        return Integer.valueOf(highTemp);
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setDate(String date){
        String[] dateArray = date.split( " " );
        setDay(dateArray[0]);
        setMonth( dateArray[1] );
        setYear( dateArray[2] );
    }

    @Override
    public String toString() {
        return  dayOfWeek + "," + year + "/" + month + "/" + day + " -> " +
                "low: " + lowTemp +
                "˚F, high: " + highTemp +
                "˚F, condition: " + condition;
    }
}
