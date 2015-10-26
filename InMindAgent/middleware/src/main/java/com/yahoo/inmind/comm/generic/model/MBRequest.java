package com.yahoo.inmind.comm.generic.model;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by oscarr on 12/22/14.
 * This class is used for interchanging data with the Message Broker
 */
public class MBRequest {
    private HashMap<String, Object> map;
    private double[] values;
    private int requestId;

    private MBRequest(int id) {
        this.map = new HashMap();
        this.requestId = id;
    }

    private MBRequest(int id, double... args){
        this.requestId = id;
        this.map = new HashMap();
        values = args;
    }

    public static MBRequest build(int id) {
        return new MBRequest(id);
    }

    public static MBRequest build(int id, double... args){
        return new MBRequest(id, args);
    }


    public MBRequest put( String name, Object value ){
        map.put( name, value );
        return this;
    }

    public Object get( String name ){
        return map.get( name );
    }

    public Bundle convertToBundle(){
        Bundle bundle = new Bundle();
        Iterator<String> it = map.keySet().iterator();
        while( it.hasNext() ){
            String key = it.next();
            Object value = map.get( key );
            if( value instanceof Boolean ){
                bundle.putBoolean( key, (Boolean) value );
            } else if( value instanceof String ){
                bundle.putString(key, (String) value);
            } else if( value instanceof Serializable){
                bundle.putSerializable(key, (Serializable) value);
            } else if( value instanceof Integer){
                bundle.putInt(key, (Integer) value);
            } else if( value instanceof Float){
                bundle.putFloat(key, (Float) value);
            } else if( value instanceof Double){
                bundle.putDouble(key, (Double) value);
            } else if( value instanceof Short){
                bundle.putShort(key, (Short) value);
            } else if( value instanceof Long){
                bundle.putLong(key, (Long) value);
            } else if( value instanceof Parcelable){
                bundle.putParcelable(key, (Parcelable) value);
            }
        }
        return bundle;
    }

    public static HashMap<String, Object> convertToMap( Bundle bundle ){
        Iterator<String> it = bundle.keySet().iterator();
        HashMap<String, Object> map = new HashMap<>();
        while( it.hasNext() ){
            String key = it.next();
            Object value = map.get( key );
            map.put( key, value );
        }
        return map;
    }



    public int getRequestId() {
        return requestId;
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

    public void setMap(HashMap<String, Object> map) {
        this.map = map;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }
}
