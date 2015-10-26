package com.yahoo.inmind.sensors.accelerometer.model;

import com.yahoo.inmind.comm.accelerometer.model.AccelerometerEvent;
import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.comm.generic.model.MBRequest;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.rules.model.DecisionRule;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;

/**
 * Created by oscarr on 10/14/15.
 */
public class AccelerometerProposition extends PropositionalStatement {

    /**
     * We need this constructor for json deserialization
     */
    public AccelerometerProposition(){
        super();
    }

    public AccelerometerProposition(String attribute, String operator, String value){
        super( attribute, operator, value );
        componentName = Constants.ACCELEROMETER;
        subscribe();
    }

    @Override
    public void subscribe(){
        super.subscribe();
        MessageBroker mb = MessageBroker.getExistingInstance();
        mb.subscribe(this);
        initializeAccelerometer( mb );
    }


    private void initializeAccelerometer(MessageBroker mb){
        mb.send(MBRequest.build(Constants.MSG_SENSOR_SETTINGS)
                .put(Constants.SENSOR_NAME, Constants.SENSOR_ACCELEROMETER)
                .put(Constants.SENSOR_SETTING, Constants.SENSOR_START)
                .put(Constants.ACCELEROMETER_FREQUENCY, 200000L )); //in microseconds
    }

    @Override
    public Boolean validate(Object objValue) {
        AccelerometerEvent event = (AccelerometerEvent) objValue;
        double valueDouble, attributeDouble = 0;
        if( attribute.equals(Constants.ACCELEROMETER_X_AXIS )){
            attributeDouble = event.getAccelerationX();
        }else if( attribute.equals(Constants.ACCELEROMETER_Y_AXIS )){
            attributeDouble = event.getAccelerationY();
        }else if( attribute.equals(Constants.ACCELEROMETER_Z_AXIS )){
            attributeDouble = event.getAccelerationZ();
        }else if( attribute.equals(Constants.ACCELEROMETER_VECTOR_SUM)){
            attributeDouble = Math.sqrt( Math.pow( event.getAccelerationX(), 2) + Math.pow(
                    event.getAccelerationY(), 2) + Math.pow( event.getAccelerationZ(), 2) );
        }
        else if( attribute.equals(Constants.ACCELEROMETER_ACCURACY )){
            attributeDouble = event.getAccuracy();
        }
        valueDouble = Double.valueOf( value );
        return validateNumbers( attributeDouble, valueDouble );
    }


    public void onEvent(AccelerometerEvent event){
        if( rules != null ){
            boolean flag = validate( event );
            for (DecisionRule rule : rules) {
                rule.setPropositionFlag( this, flag );
            }
        }
    }
}
