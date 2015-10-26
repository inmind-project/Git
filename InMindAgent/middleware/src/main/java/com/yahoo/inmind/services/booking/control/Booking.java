package com.yahoo.inmind.services.booking.control;

import com.yahoo.inmind.services.booking.model.Criteria;

import java.util.ArrayList;

/**
 * Created by oscarr on 8/3/15.
 */
public interface Booking {

    void fillFast();
    ArrayList search( Criteria criteria );
    boolean checkInputs();
}
