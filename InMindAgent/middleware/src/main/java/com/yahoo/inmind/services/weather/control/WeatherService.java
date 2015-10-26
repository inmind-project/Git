package com.yahoo.inmind.services.weather.control;

import android.util.JsonReader;

import com.yahoo.inmind.comm.weather.model.WeatherEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.control.UtilServiceAPIs;
import com.yahoo.inmind.services.generic.control.GenericService;
import com.yahoo.inmind.services.location.control.LocationService;
import com.yahoo.inmind.services.location.model.LocationVO;
import com.yahoo.inmind.services.weather.model.DayWeatherVO;
import com.yahoo.inmind.services.weather.model.HourWeatherVO;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherService extends GenericService {

    private ConcurrentHashMap<String, ForecastReport> forecastReports;


    public WeatherService() {
        super("WeatherService", null);
        if( actions.isEmpty() ) {
            this.actions.add(Constants.ACTION_WEATHER);
        }
        forecastReports = new ConcurrentHashMap<>();
    }

    @Override
    public void doAfterBind() {
    }

    public Thread findWeatherData(final LocationVO locationVO, final int forecastMode,
                                  final long refreshTime, final boolean forceRefresh,
                                  final List<WeatherProposition> rules, final boolean isSendReponse) {
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    LocationVO locationTemp = locationVO;
                    locationTemp = getPlaceLocation( locationTemp, true );
                    if( locationTemp != null ) {
                        ForecastReport forecastReport = getForecastReport(locationTemp, refreshTime, rules);
                        if (forecastReport != null) {
                            forecastReport.forecastMode = forecastMode;
                            if (forecastMode == Constants.WEATHER_FORECAST_HOURLY) {
                                queryHourlyWeather(forceRefresh, forecastReport, isSendReponse);
                            } else if (forecastMode == Constants.WEATHER_FORECAST_DAILY) {
                                queryDailyWeather(forceRefresh, forecastReport, isSendReponse);
                            } else {
                                if (forceRefresh) {
                                    forecastReport.hourlyWeatherList.clear();
                                    forecastReport.dailyWeatherList.clear();
                                }
                                queryDailyWeather(forceRefresh, forecastReport, isSendReponse).join();
                                queryHourlyWeather(forceRefresh, forecastReport, isSendReponse).join();
                            }
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
        return t;
    }

    private Thread queryDailyWeather(final boolean forceRefresh, final ForecastReport forecastReport,
                                    final boolean isSendReponse) {
        Thread t = new Thread() {
            public void run() {
                try {
                    if (forceRefresh || forecastReport.dailyWeatherList.isEmpty()) {
                        findDailyWeather(forecastReport, isSendReponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        return t;
    }

    private Thread queryHourlyWeather(final boolean forceRefresh, final ForecastReport forecastReport,
                                     final boolean isSendReponse) {
        Thread t = new Thread() {
            public void run() {
                try {
                    if (forceRefresh || forecastReport.hourlyWeatherList.isEmpty()) {
                        findHourlyWeather( forecastReport, isSendReponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        return t;
    }


    private void findDailyWeather(final ForecastReport forecastReport, final boolean isSendReponse) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(UtilServiceAPIs.API_FORECAST_YQL_URL.replace("replace_place",
                    URLEncoder.encode(forecastReport.place, "UTF-8")));
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            parseDailyWeather(reader, forecastReport, isSendReponse);
            reader.close();
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
    }


    private void findHourlyWeather(final ForecastReport forecastReport, final boolean isSendReponse) {
        HttpURLConnection urlConnection = null;
        try {
            LocationVO locationVO = forecastReport.locationVO;
            URL url = new URL(String.format(UtilServiceAPIs.API_WUNDERGROUND,
                    locationVO.getCountryCode().equals("US") ? locationVO.getStateCode()
                            : locationVO.getCountryCode(), locationVO.getPlace() ));
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            parseHourlyWeather(reader, forecastReport, isSendReponse);
            reader.close();
            in.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
    }


    private synchronized ForecastReport getForecastReport(String place, long refreshTime,
                                                          List<WeatherProposition> rules) {
        return getForecastReport( new LocationVO( place ), refreshTime, rules );
    }


    private synchronized ForecastReport getForecastReport(LocationVO locationVO, long refreshTime,
                                                          List<WeatherProposition> rules) {

        String place = locationVO.getSubArea() != null? locationVO.getSubArea()
                : locationVO.getCity() != null? locationVO.getCity()
                : locationVO.getWoeidVO() != null && locationVO.getWoeidVO().getName() != null?
                locationVO.getWoeidVO().getName() : null;
        if( place == null ) {
            mb.send(WeatherEvent.build().setIsError(true)
                    .setErrorMessage("A place is required in order to get the forecast report."));
            return null;
        }
        ForecastReport forecastReport = forecastReports.get(place.replace(" ", "_"));
        if( forecastReport == null ){
            forecastReport = new ForecastReport( refreshTime );
            forecastReport.place = place ;
            forecastReports.put( place, forecastReport );
        }
        if( locationVO == null ){
            locationVO = new LocationVO( place );
        }
        if( forecastReport.locationVO != null ) {
            forecastReport.locationVO.mergeLocation(locationVO);
        }else{
            forecastReport.locationVO = locationVO;
        }

        if( refreshTime > 0 && refreshTime != forecastReport.refreshTime ) {
            forecastReport.refreshTime = refreshTime;
            forecastReport.initTimer();
        }
        if( rules != null && !rules.isEmpty() ){
            forecastReport.rules = rules;
        }
        return forecastReport;
    }


    private void parseDailyWeather(JsonReader reader, ForecastReport forecastReport,
                                  boolean isSendReponse) throws Exception {
        HashMap<String, String> mappings = new HashMap<>();
        mappings.put("low", "setLowTemp");
        mappings.put("high", "setHighTemp");
        mappings.put("text", "setConditions");
        mappings.put("date", "setDate");
        mappings.put("day", "setDayOfWeek");
        mappings.put("query", "");
        mappings.put("results", "");
        mappings.put("channel", "");
        mappings.put("item", "");
        mappings.put("forecast", "");
        reader.setLenient(true);
        ArrayList errors = new ArrayList();
        Util.readJsonToObject(reader, "", mappings, new DayWeatherVO(), forecastReport
                .dailyWeatherList, errors);
        sendResponse( forecastReport, errors, true, isSendReponse);
    }

    private void parseHourlyWeather(JsonReader reader, ForecastReport forecastReport,
                                   boolean isSendReponse) throws Exception {
        HashMap<String, String> mappings = new HashMap<>();
        mappings.put("hourly_forecast", "");
        mappings.put("FCTTIME", "");
        mappings.put("hour_padded", "setHour");
        mappings.put("min", "setMinute");
        mappings.put("year", "setYear");
        mappings.put("mon_padded", "setMonth");
        mappings.put("mday_padded", "setDay");
        mappings.put("temp", "");
        mappings.put("temp.english", "setHighTempEnglish");
        mappings.put("temp.metric", "setHighTempMetric");
        mappings.put("dewpoint", "");
        mappings.put("dewpoint.english", "setLowTempEnglish");
        mappings.put("dewpoint.metric", "setLowTempMetric");
        mappings.put("feelslike", "");
        mappings.put("feelslike.english", "setFeelslikeTempEnglish");
        mappings.put("feelslike.metric", "setFeelslikeTempMetric");
        mappings.put("snow", "");
        mappings.put("snow.english", "setSnowEnglish");
        mappings.put("snow.metric", "setSnowMetric");
        mappings.put("condition", "setConditions");
        mappings.put("humidity", "setHumidity");
        mappings.put("icon_url", "setIconUrl");
        mappings.put("response", "");
        mappings.put("error", "");
        mappings.put("error.description", "break");
        ArrayList errors = new ArrayList();
        Util.readJsonToObject(reader, "", mappings, new HourWeatherVO(), forecastReport
                .hourlyWeatherList, errors);
        sendResponse( forecastReport, errors, true, isSendReponse);
    }

    /**
     *
     * @param forecastReport
     * @param errors
     * @param updateMap when sending partial results to the user (e.g., results after applying rules)
     *                  we do not want to update the HashMap with partial results and remove whole results
     * @param isSendReponse when updating (timer), we may want to check whether rules are triggered before
     *                     sending the response.
     */
    private synchronized void sendResponse(ForecastReport forecastReport, ArrayList errors,
                                           boolean updateMap, boolean isSendReponse){
        if( errors == null || errors.isEmpty() ){
            //forecast mode has to be either DAILY or HOURLY, otherwise both lists (daily and hourly)
            //must be filled before sending the result
            if( isSendReponse && (forecastReport.forecastMode >= 0 || ( forecastReport.forecastMode < 0
                    && !forecastReport.hourlyWeatherList.isEmpty() &&
                    !forecastReport.dailyWeatherList.isEmpty()) )) {
                if (updateMap) {
                    forecastReports.remove(forecastReport.place);
                    forecastReports.put(forecastReport.place, forecastReport);
                }
                WeatherEvent event = WeatherEvent.build()
                        .setPlace(forecastReport.place)
                        .setForecastMode(forecastReport.forecastMode);
                if (forecastReport.forecastMode == Constants.WEATHER_FORECAST_DAILY) {
                    event.setDailyWeather(forecastReport.dailyWeatherList);
                } else if (forecastReport.forecastMode == Constants.WEATHER_FORECAST_HOURLY) {
                    event.setHourlyWeather(forecastReport.hourlyWeatherList);
                } else {
                    event.setDailyWeather(forecastReport.dailyWeatherList);
                    event.setHourlyWeather(forecastReport.hourlyWeatherList);
                }
                mb.send(event);
            }
        }else{
            mb.send( WeatherEvent.build()
                    .setIsError(true)
                    .setErrorMessage( Arrays.toString( errors.toArray()) ));
        }
    }

    private List validateRules(ForecastReport forecastReport) {
        List results = new ArrayList();
        if( forecastReport.rules != null && !forecastReport.rules.isEmpty() ){
            for(WeatherProposition rule : forecastReport.rules) {
                for (HourWeatherVO hourWeatherVO : forecastReport.hourlyWeatherList) {
                    if ( rule.areDatesEqual(hourWeatherVO) ){
                        if( (Boolean) rule.validate( hourWeatherVO ) && !results.contains( hourWeatherVO )){
                            results.add( hourWeatherVO );
                        }
                    }
                }
                for (DayWeatherVO dayWeatherVO : forecastReport.dailyWeatherList) {
                    if ( rule.areDatesEqual(dayWeatherVO) ){
                        if( (Boolean)  rule.validate( dayWeatherVO ) && !results.contains( dayWeatherVO) ){
                             results.add( dayWeatherVO );
                        }
                    }
                }
            }
        }
        return results;
    }


    public int changeForecastMode(String place){
        if( place != null && !place.equals("") ){
            ForecastReport forecastReport = getForecastReport( place , -1, null);
            if( forecastReport != null ){
                if(forecastReport.forecastMode == Constants.WEATHER_FORECAST_DAILY){
                    forecastReport.forecastMode = Constants.WEATHER_FORECAST_HOURLY;
                }else{
                    forecastReport.forecastMode = Constants.WEATHER_FORECAST_DAILY;
                }
                return forecastReport.forecastMode;
            }
        }
        return Constants.WEATHER_FORECAST_HOURLY;
    }

    public List<HourWeatherVO> getHourlyReport(String place){
        if( place != null && place.equals(Constants.LOCATION_CURRENT_PLACE) ){
            LocationVO locationVO = getPlaceLocation( place, false );
            return locationVO == null ? null : getForecastReport(locationVO, -1, null).hourlyWeatherList;
        }else {
            ForecastReport forecastReport = getForecastReport( place, -1, null);
            return forecastReport == null? null : forecastReport.hourlyWeatherList;
        }
    }

    public List<DayWeatherVO> getDailyReport(String place){
        if( place != null && place.equals( Constants.LOCATION_CURRENT_PLACE) ){
            LocationVO locationVO = getPlaceLocation( place, false );
            return locationVO == null ? null : getForecastReport(locationVO, -1, null).dailyWeatherList;
        }else {
            ForecastReport forecastReport = getForecastReport( place, -1, null);
            return forecastReport == null? null : forecastReport.dailyWeatherList;
        }
    }

    @Override
    public void onDestroy(){
        for( ForecastReport forecastReport : forecastReports.values()){
            forecastReport.onDestroy();
        }
        this.forecastReports.clear();
        this.forecastReports = null;
        super.onDestroy();
    }


    private void update( ForecastReport forecastReport ) throws Exception{
        findWeatherData( new LocationVO(forecastReport.place), -1, -1, true, null, false).join();
        List results = validateRules( forecastReport );
        if( !results.isEmpty() ){
            ForecastReport fr = new ForecastReport();
            fr.place = forecastReport.place;
            for( Object result : results ){
                if( result instanceof HourWeatherVO ){
                    fr.hourlyWeatherList.add( (HourWeatherVO) result );
                }
                else if( result instanceof DayWeatherVO ){
                    fr.dailyWeatherList.add( (DayWeatherVO) result );
                }
            }
            forecastReport.forecastMode = -1;
            sendResponse( fr, null, false, true);
        }
    }


    public synchronized void unsubscribe(String place) {
        if( place != null && !place.isEmpty() ){
            if( place.equals(Constants.LOCATION_CURRENT_PLACE )) {
                place = getPlaceLocation( place, false).getPlace();
            }
            ForecastReport forecastReport = forecastReports.get( place );
            if( forecastReport != null ) {
                forecastReport.onDestroy();
                forecastReports.remove(forecastReport.place);
            }
        }
    }

    private LocationVO getPlaceLocation( String place, boolean checkWoeid){
        return serviceLocator.getService( LocationService.class)
                .getPlaceLocation(place, checkWoeid);
    }

    private LocationVO getPlaceLocation( LocationVO locationVO, boolean checkWoeid){
        return serviceLocator.getService( LocationService.class)
                .getPlaceLocation( locationVO, checkWoeid);
    }

    public class ForecastReport {
        private int forecastMode = -1;
        private String place;
        private List<HourWeatherVO> hourlyWeatherList = new ArrayList<>();
        private List<DayWeatherVO> dailyWeatherList = new ArrayList<>();
        private long refreshTime = 60 * 60 * 1000; //in miliseconds -> default: 1 hour
        private List<WeatherProposition> rules;
        private Timer timer;
        private LocationVO locationVO;

        public ForecastReport( long refreshTime ) {
            if( refreshTime > 0 ) {
                this.refreshTime = refreshTime;
                initTimer();
            }
        }

        public ForecastReport(){}

        public void onDestroy(){
            hourlyWeatherList = null;
            dailyWeatherList = null;
            rules = null;
            resetTimer();
            locationVO = null;
        }

        private void resetTimer(){
            if( timer != null ) {
                timer.purge();
                timer.cancel();
                timer = null;
            }
        }

        public void initTimer(){
            resetTimer();
            timer = new Timer();
            timer.schedule( new UpdateTimerTask(), refreshTime, refreshTime );
        }

        private final class UpdateTimerTask extends TimerTask {
            @Override
            public void run() {
                try {
                    update( ForecastReport.this );
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
