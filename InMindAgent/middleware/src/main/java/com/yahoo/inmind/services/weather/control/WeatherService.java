package com.yahoo.inmind.services.weather.control;

import android.util.JsonReader;

import com.yahoo.inmind.comm.weather.model.WeatherEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.ExecutableTask;
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
        super(null);
        if( actions.isEmpty() ) {
            this.actions.add(Constants.ACTION_WEATHER);
        }
        forecastReports = new ConcurrentHashMap<>();
    }

    @Override
    public void doAfterBind() {
    }

    public void findWeatherData(final LocationVO locationVO, final int forecastMode,
                                  final long refreshTime, final boolean forceRefresh,
                                  final List<WeatherProposition> rules, final boolean isSendReponse) {
        Util.printThread("1. findWeatherData");
        Util.execute(new ExecutableTask() {
            @Override
            public void run() {
                try {
                    Util.printThread("2. findWeatherData.run");
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
                                queryDailyWeather(forceRefresh, forecastReport, isSendReponse);
                                queryHourlyWeather(forceRefresh, forecastReport, isSendReponse);
                            }
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                Util.printThread("Finishing");
            }
        }, !isSendReponse );
        // when isSendResponse is false that means that we need to wait for another process to finish
        // and therefore we need to execute the thread in serial mode (true).
    }

    private void queryDailyWeather( boolean forceRefresh, ForecastReport forecastReport,
                                    boolean isSendReponse) {
        Util.printThread("7. queryDailyWeather");
       try {
           if (forceRefresh || forecastReport.dailyWeatherList.isEmpty()) {
               findDailyWeather(forecastReport, isSendReponse);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private void queryHourlyWeather(boolean forceRefresh, ForecastReport forecastReport,
                                     boolean isSendReponse) {
        Util.printThread("5. queryHourlyWeather");
        try {
            if (forceRefresh || forecastReport.hourlyWeatherList.isEmpty()) {
                findHourlyWeather(forecastReport, isSendReponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void findDailyWeather(final ForecastReport forecastReport, final boolean isSendReponse) {
        Util.printThread("8. findDailyWeather");
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
        Util.printThread("6. findHourlyWeather");
        HttpURLConnection urlConnection = null;
        try {
            LocationVO locationVO = forecastReport.locationVO;
            URL url = new URL(String.format(UtilServiceAPIs.API_WUNDERGROUND,
                    locationVO.getCountryCode().equals("US") ? locationVO.getStateCode()
                            : locationVO.getCountryCode(), locationVO.getPlace()));
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
        Util.printThread("getForecastReport1");
        return getForecastReport( new LocationVO( place ), refreshTime, rules );
    }


    private synchronized ForecastReport getForecastReport(LocationVO locationVO, long refreshTime,
                                                          List<WeatherProposition> rules) {
        Util.printThread("4. getForecastReport2");
        String place = locationVO.getSubArea() != null? locationVO.getSubArea()
                : locationVO.getCity() != null? locationVO.getCity()
                : locationVO.getWoeidVO() != null && locationVO.getWoeidVO().getName() != null?
                locationVO.getWoeidVO().getName() : null;
        if( place == null ) {
            mb.send( WeatherService.this,
                    WeatherEvent.build().setIsError(true)
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
        Util.printThread("parseDailyWeather");
        HashMap<String, String> mappings = new HashMap<>();
        mappings.put("low", "setLowTemp");
        mappings.put("high", "setHighTemp");
        mappings.put("text", "setCondition");
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
        Util.printThread("parseHourlyWeather");
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
        mappings.put("condition", "setCondition");
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
        Util.printThread("sendResponse");
        if( errors == null || errors.isEmpty() ){
            //forecast mode has to be either DAILY or HOURLY, otherwise both lists (daily and hourly)
            //must be filled before sending the result
            if( (forecastReport.forecastMode >= 0 || ( forecastReport.forecastMode < 0 //isSendReponse &&
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
                Util.printThread("Response is sent");
                mb.send(WeatherService.this, event);
            }
        }else{
            Util.printThread("Error is sent");
            mb.send( WeatherService.this,
                     WeatherEvent.build()
                    .setIsError(true)
                    .setErrorMessage( Arrays.toString( errors.toArray()) ));
        }
    }

    private List validateRules(ForecastReport forecastReport) {
        Util.printThread("validateRules");
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
        Util.printThread("changeForecastMode");
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
        Util.printThread("getHourlyReport");
        if( place != null && place.equals(Constants.LOCATION_CURRENT_PLACE) ){
            LocationVO locationVO = getPlaceLocation( place, false );
            return locationVO == null ? null : Util.clone( getForecastReport(locationVO, -1, null).hourlyWeatherList);
        }else {
            ForecastReport forecastReport = getForecastReport( place, -1, null);
            return forecastReport == null? null : Util.clone( forecastReport.hourlyWeatherList );
        }
    }

    public List<DayWeatherVO> getDailyReport(String place){
        Util.printThread("getDailyReport");
        if( place != null && place.equals( Constants.LOCATION_CURRENT_PLACE) ){
            LocationVO locationVO = getPlaceLocation( place, false );
            return locationVO == null ? null : Util.clone(getForecastReport(locationVO, -1, null).dailyWeatherList);
        }else {
            ForecastReport forecastReport = getForecastReport( place, -1, null);
            return forecastReport == null? null : Util.clone(forecastReport.dailyWeatherList);
        }
    }

    @Override
    public void onDestroy(){
        Util.printThread("onDestroy");
        for( ForecastReport forecastReport : forecastReports.values()){
            forecastReport.onDestroy();
        }
        this.forecastReports.clear();
        this.forecastReports = null;
        super.onDestroy();
    }


    private void update( ForecastReport forecastReport ) throws Exception{
        Util.printThread("update");
        findWeatherData( new LocationVO(forecastReport.place), -1, -1, true, null, false);
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

    public synchronized void unsubscribe(final String place) {
        Util.printThread("unsubscribe");
        Util.execute(new ExecutableTask() {
            @Override
            public void run() {
                Util.printThread("unsubscibe.run");
                String placeCopy = place;
                if( placeCopy != null && !placeCopy.isEmpty() ){
                    if( placeCopy.equals(Constants.LOCATION_CURRENT_PLACE )) {
                        placeCopy = getPlaceLocation( placeCopy, false).getPlace();
                    }
                    ForecastReport forecastReport = forecastReports.get( placeCopy );
                    if( forecastReport != null ) {
                        forecastReport.onDestroy();
                        forecastReports.remove(forecastReport.place);
                    }
                }
            }
        });
    }

    private LocationVO getPlaceLocation( String place, boolean checkWoeid){
        Util.printThread("getPlaceLocation1");
        return resourceLocator.lookupService( LocationService.class)
                .getPlaceLocation(place, checkWoeid);
    }

    private LocationVO getPlaceLocation( LocationVO locationVO, boolean checkWoeid){
        Util.printThread("3. getPlaceLocation2");
        return resourceLocator.lookupService( LocationService.class)
                .getPlaceLocation( locationVO, checkWoeid);
    }

    public HourWeatherVO getCurrentWeather() {
        Util.printThread("getCurrentWeather");
        List<HourWeatherVO> hourReport = forecastReports.get( Constants.LOCATION_CURRENT_PLACE )
                .hourlyWeatherList;
        if( hourReport != null && !hourReport.isEmpty() ){
            return hourReport.get( hourReport.size() - 1 );
        }
        return null;
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
