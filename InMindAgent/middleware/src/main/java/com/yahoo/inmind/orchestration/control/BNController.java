package com.yahoo.inmind.orchestration.control;

import android.content.Context;

import com.yahoo.inmind.sensors.speech.control.SpeechController;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by oscarr on 2/9/15.
 *
 * Scenario:
 *
 */
public class BNController {

    public static BehaviorNetwork network;
    public static int NUM_BEHAVIORS;
    public static int NUM_VARIABLES;

    public static final String DEFINE_DESTINATION = "DEFINE-DESTINATION";
    public static final String NO_DESTINATION = "NO-DESTINATION";
    public static final String PLAN_TRAVEL_REQUEST = "PLAN-TRAVEL-REQUEST";
    public static final String OK_DESTINATION = "OK-DESTINATION";
    public static final String TRAVEL_TO_DESTINATION = "TRAVEL-TO-DESTINATION";
    public static final String CHECK_TRIP_DATES = "CHECK-TRIP-DATES";
    public static final String NO_TRAVEL_DATES = "NO-TRAVEL-DATES";
    public static final String OK_TRAVEL_DATES = "OK-TRAVEL-DATES";
    public static final String RE_SCHEDULE_TRIP = "RE-SCHEDULE-TRIP";
    public static final String EVENT_ADDED = "EVENT-ADDED";
    public static final String CHECK_WEATHER_TRIP = "CHECK-WEATHER-TRIP";
    public static final String NO_WEATHER_CHECKED = "NO-WEATHER-CHECKED";
    public static final String WEATHER_CHECKED = "WEATHER-CHECKED";
    public static final String CHECK_NEWS = "CHECK-NEWS";
    public static final String NO_NEWS_CHECKED = "NO-NEWS-CHECKED";
    public static final String NEWS_CHECKED = "NEWS-CHECKED";
    public static final String BOOK_HOTEL = "BOOK-HOTEL";
    public static final String NO_HOTEL_BOOKED = "NO-HOTEL-BOOKED";
    public static final String HOTEL_BOOKED = "HOTEL-BOOKED";
    public static final String FLIGHT_BOOKED = "FLIGHT-BOOKED";
    public static final String NO_FLIGHT_BOOKED = "NO-FLIGHT-BOOKED";
    public static final String MAKE_ITINERARY = "MAKE-ITINERARY";
    public static final String CHECK_EVENT_DATES = "CHECK-EVENT-DATES";
    public static final String ADD_EVENT_REQUEST = "ADD-EVENT-REQUEST";
    public static final String NO_EVENT_DATE = "NO-EVENT-DATE";
    public static final String ADD_EVENT_CALENDAR = "ADD-EVENT-CALENDAR";
    public static final String OK_EVENT_DATE = "OK-EVENT-DATE";
    public static final String BOOK_FLIGHT = "BOOK-FLIGHT";
    public static final String CALENDAR_EVENTS = "CALENDAR-EVENTS";
    public static final String OVERLAPPED_DATES = "OVERLAPPED-DATES";
    public static final String CONFLICT_TRIP = "CONFLICT-TRIP";
    
    public static String CHUNK_DESTINATION = "DESTINATION";
    public static String CHUNK_START_DATE_TRIP = "START-DATE-TRIP";
    public static String CHUNK_END_DATE_TRIP = "END-DATE-TRIP";
    public static String CHUNK_NEW_EVENT = "NEW-EVENT";

    static int selectedBeh = -1;
    static Vector<String> states;
    static Vector<String> goals;
    static int scenario = 1;
    private HashMap<String, Object> workingMemory;
    private HashMap<String, Object> longTermMemory;
    private Context context;
    private SpeechController speechController;
    private boolean flagReschedule = true;
    private int contWeather = 0;
    private int contNews = 0;
    private int contHotel = 0;

    public BNController( Context context ){


    }


    private int contFlight = 0;

    public void setSpeechController(SpeechController speechController) {

    }

    public Double[] getActivations(){
        return network.getActivations();
    }

    public int test( ){
        return 0;
    }

    private void generateStateGoal(){

    }

    private void executeBehavior( int idx ){

    }

    private boolean checkDates( Date startDate, Date endDate){

        return true;
    }

    private boolean checkDates( Date date ){
        return checkDates( date, date );
    }

    public HashMap getWorkingMemory() {
        return workingMemory;
    }

    private void popUpMessage( final String message ){

    }

    private boolean checkTravelDates(){
        return true;
    }

    private boolean checkTripDates(){

        return false;
    }

    private void addStateWithCheck(String state) {

    }

    private void removePredicate(String predicate){

    }
}
