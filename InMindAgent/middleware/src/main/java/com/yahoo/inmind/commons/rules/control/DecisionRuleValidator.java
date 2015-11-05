package com.yahoo.inmind.commons.rules.control;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.commons.rules.model.DecisionRule;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;
import com.yahoo.inmind.effectors.alarm.control.AlarmEffector;
import com.yahoo.inmind.effectors.generic.control.EffectorDataReceiver;
import com.yahoo.inmind.sensors.accelerometer.model.AccelerometerProposition;
import com.yahoo.inmind.sensors.phonecall.model.PhoneCallProposition;
import com.yahoo.inmind.services.calendar.control.CalendarProposition;
import com.yahoo.inmind.services.calendar.model.CalendarEventVO;
import com.yahoo.inmind.services.generic.control.ResourceLocator;
import com.yahoo.inmind.services.news.model.vo.FilterVO;
import com.yahoo.inmind.services.weather.control.WeatherProposition;

import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by oscarr on 10/1/15.
 */
public class DecisionRuleValidator {

    private static DecisionRuleValidator instance;
    private static AlarmManager alarmManager;
    private static ResourceLocator resourceLocator;
    private Context context;

    public static DecisionRuleValidator getInstance() {
        if (instance == null) {
            instance = new DecisionRuleValidator();
        }
        return instance;
    }

    private DecisionRuleValidator() {
        context = MessageBroker.getContext();
        if( resourceLocator == null ){
            resourceLocator = ResourceLocator.getInstance(context);
        }
        if( alarmManager == null ){
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
    }

    /**
     * By default, all the terms of the condition part are considered joined by an AND logical operator
     * which means that if all of the conditions are satisfied then the action will be triggered
     * We need to implement and AND operator.
     */
    //TODO: finish it
    public void validateRule(DecisionRule decisionRule){
        if( resourceLocator.getDecisionRule(decisionRule.getRuleID()) == null ) {
            resourceLocator.addDecisionRule(decisionRule);
        }

        ArrayList triggeredConditions = new ArrayList();
        for(DecisionRule.ConditionElement conditionElement : decisionRule.getConditions() ){
            ArrayList tempList = conditionElement.getProposition().validate();
            if( tempList.isEmpty() ) {
                decisionRule.setPropositionFlag(conditionElement.getProposition(), false, triggeredConditions );
            }else{
                triggeredConditions.addAll(tempList);
                decisionRule.setPropositionFlag(conditionElement.getProposition(), true, triggeredConditions );
            }
        }
    }

    /**
     * When a rule is registered then its conditions can be validated in order to determine whether
     * to trigger its actions or not
     * @param decisionRule
     */
    public void registerRule(DecisionRule decisionRule){
        resourceLocator.addDecisionRule(decisionRule);
        validateRule(decisionRule);
    }

    public void unregisterRule(DecisionRule decisionRule){
        resourceLocator.removeDecisionRule(decisionRule);
    }

    public void unregisterRule(String decisionRuleId){
        DecisionRule decisionRule = resourceLocator.getDecisionRule( decisionRuleId );
        resourceLocator.removeDecisionRule(decisionRule);
    }

    public boolean validateCalendarEvent( CalendarEventVO calendarEventVO, DecisionRule decisionRule){
        HashMap<String, Object> scheduleCalEvents = decisionRule.getCacheMemory().get(Constants.CALENDAR);
        if( scheduleCalEvents == null ){
            scheduleCalEvents = new HashMap<>();
        }
        //check the conditions
        for (DecisionRule.ConditionElement conditionElement : decisionRule.extractConditions(
                Constants.CALENDAR)) {
            if ( !(Boolean) conditionElement.getProposition().validate(calendarEventVO) ) {
                return false;
            }
        }
        scheduleCalEvents.put(calendarEventVO.getUUID(), calendarEventVO);
        decisionRule.getCacheMemory().put(Constants.CALENDAR, scheduleCalEvents);
        for( DecisionRule.ActionElement action : decisionRule.extractActions(Constants.ALARM) ){
            Date date = validateTimeAlarm( action, calendarEventVO.getStartDate(),
                    calendarEventVO.getEndDate() );
            //scheduleActions( decisionRule, Constants.ACTION_SET_ALARM, date, calendarEventVO.getId() );
            triggerAction( action, date, calendarEventVO);
        }
        return true;
    }


    private void triggerAction( DecisionRule.ActionElement actionElement, Date date, Object element ){
        if( actionElement.getComponentName().equals( Constants.ALARM )){
            resourceLocator.lookupEffector( AlarmEffector.class ).setAlarm( (CalendarEventVO) element, date );
        }
    }

    private void scheduleActions( DecisionRule decisionRule, String action, Date date, String uuid ){
        Intent intentAlarm = new Intent(context, EffectorDataReceiver.class);
        intentAlarm.setAction( action );
        intentAlarm.putExtra(Constants.DECISION_RULE, decisionRule.hashCode() );
        intentAlarm.putExtra(Constants.DECISION_RULE_ELEMENT, uuid );
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentAlarm, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
    }


    /**
     * It determines when to fire/trigger the action
     * @param actionElement
     * @param start start time of CalendarEventVO or PhoneCallVO
     * @param end end time of CalendarEventVO or PhoneCallVO
     */
    private Date validateTimeAlarm( DecisionRule.ActionElement actionElement, Date start, Date end ) {
        Date time1, now = time1 = new Date(System.currentTimeMillis());
        HashMap<String, Object> attributes = actionElement.getAttributes();
        if (attributes.get(Constants.ALARM_RELATIVE_TIME) != null) {
            time1 = new Date(now.getTime() + (Long) attributes.get(Constants.ALARM_RELATIVE_TIME));
        } else if (attributes.get(Constants.ALARM_ABSOLUTE_TIME) != null) {
            time1 = Util.getDateTime(now, (String) attributes.get(Constants.ALARM_ABSOLUTE_TIME));
        }

        if (attributes.get(Constants.ALARM_REFERENCE_TIME) != null) {
            if ( attributes.get(Constants.ALARM_CONDITION_AFTER) != null
                    && (Boolean) attributes.get(Constants.ALARM_CONDITION_AFTER)) {
                if ( ((String) attributes.get(Constants.ALARM_REFERENCE_TIME)).endsWith("_NOW")) {
                    return time1;
                } else if (attributes.get(Constants.ALARM_REFERENCE_TIME).equals(
                        Constants.CALENDAR_START_TIME)) {
                    return new Date(start.getTime() + (Long) attributes.get(Constants.ALARM_RELATIVE_TIME));
                } else if (attributes.get(Constants.ALARM_REFERENCE_TIME).equals(
                        Constants.CALENDAR_END_TIME)) {
                    return new Date(end.getTime() + (Long) attributes.get(Constants.ALARM_RELATIVE_TIME));
                }
            }
            if (attributes.get(Constants.ALARM_CONDITION_BEFORE) != null
                    && (Boolean) attributes.get(Constants.ALARM_CONDITION_BEFORE)) {
                if ( ((String) attributes.get(Constants.ALARM_REFERENCE_TIME)).endsWith("_NOW")) {
                    return new Date(now.getTime() - (Long) attributes.get(Constants.ALARM_RELATIVE_TIME));
                } else if (attributes.get(Constants.ALARM_REFERENCE_TIME).equals(
                        Constants.CALENDAR_START_TIME)) {
                    return new Date(start.getTime() - (Long) attributes.get(Constants.ALARM_RELATIVE_TIME));
                } else if (attributes.get(Constants.ALARM_REFERENCE_TIME).equals(
                        Constants.CALENDAR_END_TIME)) {
                    return new Date(end.getTime() - (Long) attributes.get(Constants.ALARM_RELATIVE_TIME));
                }
            }
            if ( attributes.get(Constants.ALARM_CONDITION_AT) != null
                    && (Boolean) attributes.get(Constants.ALARM_CONDITION_AT)) {
                if ( ((String) attributes.get(Constants.ALARM_REFERENCE_TIME)).endsWith("_NOW")) {
                    return now;
                } else if (attributes.get(Constants.ALARM_REFERENCE_TIME).equals(
                        Constants.CALENDAR_START_TIME)) {
                    return start;
                } else if (attributes.get(Constants.ALARM_REFERENCE_TIME).equals(
                        Constants.CALENDAR_END_TIME)) {
                    return end;
                }
                if( attributes.get(Constants.ALARM_REFERENCE_TIME).equals(Constants.CALENDAR_TODAY) ){
                    return time1;
                }
                if( attributes.get(Constants.ALARM_REFERENCE_TIME).equals(Constants.CALENDAR_TOMORROW) ){
                    return Util.getRelativeDate( time1, Calendar.DAY_OF_MONTH, 1);
                }
            }
        }
        return null;
    }

    //TODO: finish it
    public void triggerActions(DecisionRule decisionRule, ArrayList triggeredConditions ) {
        for( DecisionRule.ActionElement action : decisionRule.getActions() ){
            if( action.getComponentName().equals( Constants.ALARM ) ){
                triggerAlarmAction( action, triggeredConditions );
            }else if( action.getComponentName().equals( Constants.TOAST) ){
                triggerToastMsgAction( action );
            }
        }
    }

    private void triggerAlarmAction(DecisionRule.ActionElement actionElement, ArrayList triggeredConditions ){
        if( triggeredConditions == null || triggeredConditions.isEmpty() ) {
            resourceLocator.lookupEffector(AlarmEffector.class).playRingtone(
                    ((Number) actionElement.getAttributes().get(Constants.ALARM_RINGTONE_TYPE)).intValue());
        }else{
            //do something with the CalendarEvents??
            resourceLocator.lookupEffector(AlarmEffector.class).playRingtone(
                    ((Number) actionElement.getAttributes().get(Constants.ALARM_RINGTONE_TYPE)).intValue());
        }
    }

    private void triggerToastMsgAction(final DecisionRule.ActionElement actionElement){
        final Activity currentActivity = resourceLocator.getTopActivity();
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText( currentActivity, (String) actionElement.getAttributes()
                                .get(Constants.TOAST_MESSAGE), Toast.LENGTH_LONG).show();
            }
        });
    }

    //TODO: finish it
    public Class<? extends PropositionalStatement> extractProposition(String type) {
        Class<? extends PropositionalStatement> clazz = null;
        if (type.equals(Constants.CALENDAR)) {
            clazz = CalendarProposition.class;
        } else if (type.equals(Constants.NEWS)) {
            clazz = FilterVO.class;
        } else if (type.equals(Constants.WEATHER)) {
            clazz = WeatherProposition.class;
        } else if (type.equals(Constants.PHONECALL)) {
            clazz = PhoneCallProposition.class;
        } else if (type.equals(Constants.ACCELEROMETER)) {
            clazz = AccelerometerProposition.class;
        }
        return clazz;
    }
}
