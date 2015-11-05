package com.yahoo.inmind.services.calendar.control;

import com.yahoo.inmind.comm.calendar.model.CalendarNotificationEvent;
import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.rules.model.DecisionRule;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;
import com.yahoo.inmind.services.calendar.model.CalendarEventVO;
import com.yahoo.inmind.services.generic.control.ResourceLocator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by oscarr on 9/28/15.
 */
public class CalendarProposition extends PropositionalStatement {

    public CalendarProposition(){
        super();
        initialize();
    }

    public CalendarProposition(String attribute, String operator, Object value) {
        super(attribute, operator, value.toString());
        initialize();
    }

    public CalendarProposition(String attribute, String operator, Object value, String referenceAttribute) {
        super(attribute, operator, value.toString(), referenceAttribute);
        initialize();
    }

    private void initialize(){
        subscribe();
        componentName = Constants.CALENDAR;
    }

    @Override
    public ArrayList validate() {
        return getList(ResourceLocator.getExistingInstance().lookupService(CalendarService.class)
                .getEvents());
    }

    @Override
    public Object validate(Object calendarEvent) {
        CalendarEventVO calendarEventVO = (CalendarEventVO) calendarEvent;
        if( attribute.equals(Constants.CALENDAR_START_TIME)
                || attribute.equals( Constants.CALENDAR_END_TIME)){
            return validateTimes( calendarEventVO );
        }else if( attribute.equals(Constants.CALENDAR_START_DATE)
                || attribute.equals( Constants.CALENDAR_END_DATE)){
            return validateDates( calendarEventVO );
        }
        return false;
    }
    

    public Boolean validateDates( CalendarEventVO calendarEventVO ){
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
    private Object validateTimes( CalendarEventVO calendarEventVO ){
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
            if( value.equals( Constants.CALENDAR_EVENT_NOW ) && Util.isDateInRange(time1.getTime(),
                    threshhold, now.getTime()) ){
                return calendarEventVO;
            }
            // time1 starts n minutes before time2
            if( time2 != null && ( time2.getTime() + threshhold >= time1.getTime()
                    + (value.contains(":")? 0 : Long.valueOf(value).longValue() )) ){
                return calendarEventVO;
            }
        }else if( operator.equals( Constants.OPERATOR_TIME_AFTER ) ){
            // time1 is n minutes after now
            if( referenceAttribute.equals( Constants.CALENDAR_EVENT_NOW ) && Util.isDateInRange(
                    time1.getTime(), threshhold, now.getTime() + Long.valueOf( value ) ) ){
                return calendarEventVO;
            }
            // time1 n minutes after time2
            if( time2 != null && ( time2.getTime() + Long.valueOf( value ) >= time1.getTime() ) ){
                return calendarEventVO;
            }
        }else if( operator.equals( Constants.OPERATOR_TIME_EQUAL ) ){
            if( value.equals( Constants.CALENDAR_EVENT_NOW ) &&
                    Util.isDateInRange( time1.getTime(), threshhold, now.getTime() )) {
                return calendarEventVO;
            }

            String[] valueArray = value.split(":");
            Date date = Util.getTime( new Date(), Integer.valueOf( valueArray[0]),
                Integer.valueOf( valueArray[1]) );

            // time1 is today at time: "value"
            if( referenceAttribute.equals( Constants.CALENDAR_TODAY ) && Util.isDateInRange(
                    time1.getTime(), threshhold, date.getTime() ) ){
                return calendarEventVO;
            }
            // time1 is tommorrow at time: "value"
            if( referenceAttribute.equals( Constants.CALENDAR_TOMORROW) ){
                date = Util.getRelativeDate( Calendar.DAY_OF_MONTH, 1 );
                if( Util.isDateInRange( time1.getTime(), threshhold, date.getTime() ) ) {
                    return calendarEventVO;
                }
            }
        }
        return null;
    }


    public void onEvent( CalendarNotificationEvent event ){
        if( event.getNewOrModifiedEvent() != null ) {
            if (rules != null) {
                Object flag = validate(event.getNewOrModifiedEvent());
                for (DecisionRule rule : rules) {
                    rule.setPropositionFlag(this, flag != null, null);
                }
            }
        }
    }

}
