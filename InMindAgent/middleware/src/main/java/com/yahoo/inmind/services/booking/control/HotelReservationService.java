package com.yahoo.inmind.services.booking.control;

import android.util.JsonReader;

import com.yahoo.inmind.comm.hotel.model.HotelReservationEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.control.UtilServiceAPIs;
import com.yahoo.inmind.services.booking.model.Criteria;
import com.yahoo.inmind.services.booking.model.HotelSearchCriteria;
import com.yahoo.inmind.services.booking.model.HotelVO;
import com.yahoo.inmind.services.generic.control.GenericService;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;


public class HotelReservationService extends GenericService implements Booking {
    private static String URL = "http://api.hotwire.com/v1/search/hotel?apikey=";

    private ArrayList<HotelVO> hotels;
    private HotelSearchCriteria searchCriteria = new HotelSearchCriteria();

    public HotelReservationService(){
        super(null);
        if( actions.isEmpty() ) {
            this.actions.add(Constants.ACTION_HOTEL_RESERVATION);
        }
        hotels = new ArrayList<>();
    }

    public void setSearchCriteria(HotelSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public void fillFast() {
        searchCriteria.setAddress("New York");
        searchCriteria.setAdults("1");
        searchCriteria.setRooms("1");
        searchCriteria.setChildren("0");

        Calendar cal= Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 27);
        searchCriteria.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        searchCriteria.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 29);
        searchCriteria.setEndDate(cal.getTime());
    }

    @Override
    public ArrayList<HotelVO> search( Criteria criteria ) {
        searchCriteria = (HotelSearchCriteria) criteria;
        hotels = new ArrayList<>();
        if(checkInputs()){
            String urlString = String.format( URL +
                            "%s&dest=%s" +
                            "&rooms=%s" +
                            "&children=%s" +
                            "&adults=%s" +
                            "&startdate=%s" +
                            "&enddate=%s" +
                            "&limit=%s" +
                            "&sort=%s" +
                            "&format=json",
                    UtilServiceAPIs.API_KEY_HOTWIRE,
                    searchCriteria.getAddress().replaceAll("\\s+", "+"),
                    searchCriteria.getRooms(),
                    searchCriteria.getChildren(),
                    searchCriteria.getAdults(),
                    Util.getDate(searchCriteria.getStartDate(), "MM/dd/yyyy"),
                    Util.getDate( searchCriteria.getEndDate(), "MM/dd/yyyy" ),
                    searchCriteria.getResultLimit(),
                    searchCriteria.getSortBy() );
            hotels = searchHotelShopping(urlString);
        }
        return hotels;
    }

    @Override
    public boolean checkInputs() {
        return searchCriteria.getStartDate().before( searchCriteria.getEndDate() );
    }

    public ArrayList<HotelVO> searchHotelShopping(final String urlString) {
        try {
            Thread t = new Thread() {
                public void run() {
                    HttpURLConnection urlConnection = null;
                    try {
                        URL url = new URL(urlString);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                        try {
                            parseHotelShopping(reader);
                        } finally {
                            reader.close();
                        }
                    } catch (MalformedURLException e) {
                        mb.send( HotelReservationService.this, HotelReservationEvent.build()
                                .setErrorMessage(e.getMessage()) );
                        e.printStackTrace();
                        return;
                    } catch (Exception e) {
                        mb.send( HotelReservationService.this, HotelReservationEvent.build()
                                .setErrorMessage(e.getMessage()));
                        e.printStackTrace();
                    } finally {
                        urlConnection.disconnect();
                    }
                }
            };
            t.start();
            //t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hotels;
    }


    public void parseHotelShopping(JsonReader reader) throws Exception {
        HashMap<String, String> mappings = new HashMap<>();
        mappings.put("Result", "");
        mappings.put("Errors", "");
        mappings.put("DeepLink", "setUrl");
        mappings.put("TotalPrice", "setTotalPrice");
        mappings.put("AmenityCodes", "");
        mappings.put("AmenityCodes.childString", "setAmenityCode");
        mappings.put("AmenityCodes.Code", "setAmenityCode");
        mappings.put("AveragePricePerNight", "setPricePerNight");
        mappings.put("RecommendationPercentage", "setRecommendationPercentage");
        mappings.put("SavingPercentage", "setSavingPercentage");
        mappings.put("StarRating", "setStarRating");
        mappings.put("ErrorMessage", "break");
        mappings.put("Error", "");

        ArrayList errors = new ArrayList();
        hotels.clear();
        Util.readJsonToObject(reader, "", mappings, new HotelVO(), hotels, errors);
        if( errors.isEmpty() ) {
            mb.send(HotelReservationService.this, HotelReservationEvent.build().setHotels(hotels));
        } else{
            mb.send(HotelReservationService.this, HotelReservationEvent.build()
                    .setErrorMessage(Arrays.toString(errors.toArray() )));
        }
    }

    @Override
    public void doAfterBind() {

    }

    @Override
    public void onDestroy(){
        this.searchCriteria = null;
        this.hotels = null;
        super.onDestroy();
    }

}
