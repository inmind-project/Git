package com.yahoo.inmind.services.calendar.control;

import com.yahoo.inmind.comm.calendar.model.CalendarNotificationEvent;
import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.rules.model.DecisionRule;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;
import com.yahoo.inmind.services.calendar.model.CalendarEventVO;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by oscarr on 9/28/15.
 */
public class CalendarProposition extends PropositionalStatement {
    public CalendarProposition(String attribute, String operator, Object value) {
        super(attribute, operator, value.toString());
        MessageBroker.getExistingInstance().subscribe(this);
        componentName = Constants.CALENDAR;
    }

    public CalendarProposition(String attribute, String operator, Object value, String referenceAttribute) {
        super(attribute, operator, value.toString(), referenceAttribute);
        MessageBroker.getExistingInstance().subscribe(this);
        componentName = Constants.CALENDAR;
    }

    @Override
    public Boolean validate(Object calendarEvent) {
        CalendarEventVO calendarEventVO = (CalendarEventVO) calendarEvent;
        if( attribute.equals(Constants.CALENDAR_START_TIME)
                || attribute.equals( Constants.CALENDAR_END_TIME)){
            return validateTimes( calendarEventVO );
        }else if( attribute.equals(Constants.CALENDAR_START_TIME )
                ){
            //calendarEventVO.getEndDate()
        }
        return false;
    }
    

    public Boolean validateDates( Date attributeDate ){
        if( operator.equals( Constants.OPERATOR_DATE_BEFORE ) ){
            //return System.currentTimeMillis() - attributeDate.getTime() == ( Integer.valueOf(value) * 1000 );
        }else if( operator.equals( Constants.OPERATOR_DATE_AFTER ) ){
            //return System.currentTimeMillis() + attributeDate.getTime() == ( Integer.valueOf(value) * 1000 );
        }else if( operator.equals( Constants.OPERATOR_DATE_EQUAL ) ){
            if( value.equals( Constants.CALENDAR_EVENT_NOW )){
                //return new Date( System.currentTimeMillis() );
            }
        }
        return false;
    }


    /**
     * It returns a date/time when to schedule a process which will validateRule/process the action
     * @param calendarEventVO
     * @return
     */
    private boolean validateTimes( CalendarEventVO calendarEventVO ){
        Date time1, time2 = null, now = new Date( System.currentTimeMillis() );
        time1 = attribute.equals( Constants.CALENDAR_START_TIME )? calendarEventVO.getStartDate()
            : attribute.equals( Constants.CALENDAR_END_TIME )? calendarEventVO.getEndDate() : now;
        if( referenceAttribute != null && referenceAttribute.equals( Constants.CALENDAR_END_TIME ) ){
            time2 = calendarEventVO.getEndDate();
        }else if( referenceAttribute != null && referenceAttribute.equals( Constants.CALENDAR_START_TIME ) ){
            time2 = calendarEventVO.getStartDate();
        }else if( referenceAttribute != null && referenceAttribute.equals( Constants.CALENDAR_TODAY )){
            time2 = Util.getDateTime( new Date(), value);
        }else if( referenceAttribute != null && referenceAttribute.equals( Constants.CALENDAR_TOMORROW )){
            time2 = Util.getRelativeDate( Util.getDateTime( new Date(), value), Calendar.DAY_OF_MONTH, 1);
        }

        long threshhold = 5 /* min */ * 60 /* sec */ * 1000 /* millis */;
        if( operator.equals( Constants.OPERATOR_TIME_BEFORE ) ){
            // time1 starts n minutes before now
            if( value.equals( Constants.CALENDAR_EVENT_NOW ) ){
                return false; //null;
            }
            // time1 starts n minutes before time2
            if( time2 != null && ( time2.getTime() + threshhold >= time1.getTime()
                    + (value.contains(":")? 0 : Long.valueOf(value).longValue() )) ){
                return true; //new Date( time1.getTime() - threshhold );
            }
        }else if( operator.equals( Constants.OPERATOR_TIME_AFTER ) ){
            // time1 n minutes after now
            if( referenceAttribute.equals( Constants.CALENDAR_EVENT_NOW ) && ( time1.getTime()
                    + threshhold >= now.getTime() + Long.valueOf( value ) ) ){
                return true; //new Date( time1.getTime() - Long.valueOf( value ));
            }
            // time1 n minutes after time2
            if( time2 != null && ( time2.getTime() + Long.valueOf( value ) >= time1.getTime() ) ){
                return true; //time2;
            }
        }else if( operator.equals( Constants.OPERATOR_TIME_EQUAL ) ){
            if( value.equals( Constants.CALENDAR_EVENT_NOW ) ){
                if( now.getTime() >= time1.getTime() - threshhold && now.getTime() <= time1.getTime() + threshhold ) {
                    return true; //new Date( time1.getTime() + threshhold);
                }else{
                    return false; //null;
                }
            }
            Date date = Util.getDateTime( now, value );
            if( referenceAttribute.equals( Constants.CALENDAR_TODAY ) && now.before( date ) ){
                return true; //date;
            }
            if( referenceAttribute.equals( Constants.CALENDAR_TOMORROW) ){
                String[] valueArray = value.split(":");
                date = Util.getTime( new Date(), Integer.valueOf( valueArray[0]),
                        Integer.valueOf( valueArray[1]) );
                return true; //Util.getRelativeDate(date, Calendar.DAY_OF_MONTH, 1 );
            }
        }
        return false; //null;
    }


    public void onEvent( CalendarNotificationEvent event ){
        if( event.getNewOrModifiedEvent() != null ) {
            if (rules != null) {
                boolean flag = validate(event.getNewOrModifiedEvent());
                for (DecisionRule rule : rules) {
                    rule.setPropositionFlag(this, flag);
                }
            }
        }
    }
}
