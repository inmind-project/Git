package com.yahoo.inmind.sensors.phonecall.model;

import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;

import java.util.ArrayList;

/**
 * Created by oscarr on 9/29/15.
 */
public class PhoneCallProposition extends PropositionalStatement {

    public PhoneCallProposition(){
        componentName = Constants.PHONECALL;
    }

    public PhoneCallProposition(String attribute, String operator, Object value) {
        super(attribute, operator, value.toString());
        componentName = Constants.PHONECALL;
    }

    public PhoneCallProposition(String attribute, String operator, Object value, String referenceAttribute) {
        super(attribute, operator, value.toString(), referenceAttribute);
        componentName = Constants.PHONECALL;
    }

    @Override
    public Object validate(Object objValue) {
        return false;
    }

    @Override
    public ArrayList validate() {
        return getList(null);
    }
}
